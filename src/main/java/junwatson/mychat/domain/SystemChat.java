package junwatson.mychat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class SystemChat {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private String content;
    @Column(nullable = false)
    private LocalDateTime inputDate;

    @Builder
    private SystemChat(ChatRoom chatRoom, String content, LocalDateTime inputDate) {
        this.chatRoom = chatRoom;
        this.content = content;
        this.inputDate = inputDate;
    }
}
