package junwatson.mychat.dto.request;

import junwatson.mychat.repository.condition.ChatSearchCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PUBLIC;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ChatSearchRequestDto {

    private Long id;
    private String content;

    public ChatSearchCondition toCondition() {
        return ChatSearchCondition.builder()
                .content(content)
                .build();
    }
}
