package junwatson.mychat.dto.request;

import junwatson.mychat.domain.UserChat;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatCreateRequestDto {

    private Long chatRoomId;
    private String content;

    public UserChat toEntityWithMemberChatRoom(Member member, ChatRoom chatRoom) {
        return UserChat.builder()
                .member(member)
                .chatRoom(chatRoom)
                .content(content)
                .inputDate(LocalDateTime.now())
                .build();
    }
}
