package junwatson.mychat.dto.request;

import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.type.MemberRole;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.*;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class MemberSignUpRequestDto {

    private String email;

    private String name;

    private String password;

    private String profileUrl;

    public Member toEntity() {
        return Member.builder()
                .email(email)
                .name(name)
                .password(password)
                .profileUrl(profileUrl)
                .role(MemberRole.USER)
                .build();
    }
}
