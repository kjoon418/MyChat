package junwatson.mychat.repository;

import jakarta.persistence.EntityManager;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberRepository {

    private final EntityManager em;
    private final RefreshTokenDao refreshTokenDao;

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

    public String createRefreshToken(Member member) {
        RefreshToken refreshToken = refreshTokenDao.createRefreshToken(member);

        return refreshToken.getToken();
    }
}
