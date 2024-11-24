package junwatson.mychat.dto.request;

import junwatson.mychat.domain.ChatRoom;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ChatRoomInfoRequestDto {

    private String name;
    private String profileUrl;
    private final List<Friend> friends = new ArrayList<>();

    public ChatRoom toEntity() {
        return ChatRoom.builder()
                .name(name)
                .profileUrl(profileUrl)
                .build();
    }

    @Getter
    public static class Friend {
        private String email;
    }
}