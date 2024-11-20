package junwatson.mychat.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import junwatson.mychat.domain.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    private static final String ROLE_CLAIM = "Role";
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";

    private final Key key;
    private final long accessTokenValidityTime;

    public TokenProvider(@Value("${jwt.secret}") String secretKey,
                         @Value("${jwt.access-token-validity-in-milliseconds}") long accessTokenValidityTime) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityTime = accessTokenValidityTime;
    }

    public String createAccessToken(Member member) {
        long nowTime = (new Date().getTime());

        Date accessTokenExpiredTime = new Date(nowTime + accessTokenValidityTime);

        return Jwts.builder()
                .setSubject(member.getId().toString())
                .claim(ROLE_CLAIM, member.getRole().name())
                .setExpiration(accessTokenExpiredTime)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get(ROLE_CLAIM) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 사용자의 권한 정보를 securityContextHolder에 담아준다
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(ROLE_CLAIM).toString().split(","))
                // 해당 hasRole이 권한 정보를 식별하기 위한 전처리 작업
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
        authentication.setDetails(claims);

        return authentication;
    }

    public String resolveToken(HttpServletRequest request) { //토큰 분해/분석
        String bearerToken = request.getHeader(AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(7);
        }

        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (UnsupportedJwtException | ExpiredJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (SignatureException e) {
            throw new RuntimeException("토큰 복호화에 실패했습니다.");
        }
    }
}