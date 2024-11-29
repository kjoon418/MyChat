package junwatson.mychat.testdto;

import junwatson.mychat.dto.request.MemberSignInRequestDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.*;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class TestMemberSignInRequestDto extends MemberSignInRequestDto {

    private String email;
    private String password;

}
