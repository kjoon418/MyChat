package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.Chat;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.type.ChatType;
import junwatson.mychat.repository.condition.ChatSearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
@Slf4j
public class ChatDao {

    public Optional<Chat> findChatById(Member member, Long id) {
        log.info("ChatDao.findChatById() called");

        return member.getChats().stream()
                .filter(chat -> chat.getId().equals(id))
                .findAny();
    }

    public void remove(Chat chat) {
        log.info("ChatDao.remove() called");

        Member member = chat.getMember();
        ChatRoom chatRoom = chat.getChatRoom();

        member.getChats().remove(chat);
        chatRoom.getChats().remove(chat);
    }

    public List<Chat> searchByCondition(ChatRoom chatRoom, ChatSearchCondition condition) {
        log.info("ChatDao.searchByCondition() called");

        String content = condition.getContent();
        Stream<Chat> stream = chatRoom.getChats().stream();
        if (StringUtils.hasText(content)) {
            stream = stream.filter(chat -> chat.getContent().contains(content));
        }

        return stream.sorted()
                .toList();
    }

    /**
     * 해당 문자열을 담은 시스템 채팅을 생성하는 메서드
     */
    public void createSystemChat(ChatRoom chatRoom, String message) {
        log.info("ChatDao.createSystemChat() called");

        Chat systemChat = Chat.builder()
                .chatRoom(chatRoom)
                .content(message)
                .inputDate(LocalDateTime.now())
                .chatType(ChatType.SYSTEM)
                .build();

        chatRoom.getChats().add(systemChat);
    }

    /**
     * 해당 멤버의 모든 채팅을 (알수없음)으로 변경하는 메서드
     */
    public void removeAllChats(Member member) {
        log.info("ChatDao.removeAllChats() called");

        List<Chat> chats = member.getChats();
        for (Chat chat : chats) {
            chat.setMember(null);
        }
    }
}
