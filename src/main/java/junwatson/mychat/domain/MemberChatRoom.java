package junwatson.mychat.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(uniqueConstraints = {@UniqueConstraint(name = "member_chat_room_unique", columnNames = {"member_id", "chat_room_id"})})
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

    private LocalDateTime viewDate;
    @Setter
    private String aliasName;
    @Setter
    private String aliasProfileUrl;

    @Builder
    private MemberChatRoom(Member member, ChatRoom chatRoom) {
        this.member = member;
        this.chatRoom = chatRoom;
        this.viewDate = LocalDateTime.now();
    }

    /**
     * 채팅방 조회 시각을 현재로 설정하는 메서드
     */
    public void setViewDateToNow() {
        this.viewDate = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberChatRoom that = (MemberChatRoom) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
