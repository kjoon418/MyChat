package junwatson.mychat.dto.request;

import junwatson.mychat.repository.condition.MemberSearchCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
@Getter
public class SearchFriendsRequestDto {

    private String name;
    private String email;

    public MemberSearchCondition toCondition() {
        return MemberSearchCondition.builder()
                .name(name)
                .email(email)
                .build();
    }
}