package junwatson.mychat.dto.response;

import junwatson.mychat.domain.Chat;
import junwatson.mychat.domain.type.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ChatInfoResponseDto {

    private Long chatId;
    private Long memberId;
    private String content;
    private LocalDateTime inputDate;
    private int unconfirmedCounter;
    private ChatType chatType;

    public static ChatInfoResponseDto of(Chat chat, int unconfirmedCounter) {
        return ChatInfoResponseDto.builder()
                .chatId(chat.getId())
                .memberId(chat.getMember().getId())
                .content(chat.getContent())
                .inputDate(chat.getInputDate())
                .chatType(chat.getChatType())
                .unconfirmedCounter(unconfirmedCounter)
                .build();
    }
}
