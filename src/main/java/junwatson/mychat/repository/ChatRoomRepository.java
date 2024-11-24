package junwatson.mychat.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import junwatson.mychat.domain.Chat;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import junwatson.mychat.exception.ChatRoomNotExistsException;
import junwatson.mychat.repository.dao.MemberChatRoomDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junwatson.mychat.domain.QChatRoom.*;

@Repository
@Slf4j
public class ChatRoomRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;
    private final MemberChatRoomDao memberChatRoomDao;

    public ChatRoomRepository(EntityManager em, MemberChatRoomDao memberChatRoomDao) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
        this.memberChatRoomDao = memberChatRoomDao;
    }

    public ChatRoom save(ChatRoom chatRoom) {
        em.persist(chatRoom);

        return chatRoom;
    }

    public ChatRoom remove(ChatRoom chatRoom) {
        em.remove(chatRoom);

        return chatRoom;
    }

    public Optional<ChatRoom> findById(Long id) {
        return Optional.ofNullable(em.find(ChatRoom.class, id));
    }

    public List<ChatRoom> findByName(String name) {
        return query.select(chatRoom)
                .from(chatRoom)
                .where(nameLike(name))
                .fetch();
    }

    public MemberChatRoom createMemberChatRoom(Member member, ChatRoom chatRoom) {
        return memberChatRoomDao.createMemberChatRoom(member, chatRoom);
    }

    public void leaveChatRoom(Member member, ChatRoom chatRoom) {
        MemberChatRoom memberChatRoom = memberChatRoomDao.findByMemberAndChatRoom(member, chatRoom)
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방에 소속되지 않았습니다."));

        member.getMemberChatRooms()
                .remove(memberChatRoom);
        chatRoom.getMemberChatRooms()
                .remove(memberChatRoom);

        if (isEmptyChatRoom(chatRoom)) {
            em.remove(chatRoom);
        }
    }

    public void leaveAllChatRooms(Member member) {
        List<MemberChatRoom> removeChatRooms = new ArrayList<>(member.getMemberChatRooms());

        for (MemberChatRoom memberChatRoom : removeChatRooms) {
            leaveChatRoom(member, memberChatRoom.getChatRoom());
        }
    }

    private boolean isEmptyChatRoom(ChatRoom chatRoom) {
        return chatRoom.getMemberChatRooms().isEmpty();
    }

    private BooleanExpression nameLike(String name) {
        return chatRoom.name.like("%" + name + "%");
    }
}
