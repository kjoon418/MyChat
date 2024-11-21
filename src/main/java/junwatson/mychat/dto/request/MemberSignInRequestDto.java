package junwatson.mychat.dto.request;

import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.type.MemberRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
@Getter
public class MemberSignInRequestDto {

    private String email;

    private String password;

}
