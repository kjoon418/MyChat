package junwatson.mychat.repository;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import junwatson.mychat.domain.Friendship;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.RefreshToken;
import junwatson.mychat.exception.IllegalRefreshTokenException;
import junwatson.mychat.exception.MemberNotExistsException;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.repository.condition.MemberSearchCondition;
import junwatson.mychat.repository.dao.FriendshipDao;
import junwatson.mychat.repository.dao.FriendshipRequestDao;
import junwatson.mychat.repository.dao.RefreshTokenDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberRepository {

    private final EntityManager em;
    private final TokenProvider tokenProvider;
    private final RefreshTokenDao refreshTokenDao;
    private final FriendshipRequestDao friendshipRequestDao;
    private final FriendshipDao friendshipDao;

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

    public String reissueAccessToken(HttpServletRequest request) {
        String token = tokenProvider.resolveToken(request);

        Member member = findById(Long.parseLong(tokenProvider.parseClaims(token).getSubject()))
                .orElseThrow(() -> new MemberNotExistsException("해당 토큰으로 회원을 조회할 수 없습니다."));

        if (!refreshTokenDao.isValidateRefreshToken(member, token)) {
            throw new IllegalRefreshTokenException("부적절한 리프레시 토큰입니다.");
        }

        return tokenProvider.createAccessToken(member);
    }

    public boolean isExistFriendshipRequest(Member member, Member friend) {
        return friendshipRequestDao.isRequestExists(member, friend);
    }

    public void createFriendshipRequest(Member member, Member friend) {
        friendshipRequestDao.createFriendshipRequest(member, friend);
    }

    public void removeFriendshipRequest(Member member, Member friend) {
        friendshipRequestDao.removeRequest(member, friend);
    }

    public void createFriendship(Member member, Member friend) {
        friendshipDao.createFriendship(member, friend);
    }

    public List<Friendship> searchFriendship(Member member, MemberSearchCondition condition) {
        return friendshipDao.searchFriendships(member, condition);
    }
}
