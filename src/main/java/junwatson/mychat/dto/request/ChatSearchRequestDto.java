package junwatson.mychat.dto.request;

import junwatson.mychat.repository.condition.ChatSearchCondition;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class ChatSearchRequestDto {

    private Long id;
    private String content;

    public ChatSearchCondition toCondition() {
        return ChatSearchCondition.builder()
                .content(content)
                .build();
    }
}
