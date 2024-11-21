package junwatson.mychat.repository.condition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.*;

@Builder
@AllArgsConstructor(access = PRIVATE)
@Getter
public class MemberSearchCondition {

    private String name;
    private String email;

    public static MemberSearchCondition noCondition() {
        return MemberSearchCondition.builder().build();
    }
}
