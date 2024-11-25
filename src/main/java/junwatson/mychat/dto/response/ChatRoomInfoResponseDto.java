package junwatson.mychat.dto.response;

import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.MemberChatRoom;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.*;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ChatRoomInfoResponseDto {

    private Long id;
    private String name;
    private String profileUrl;

    /**
     * 사용자가 본인이 지정한 채팅방의 별명, 프로필 사진 등을 제공받아야 하기 때문에, 항상 MemberChatRoom을 통해 생성하도록 함
     */
    public static ChatRoomInfoResponseDto from(MemberChatRoom memberChatRoom) {
        String name = memberChatRoom.getAliasName() == null ? memberChatRoom.getChatRoom().getName() : memberChatRoom.getAliasName();
        String profileUrl = memberChatRoom.getAliasProfileUrl() == null ? memberChatRoom.getChatRoom().getProfileUrl() : memberChatRoom.getAliasProfileUrl();

        return ChatRoomInfoResponseDto.builder()
                .id(memberChatRoom.getChatRoom().getId())
                .name(name)
                .profileUrl(profileUrl)
                .build();
    }
}
