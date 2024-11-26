package junwatson.mychat.dto.request;

import junwatson.mychat.repository.condition.ChatSearchCondition;
import lombok.Getter;

@Getter
public class ChatSearchRequestDto {

    private Long id;
    private String content;

    public ChatSearchCondition toCondition() {
        return ChatSearchCondition.builder()
                .content(content)
                .build();
    }
}
