package junwatson.mychat.dto.response;

import junwatson.mychat.domain.UserChat;
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
    private LocalDateTime inputDate;
    private int unconfirmedCounter;

    public static ChatInfoResponseDto of(UserChat userChat, int unconfirmedCounter) {
        return ChatInfoResponseDto.builder()
                .id(userChat.getId())
                .content(userChat.getContent())
                .inputDate(userChat.getInputDate())
                .unconfirmedCounter(unconfirmedCounter)
                .build();
    }
}
