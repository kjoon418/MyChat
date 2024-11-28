package junwatson.mychat.dto.request;

import junwatson.mychat.domain.ChatRoom;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class ChatRoomCreateRequestDto {

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