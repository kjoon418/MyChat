package junwatson.mychat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class ChatInfoRequestDto {

    private Long chatRoomId;
    private Long chatId;

}
