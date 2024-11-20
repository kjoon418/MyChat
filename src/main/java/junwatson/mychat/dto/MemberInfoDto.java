package junwatson.mychat.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberInfoDto {
    private String id;
    private String email;
    @SerializedName("verified_email")
    private Boolean verifiedEmail;
    private String name;
    @SerializedName("given_name")
    private String givenName;
    @SerializedName("family_name")
    private String familyName;
    @SerializedName("picture")
    private String pictureUrl;
    private String locale;
}