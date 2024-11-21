package junwatson.mychat.jwt;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@Getter
public class TokenConstant {

    public static final String ROLE_CLAIM = "Role";
    public static final String TOKEN_TYPE_CLAIM = "JunWatson/MyChat/TokenType";
    public static final String BEARER = "Bearer ";
    public static final String AUTHORIZATION = "Authorization";

    public static Key KEY;
    public static long ACCESS_TOKEN_VALIDITY_TIME;

    public TokenConstant(@Value("${jwt.secret}") String secretKey,
                           @Value("${jwt.access-token-validity-in-milliseconds}") long accessTokenValidityTime) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        TokenConstant.KEY = Keys.hmacShaKeyFor(keyBytes);
        TokenConstant.ACCESS_TOKEN_VALIDITY_TIME = accessTokenValidityTime;
    }
}
