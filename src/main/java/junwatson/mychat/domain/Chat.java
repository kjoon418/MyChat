package junwatson.mychat.domain;

import jakarta.persistence.*;
import junwatson.mychat.domain.type.ChatType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class Chat implements Comparable<Chat> {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @JoinColumn
    @ManyToOne(fetch = LAZY)
    private Member member;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = LAZY)
    private ChatRoom chatRoom;

    @Setter
    private String content;
    @Setter
    private LocalDateTime inputDate;
    @Enumerated(STRING)
    private ChatType chatType;

    @Builder
    private Chat(Member member, ChatRoom chatRoom, String content, LocalDateTime inputDate, ChatType chatType) {
        this.member = member;
        this.chatRoom = chatRoom;
        this.content = content;
        this.inputDate = inputDate;
        this.chatType = chatType;
    }

    @Override
    public int compareTo(Chat o) {
        return inputDate.compareTo(o.inputDate) * -1;
    }
}
