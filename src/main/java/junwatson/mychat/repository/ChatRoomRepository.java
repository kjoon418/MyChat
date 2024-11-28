package junwatson.mychat.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import junwatson.mychat.repository.condition.MemberChatRoomSearchCondition;
import junwatson.mychat.repository.dao.MemberChatRoomDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junwatson.mychat.domain.QChatRoom.chatRoom;

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
        log.info("ChatRoomRepository.save() called");

        em.persist(chatRoom);

        return chatRoom;
    }

    public Optional<ChatRoom> findById(Long id) {
        log.info("ChatRoomRepository.findById() called");

        return Optional.ofNullable(em.find(ChatRoom.class, id));
    }

    public void leaveChatRoom(MemberChatRoom memberChatRoom) {
        log.info("ChatRoomRepository.leaveChatRoom() called");

        // MemberChatRoom의 부모 엔티티로부터 삭제
        memberChatRoomDao.removeMemberChatRoom(memberChatRoom);

        // 채팅방이 비어 있을 경우, 해당 채팅방에 대한 데이터를 삭제
        removeChatRoomIfEmpty(memberChatRoom.getChatRoom());
    }

    public void leaveAllChatRooms(Member member) {
        log.info("ChatRoomRepository.leaveAllChatRooms() called");

        List<MemberChatRoom> removeChatRooms = new ArrayList<>(member.getMemberChatRooms());

        for (MemberChatRoom memberChatRoom : removeChatRooms) {
            leaveChatRoom(memberChatRoom);
        }
    }

    private void removeChatRoomIfEmpty(ChatRoom chatRoom) {
        log.info("ChatRoomRepository.removeChatRoomIfEmpty() called");

        if (chatRoom.getMemberChatRooms().isEmpty()) {
            em.remove(chatRoom);
        }
    }

    private BooleanExpression nameLike(String name) {
        log.info("ChatRoomRepository.nameLike() called");

        if (!StringUtils.hasText(name)) {
            return null;
        }

        return chatRoom.name.like("%" + name + "%");
    }
}
