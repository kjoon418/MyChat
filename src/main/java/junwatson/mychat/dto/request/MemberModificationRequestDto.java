package junwatson.mychat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class MemberModificationRequestDto {

    private String email;
    private String name;
    private String password;
    private String profileUrl;

}
