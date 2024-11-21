package junwatson.mychat.repository.dao;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.RefreshToken;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.jwt.TokenType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.security.Key;
import java.util.Date;

import static junwatson.mychat.jwt.TokenConstant.*;

@Repository
@Slf4j
public class RefreshTokenDao {

    public RefreshToken createRefreshToken(Member member) {
        String refreshTokenString = createRefreshTokenString(member);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .build();
        member.setRefreshToken(refreshToken);

        return refreshToken;
    }

    public boolean isValidateRefreshToken(Member member, String token) {
        if (member == null || member.getRefreshToken() == null) {
            return false;
        }

        return member.getRefreshToken()
                .getToken()
                .equals(token);
    }

    private String createRefreshTokenString(Member member) {
        log.info("TokenProvider.createRefreshToken() called");

        long nowTime = (new Date().getTime());

        Date accessTokenExpiredTime = new Date(nowTime + (ACCESS_TOKEN_VALIDITY_TIME * 24));

        return Jwts.builder()
                .setSubject(member.getId().toString())
                .claim(ROLE_CLAIM, member.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, TokenType.REFRESH)
                .setExpiration(accessTokenExpiredTime)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }
}
