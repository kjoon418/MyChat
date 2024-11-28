package junwatson.mychat.dto.request;

import junwatson.mychat.repository.condition.MemberChatRoomSearchCondition;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class ChatRoomSearchRequestDto {

    private String name;

    public MemberChatRoomSearchCondition toCondition() {
        return MemberChatRoomSearchCondition.builder()
                .name(name)
                .build();
    }
}
