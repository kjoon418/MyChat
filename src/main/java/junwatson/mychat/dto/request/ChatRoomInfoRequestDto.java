package junwatson.mychat.dto.request;

import junwatson.mychat.domain.ChatRoom;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ChatRoomInfoRequestDto {

    private String name;
    private final List<Friend> friends = new ArrayList<>();

    public ChatRoom toEntity() {
        return ChatRoom.builder()
                .name(name)
                .build();
    }

    @Getter
    public static class Friend {

        private String email;
    }
}