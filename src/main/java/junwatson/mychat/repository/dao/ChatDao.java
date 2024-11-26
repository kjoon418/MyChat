package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.Chat;
import junwatson.mychat.domain.MemberChatRoom;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ChatDao {

    public Optional<Chat> findChatById(MemberChatRoom memberChatRoom, Long id) {
        return memberChatRoom.getChats().stream()
                .filter(chat -> chat.getId().equals(id))
                .findAny();
    }

    public List<Chat> findAllChats(MemberChatRoom memberChatRoom) {
        return memberChatRoom.getChats();
    }
}
