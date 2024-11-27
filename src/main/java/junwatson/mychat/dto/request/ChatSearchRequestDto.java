package junwatson.mychat.dto.request;

import junwatson.mychat.repository.condition.ChatSearchCondition;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class ChatSearchRequestDto {

    private Long id;
    private String content;

    public ChatSearchCondition toCondition() {
        return ChatSearchCondition.builder()
                .content(content)
                .build();
    }
}
