package junwatson.mychat.dto.request;

import junwatson.mychat.repository.condition.MemberSearchCondition;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class SearchMemberRequestDto {

    private String name;
    private String email;

    public MemberSearchCondition toCondition() {
        return MemberSearchCondition.builder()
                .name(name)
                .email(email)
                .build();
    }
}
