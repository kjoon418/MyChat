package junwatson.mychat.repository.condition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.*;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ChatSearchCondition {
    private String content;

    public static ChatSearchCondition noCondition() {
        return ChatSearchCondition.builder()
                .content(null)
                .build();
    }
}