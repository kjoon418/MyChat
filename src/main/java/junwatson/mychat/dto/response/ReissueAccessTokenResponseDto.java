package junwatson.mychat.dto.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ReissueAccessTokenResponseDto {

    @SerializedName("access_token")
    private String accessToken;

    public static ReissueAccessTokenResponseDto from(String accessToken) {
        return ReissueAccessTokenResponseDto.builder()
                .accessToken(accessToken)
                .build();
    }
}