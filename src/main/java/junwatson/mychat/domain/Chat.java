package junwatson.mychat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class Chat {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = LAZY)
    private Member member;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = LAZY)
    private ChatRoom chatRoom;

    private String content;
    private LocalDateTime input_date;

    @Builder
    private Chat(Member member, ChatRoom chatRoom, String content, LocalDateTime input_date) {
        this.member = member;
        this.chatRoom = chatRoom;
        this.content = content;
        this.input_date = input_date;
    }
}
