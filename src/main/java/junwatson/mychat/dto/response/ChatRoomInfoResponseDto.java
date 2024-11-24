package junwatson.mychat.dto.response;

import junwatson.mychat.domain.ChatRoom;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.*;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ChatRoomInfoResponseDto {

    private Long id;
    private String name;
    private String profileUrl;

    public static ChatRoomInfoResponseDto from(ChatRoom chatRoom) {
        return ChatRoomInfoResponseDto.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .profileUrl(chatRoom.getProfileUrl())
                .build();
    }
}
