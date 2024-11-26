package junwatson.mychat.dto.response;

import junwatson.mychat.domain.Chat;
import lombok.AccessLevel;
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

    public static ChatInfoResponseDto from(Chat chat) {
        return ChatInfoResponseDto.builder()
                .id(chat.getId())
                .content(chat.getContent())
                .input_date(chat.getInput_date())
                .build();
    }
}
