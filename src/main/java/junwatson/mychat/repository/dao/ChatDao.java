package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.Chat;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.repository.condition.ChatSearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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

    public List<Chat> findAllChats(Member member) {
        log.info("ChatDao.findAllChats() called");

        return member.getChats();
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
}
