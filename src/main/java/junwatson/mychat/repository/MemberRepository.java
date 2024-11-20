package junwatson.mychat.repository;

import jakarta.persistence.EntityManager;
import junwatson.mychat.domain.Member;
import junwatson.mychat.exception.MemberNotExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public Optional<Member> findByEmail(String email) {
        String query = "select m from Member m where m.email=:email";

        return em.createQuery(query, Member.class)
                .getResultList()
                .stream()
                .findAny();
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(em.find(Member.class, id));
    }

    public Member save(Member member) {
        em.persist(member);

        return member;
    }
}
