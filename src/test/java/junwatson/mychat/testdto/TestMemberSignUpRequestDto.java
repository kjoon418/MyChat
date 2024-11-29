package junwatson.mychat.testdto;

import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.type.MemberAuthorizationType;
import junwatson.mychat.domain.type.MemberRole;
import junwatson.mychat.dto.request.MemberSignUpRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TestMemberSignUpRequestDto extends MemberSignUpRequestDto {

    private String email;
    private String name;
    private String password;
    private String profileUrl;

    @Override
    public Member toEntity() {
        return Member.builder()
                .email(email)
                .name(name)
                .password(password)
                .profileUrl(profileUrl)
                .role(MemberRole.USER)
                .authorizedBy(MemberAuthorizationType.JUN_WATSON)
                .build();
    }
}
