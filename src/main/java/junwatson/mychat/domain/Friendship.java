package junwatson.mychat.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@Table(uniqueConstraints = {@UniqueConstraint(name = "couple_of_member_unique", columnNames = {"member_id", "friend_id"})})
public class Friendship {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private Member friendMember;

    @Builder
    private Friendship(Member member, Member friendMember) {
        this.member = member;
        this.friendMember = friendMember;
    }
}
