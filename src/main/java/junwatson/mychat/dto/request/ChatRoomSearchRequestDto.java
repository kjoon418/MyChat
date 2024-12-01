package junwatson.mychat.dto.request;

import junwatson.mychat.repository.condition.MemberChatRoomSearchCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.PUBLIC;

@Getter
@Builder
@AllArgsConstructor(access = PUBLIC)
public class ChatRoomSearchRequestDto {

    private String name;

    public MemberChatRoomSearchCondition toCondition() {
        return MemberChatRoomSearchCondition.builder()
                .name(name)
                .build();
    }
}
