package junwatson.mychat.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String profileUrl;

    @OneToMany(mappedBy = "chatRoom", cascade = ALL, orphanRemoval = true)
    private final List<MemberChatRoom> memberChatRooms = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = ALL, orphanRemoval = true)
    private final List<Chat> chats = new ArrayList<>();

    @Builder
    private ChatRoom(String name, String profileUrl) {
        this.name = name;
        this.profileUrl = profileUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return Objects.equals(id, chatRoom.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
