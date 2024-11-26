package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.UserChat;
import junwatson.mychat.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ChatDao {

    public Optional<UserChat> findChatById(Member member, Long id) {
        return member.getUserChats().stream()
                .filter(chat -> chat.getId().equals(id))
                .findAny();
    }

    public List<UserChat> findAllChats(Member member) {
        return member.getUserChats();
    }
}
