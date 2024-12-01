package junwatson.mychat.dto.request;

import junwatson.mychat.repository.condition.MemberSearchCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PUBLIC;

@Getter
@Builder
@AllArgsConstructor(access = PUBLIC)
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
