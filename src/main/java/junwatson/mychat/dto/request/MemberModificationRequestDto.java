package junwatson.mychat.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.*;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class MemberModificationRequestDto {

    private String email;
    private String name;
    private String password;
    private String profileUrl;

}
