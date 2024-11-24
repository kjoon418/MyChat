package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import org.springframework.stereotype.Repository;

@Repository
public class MemberChatRoomDao {

    public MemberChatRoom createMemberChatRoom(Member member, ChatRoom chatRoom) {
        MemberChatRoom memberChatRoom = MemberChatRoom.builder()
                .member(member)
                .chatRoom(chatRoom)
                .build();

        member.getMemberChatRooms().add(memberChatRoom);
        chatRoom.getMemberChatRooms().add(memberChatRoom);

        return memberChatRoom;
    }
}
