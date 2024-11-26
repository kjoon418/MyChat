package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.Chat;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ChatDao {

    public Optional<Chat> findChatById(Member member, Long id) {
        return member.getChats().stream()
                .filter(chat -> chat.getId().equals(id))
                .findAny();
    }

    public List<Chat> findAllChats(Member member) {
        return member.getChats();
    }
}
