package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import junwatson.mychat.repository.condition.MemberChatRoomSearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class MemberChatRoomDao {

    public MemberChatRoom createMemberChatRoom(Member member, ChatRoom chatRoom) {
        log.info("MemberChatRoomDao.createMemberChatRoom() called");

        MemberChatRoom memberChatRoom = MemberChatRoom.builder()
                .member(member)
                .chatRoom(chatRoom)
                .build();

        member.getMemberChatRooms().add(memberChatRoom);
        chatRoom.getMemberChatRooms().add(memberChatRoom);

        return memberChatRoom;
    }

    public Optional<MemberChatRoom> findByMemberAndChatRoom(Member member, ChatRoom chatRoom) {
        log.info("MemberChatRoomDao.findByMemberAndChatRoom() called");

        return member.getMemberChatRooms().stream()
                .filter(memberChatRoom -> memberChatRoom.getChatRoom().equals(chatRoom))
                .findAny();
    }

    public void removeMemberChatRoom(MemberChatRoom memberChatRoom) {
        log.info("MemberChatRoomDao.removeMemberChatRoom() called");

        Member member = memberChatRoom.getMember();
        ChatRoom chatRoom = memberChatRoom.getChatRoom();

        member.getMemberChatRooms()
                .remove(memberChatRoom);
        chatRoom.getMemberChatRooms()
                .remove(memberChatRoom);
    }

    public List<MemberChatRoom> filterWithCondition(List<MemberChatRoom> memberChatRooms, MemberChatRoomSearchCondition condition) {
        log.info("MemberChatRoomDao.filterWithCondition() called");

        return memberChatRooms.stream()
                .filter(memberChatRoom -> {
                    if (StringUtils.hasText(memberChatRoom.getAliasName())) {
                        return memberChatRoom.getAliasName().contains(condition.getName());
                    }
                    return memberChatRoom.getChatRoom()
                            .getName()
                            .contains(condition.getName());
                })
                .toList();
    }
}
