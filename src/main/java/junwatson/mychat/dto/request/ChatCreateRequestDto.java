package junwatson.mychat.dto.request;

import junwatson.mychat.domain.Chat;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.type.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PUBLIC;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
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
