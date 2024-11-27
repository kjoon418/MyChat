package junwatson.mychat.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(uniqueConstraints = {@UniqueConstraint(name = "couple_of_member_unique", columnNames = {"member_id", "target_id"})})
public class Blacklist {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(nullable = false, name = "target_id")
    private Member targetMember;

    @Builder
    private Blacklist(Member member, Member targetMember) {
        this.member = member;
        this.targetMember = targetMember;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Blacklist blacklist = (Blacklist) o;
        return Objects.equals(id, blacklist.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
