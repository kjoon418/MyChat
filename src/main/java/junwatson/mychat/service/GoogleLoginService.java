package junwatson.mychat.service;

import com.google.gson.Gson;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.type.MemberAuthorizationType;
import junwatson.mychat.domain.type.MemberRole;
import junwatson.mychat.dto.MemberInfoDto;
import junwatson.mychat.dto.response.TokenDto;
import junwatson.mychat.exception.IllegalMemberStateException;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.repository.MemberRepository;
import junwatson.mychat.repository.dao.RefreshTokenDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Service
@Slf4j
public class GoogleLoginService {

    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final String GOOGLE_CLIENT_ID;
    private final String GOOGLE_CLIENT_SECRET;
    private final String GOOGLE_REDIRECT_URI;

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenDao refreshTokenDao;

    public GoogleLoginService(
            MemberRepository memberRepository,
            TokenProvider tokenProvider,
            @Value("${oauth.client-id}") String clientId,
            @Value("${oauth.client-secret}") String clientSecret,
            @Value("${oauth.redirect-url}") String redirectUrl,
            RefreshTokenDao refreshTokenDao) {

        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
        this.GOOGLE_CLIENT_ID = clientId;
        this.GOOGLE_CLIENT_SECRET = clientSecret;
        this.GOOGLE_REDIRECT_URI = redirectUrl;
        this.refreshTokenDao = refreshTokenDao;
    }

    public String getGoogleAccessToken(String code) {
        log.info("GoogleLoginService.getGoogleAccessToken() called");

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> params = Map.of(
                "code", code,
                "scope", "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email",
                "client_id", GOOGLE_CLIENT_ID,
                "client_secret", GOOGLE_CLIENT_SECRET,
                "redirect_uri", GOOGLE_REDIRECT_URI,
                "grant_type", "authorization_code"
        );

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(GOOGLE_TOKEN_URL, params, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String json = responseEntity.getBody();
            Gson gson = new Gson();

            return gson.fromJson(json, TokenDto.class)
                    .getAccessToken();
        }

        throw new RuntimeException("구글 엑세스 토큰을 가져오는데 실패했습니다.");
    }

    public TokenDto loginOrSignUp(String googleAccessToken) {
        log.info("GoogleLoginService.loginOrSignUp() called");

        MemberInfoDto userInfo = getUserInfo(googleAccessToken);

        // 유효성 검사
        if (!userInfo.getVerifiedEmail()) {
            throw new RuntimeException("이메일 인증이 되지 않은 유저입니다.");
        }

        // 이미 가입되어 있는 회원이라면 로그인하고, 그렇지 않다면 회원가입시킴
        Member member = memberRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .email(userInfo.getEmail())
                        .name(userInfo.getName())
                        .profileUrl(userInfo.getPictureUrl())
                        .role(MemberRole.USER)
                        .authorizedBy(MemberAuthorizationType.GOOGLE)
                        .build())
        );

        // 일반 회원가입과 중복될 수 없게 함
        if (member.getAuthorizedBy() != MemberAuthorizationType.GOOGLE) {
            throw new IllegalMemberStateException("이미 다른 경로로 회원 가입된 이메일입니다.");
        }

        return TokenDto.builder()
                .accessToken(tokenProvider.createAccessToken(member))
                .refreshToken(refreshTokenDao.createRefreshToken(member).getToken())
                .build();
    }

    private MemberInfoDto getUserInfo(String accessToken) {
        log.info("GoogleLoginService.getUserInfo() called");

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String json = responseEntity.getBody();
            Gson gson = new Gson();
            return gson.fromJson(json, MemberInfoDto.class);
        }

        throw new RuntimeException("유저 정보를 가져오는데 실패했습니다.");
    }
}