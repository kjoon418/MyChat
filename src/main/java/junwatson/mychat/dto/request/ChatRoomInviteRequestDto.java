package junwatson.mychat.dto.request;

import junwatson.mychat.domain.ChatRoom;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ChatRoomInviteRequestDto {

    private Long id;
    private final List<Friend> friends = new ArrayList<>();

    @Getter
    public static class Friend {
        private String email;
    }
}