package junwatson.mychat.repository;

import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.RefreshToken;
import junwatson.mychat.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenDao {

    private final TokenProvider tokenProvider;

    public RefreshToken createRefreshToken(Member member) {
        String refreshTokenString = tokenProvider.createRefreshToken(member);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .build();
        member.setRefreshToken(refreshToken);

        return refreshToken;
    }
}