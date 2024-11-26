package junwatson.mychat.dto.request;

import junwatson.mychat.domain.Chat;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatCreateRequestDto {

    private Long chatRoomId;
    private String content;

    public Chat toEntityWithMemberChatRoom(Member member, ChatRoom chatRoom) {
        return Chat.builder()
                .member(member)
                .chatRoom(chatRoom)
                .content(content)
                .input_date(LocalDateTime.now())
                .build();
    }
}
