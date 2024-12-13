package junwatson.mychat.dto.request;

import junwatson.mychat.domain.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PUBLIC;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
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
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Friend {
        private String email;
    }
}