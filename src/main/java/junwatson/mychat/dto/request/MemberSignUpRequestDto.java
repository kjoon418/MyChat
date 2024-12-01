package junwatson.mychat.dto.request;

import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.type.MemberAuthorizationType;
import junwatson.mychat.domain.type.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PUBLIC;

/**
 * 다른 OAuth 를 이용하지 않고, 직접 MyChat 서비스를 통해 회원 가입하는 리퀘스트를 처리하는 DTO
 */
@Getter
@Builder
@AllArgsConstructor(access = PUBLIC)
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
                .authorizedBy(MemberAuthorizationType.JUN_WATSON)
                .build();
    }
}
