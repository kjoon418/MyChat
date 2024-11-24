package junwatson.mychat.dto.request;

import junwatson.mychat.domain.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.PRIVATE;

@Getter
public class ChatRoomInfoRequestDto {
    private Long id;
}
