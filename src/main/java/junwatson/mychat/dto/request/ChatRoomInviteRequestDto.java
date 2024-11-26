package junwatson.mychat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class ChatRoomInviteRequestDto {

    private Long id;
    private final List<Friend> friends = new ArrayList<>();

    @Getter
    @NoArgsConstructor(access = PRIVATE)
    public static class Friend {
        private String email;
    }
}