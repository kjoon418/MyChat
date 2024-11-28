package junwatson.mychat.repository.condition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MemberChatRoomSearchCondition {

    private String name;

    public static MemberChatRoomSearchCondition noCondition() {
        return MemberChatRoomSearchCondition.builder()
                .name(null)
                .build();
    }
}
