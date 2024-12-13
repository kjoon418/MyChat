package junwatson.mychat.dto.response;

import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

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

        String profileUrl = memberChatRoom.getAliasProfileUrl() == null ? memberChatRoom.getChatRoom().getProfileUrl() : memberChatRoom.getAliasProfileUrl();
        String name = memberChatRoom.getAliasName();

        // 채팅방 이름이 없을 경우, 기본 채팅방 이름을 사용
        if (!StringUtils.hasText(name)) {
            name = memberChatRoom.getChatRoom().getName();
        }
        // 기본 채팅방 이름도 없을 경우, 방 멤버들의 이름을 사용
        if (!StringUtils.hasText(name)) {
            StringBuilder chatRoomNameBuilder = new StringBuilder();

            ArrayList<Member> members = new ArrayList<>(memberChatRoom.getChatRoom()
                    .getMemberChatRooms().stream()
                    .map(MemberChatRoom::getMember)
                    .filter(member -> !member.equals(memberChatRoom.getMember()))
                    .toList());
            if (members.isEmpty()) { // 만약 방에 다른 멤버가 아무도 없다면, 본인 이름을 사용
                members.add(memberChatRoom.getMember());
            }

            for (Member member : members) {
                chatRoomNameBuilder.append(member.getName()).append(", ");
            }
            chatRoomNameBuilder.delete(chatRoomNameBuilder.length() - 2, chatRoomNameBuilder.length());

            name = chatRoomNameBuilder.toString();
        }

        return ChatRoomInfoResponseDto.builder()
                .id(memberChatRoom.getChatRoom().getId())
                .name(name)
                .profileUrl(profileUrl)
                .build();
    }
}
