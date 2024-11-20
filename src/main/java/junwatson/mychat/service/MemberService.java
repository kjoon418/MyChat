package junwatson.mychat.service;

import jakarta.servlet.http.HttpServletRequest;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.response.ReissueAccessTokenResponseDto;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.jwt.TokenType;
import junwatson.mychat.repository.MemberRepository;
import junwatson.mychat.repository.RefreshTokenDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public ReissueAccessTokenResponseDto reissueAccessToken(HttpServletRequest request) {
        String token = memberRepository.reissueAccessToken(request);

        return ReissueAccessTokenResponseDto.from(token);
    }
}
