package junwatson.mychat.dto.request;

import junwatson.mychat.domain.Chat;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.type.ChatType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class ChatCreateRequestDto {

    private Long chatRoomId;
    private String content;

    public Chat toEntityWithMemberChatRoom(Member member, ChatRoom chatRoom) {
        return Chat.builder()
                .member(member)
                .chatRoom(chatRoom)
                .content(content)
                .inputDate(LocalDateTime.now())
                .chatType(ChatType.USER)
                .build();
    }
}
