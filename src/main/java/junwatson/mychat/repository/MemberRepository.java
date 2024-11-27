package junwatson.mychat.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import junwatson.mychat.domain.Member;
import junwatson.mychat.repository.condition.MemberSearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static junwatson.mychat.domain.QMember.member;

@Repository
@Transactional
@Slf4j
public class MemberRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    public MemberRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    public Optional<Member> findByEmail(String email) {
        log.info("MemberRepository.findByEmail() called");

        String query = "select m from Member m where m.email=:email";

        return em.createQuery(query, Member.class)
                .setParameter("email", email)
                .getResultList()
                .stream()
                .findAny();
    }

    public Optional<Member> findById(Long id) {
        log.info("MemberRepository.findById() called");

        return Optional.ofNullable(em.find(Member.class, id));
    }

    public Member save(Member member) {
        log.info("MemberRepository.save() called");

        em.persist(member);

        return member;
    }

    public Member delete(Member member) {
        log.info("MemberRepository.delete() called");

        em.flush();
        em.remove(member);

        return member;
    }

    public void updateEmail(Member member, String email) {
        log.info("MemberRepository.updateEmail() called");

        member.setEmail(email);
    }

    public void updatePassword(Member member, String password) {
        log.info("MemberRepository.updatePassword() called");

        member.setPassword(password);
    }

    public void updateName(Member member, String name) {
        log.info("MemberRepository.updateName() called");

        member.setName(name);
    }

    public void updateProfileUrl(Member member, String profileUrl) {
        log.info("MemberRepository.updateProfileUrl() called");

        member.setProfileUrl(profileUrl);
    }

    public List<Member> searchMembers(Member requestMember, MemberSearchCondition condition) {
        log.info("MemberRepository.searchMember() called");

        String email = condition.getEmail();
        String name = condition.getName();
        Long id = requestMember.getId();

        return query.select(member)
                .from(member)
                .where(likeEmail(email), likeName(name), differentId(id))
                .fetch();
    }

    private BooleanExpression likeName(String name) {
        log.info("MemberRepository.likeName() called");

        if (!StringUtils.hasText(name)) {
            return null;
        }

        return member.name.like("%" + name + "%");
    }

    private BooleanExpression likeEmail(String email) {
        log.info("MemberRepository.likeEmail() called");

        if (!StringUtils.hasText(email)) {
            return null;
        }

        return member.email.like("%" + email + "%");
    }

    private BooleanExpression differentId(Long id) {
        log.info("MemberRepository.differentId() called");

        if (id == null) {
            return null;
        }

        return member.id.eq(id).not();
    }
}
