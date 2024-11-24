package junwatson.mychat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
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
    private final List<SystemChat> systemChats = new ArrayList<>();

    @Builder
    private ChatRoom(String name, String profileUrl) {
        this.name = name;
        this.profileUrl = profileUrl;
    }
}
