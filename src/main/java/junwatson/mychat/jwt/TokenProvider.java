package junwatson.mychat.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import junwatson.mychat.domain.Member;
import lombok.extern.slf4j.Slf4j;
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

import static junwatson.mychat.jwt.TokenConstant.*;

@Component
@Slf4j
public class TokenProvider {

    public String createAccessToken(Member member) {
        log.info("TokenProvider.createAccessToken() called");

        long nowTime = (new Date().getTime());

        Date accessTokenExpiredTime = new Date(nowTime + ACCESS_TOKEN_VALIDITY_TIME);

        return Jwts.builder()
                .setSubject(member.getId().toString())
                .claim(ROLE_CLAIM, member.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, TokenType.ACCESS)
                .setExpiration(accessTokenExpiredTime)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        log.info("TokenProvider.getAuthentication() called");

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
        log.info("TokenProvider.resolveToken() called");

        String bearerToken = request.getHeader(AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(7);
        }

        return null;
    }

    public boolean validateToken(String token) {
        log.info("TokenProvider.validateToken() called");

        try {
            Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (UnsupportedJwtException | ExpiredJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean hasProperType(String token, TokenType tokenType) {
        log.info("TokenProvider.hasProperType() called");

        Claims claims = parseClaims(token);
        String tokenTypeClaim = (String) claims.get(TOKEN_TYPE_CLAIM);

        return tokenType == TokenType.valueOf(tokenTypeClaim);
    }

    public Claims parseClaims(String accessToken) {
        log.info("TokenProvider.parseClaims() called");

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
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