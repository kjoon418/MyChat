package junwatson.mychat.dto.request;

import junwatson.mychat.repository.condition.MemberChatRoomSearchCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import static lombok.AccessLevel.PUBLIC;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ChatRoomSearchRequestDto {

    private String name;

    public MemberChatRoomSearchCondition toCondition() {
        String name = StringUtils.hasText(this.name) ? this.name : "";

        return MemberChatRoomSearchCondition.builder()
                .name(name)
                .build();
    }
}
