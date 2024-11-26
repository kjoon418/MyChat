package junwatson.mychat.dto.request;

import junwatson.mychat.domain.Chat;
import junwatson.mychat.domain.MemberChatRoom;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatCreateRequestDto {

    private Long chatRoomId;
    private String content;

    public Chat toEntityWithMemberChatRoom(MemberChatRoom memberChatRoom) {
        return Chat.builder()
                .memberChatRoom(memberChatRoom)
                .content(content)
                .input_date(LocalDateTime.now())
                .build();
    }
}
