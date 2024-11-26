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
    @ManyToOne
    private MemberChatRoom memberChatRoom;

    private String content;
    private LocalDateTime input_date;

    @Builder
    private Chat(MemberChatRoom memberChatRoom, String content, LocalDateTime input_date) {
        this.memberChatRoom = memberChatRoom;
        this.content = content;
        this.input_date = input_date;
    }
}
