package junwatson.mychat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
public class MemberChatRoom {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(nullable = false)
    private ChatRoom chatRoom;

    @OneToMany(fetch = LAZY, orphanRemoval = true, cascade = ALL)
    private List<Chat> chats = new ArrayList<>();

    private LocalDateTime view_date;
}
