package junwatson.mychat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@Table(uniqueConstraints = {@UniqueConstraint(name = "couple_of_member_unique", columnNames = {"request_member_id", "response_member_id"})})
public class FriendshipRequest {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Member requestMember;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Member responseMember;

    @Builder
    private FriendshipRequest(Member requestMember, Member responseMember) {
        this.requestMember = requestMember;
        this.responseMember = responseMember;
    }
}
