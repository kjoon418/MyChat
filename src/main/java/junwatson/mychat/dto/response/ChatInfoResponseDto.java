package junwatson.mychat.dto.response;

import junwatson.mychat.domain.Chat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static lombok.AccessLevel.*;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ChatInfoResponseDto {

    private Long id;
    private String content;
    private LocalDateTime input_date;
    private int unconfirmedCounter;

    public static ChatInfoResponseDto of(Chat chat, int unconfirmedCounter) {
        return ChatInfoResponseDto.builder()
                .id(chat.getId())
                .content(chat.getContent())
                .input_date(chat.getInput_date())
                .unconfirmedCounter(unconfirmedCounter)
                .build();
    }
}
