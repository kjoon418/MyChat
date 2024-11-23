package junwatson.mychat.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import junwatson.mychat.domain.*;
import junwatson.mychat.exception.IllegalRefreshTokenException;
import junwatson.mychat.exception.MemberNotExistsException;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.repository.condition.MemberSearchCondition;
import junwatson.mychat.repository.dao.BlacklistDao;
import junwatson.mychat.repository.dao.FriendshipDao;
import junwatson.mychat.repository.dao.FriendshipRequestDao;
import junwatson.mychat.repository.dao.RefreshTokenDao;
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
    private final TokenProvider tokenProvider;
    private final JPAQueryFactory query;
    private final RefreshTokenDao refreshTokenDao;
    private final FriendshipRequestDao friendshipRequestDao;
    private final FriendshipDao friendshipDao;
    private final BlacklistDao blacklistDao;

    public MemberRepository(EntityManager em, TokenProvider tokenProvider, RefreshTokenDao refreshTokenDao, FriendshipRequestDao friendshipRequestDao, FriendshipDao friendshipDao, BlacklistDao blacklistDao) {
        this.em = em;
        this.tokenProvider = tokenProvider;
        this.query = new JPAQueryFactory(em);
        this.refreshTokenDao = refreshTokenDao;
        this.friendshipRequestDao = friendshipRequestDao;
        this.friendshipDao = friendshipDao;
        this.blacklistDao = blacklistDao;
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

    public Member updateEmail(Member member, String email) {
        log.info("MemberRepository.updateEmail() called");

        member.setEmail(email);

        return member;
    }

    public Member updatePassword(Member member, String password) {
        log.info("MemberRepository.updatePassword() called");

        member.setPassword(password);

        return member;
    }

    public Member updateName(Member member, String name) {
        log.info("MemberRepository.updateName() called");

        member.setName(name);

        return member;
    }

    public Member updateProfileUrl(Member member, String profileUrl) {
        log.info("MemberRepository.updateProfileUrl() called");

        member.setProfileUrl(profileUrl);

        return member;
    }

    public String createRefreshToken(Member member) {
        log.info("MemberRepository.createRefreshToken() called");

        RefreshToken refreshToken = refreshTokenDao.createRefreshToken(member);

        return refreshToken.getToken();
    }

    public String reissueAccessToken(HttpServletRequest request) {
        log.info("MemberRepository.reissueAccessToken() called");

        String token = tokenProvider.resolveToken(request);

        Member member = findById(Long.parseLong(tokenProvider.parseClaims(token).getSubject()))
                .orElseThrow(() -> new MemberNotExistsException("해당 토큰으로 회원을 조회할 수 없습니다."));

        if (!refreshTokenDao.isValidateRefreshToken(member, token)) {
            throw new IllegalRefreshTokenException("부적절한 리프레시 토큰입니다.");
        }

        return tokenProvider.createAccessToken(member);
    }

    public boolean isReceivedFriendshipRequestExists(Member member, Member friend) {
        log.info("MemberRepository.isReceivedFriendshipRequestExists() called");

        return friendshipRequestDao.isReceivedFriendshipRequestExists(member, friend);
    }

    public boolean isSentFriendshipRequestExists(Member member, Member friend) {
        log.info("MemberRepository.isSentFriendshipRequestExists() called");

        return friendshipRequestDao.isSentFriendshipRequestExists(member, friend);
    }

    public boolean isBlocked(Member member, Member target) {
        log.info("MemberRepository.isBlocked() called");

        return blacklistDao.isBlocked(member, target);
    }

    public boolean isBlacklistExists(Member member, Member target) {
        log.info("MemberRepository.isBlacklistExists() called");

        return blacklistDao.isBlacklistExists(member, target);
    }

    public List<FriendshipRequest> findSentFriendshipRequests(Member member) {
        log.info("MemberRepository.findSentFriendshipRequest() called");

        return friendshipRequestDao.findSentFriendshipRequests(member);
    }

    public List<FriendshipRequest> findReceivedFriendshipRequests(Member member) {
        log.info("MemberRepository.findReceivedFriendshipRequest() called");

        return friendshipRequestDao.findReceivedFriendshipRequests(member);
    }

    public void createFriendshipRequest(Member member, Member friend) {
        log.info("MemberRepository.createFriendshipRequest() called");

        friendshipRequestDao.createFriendshipRequest(member, friend);
    }

    public void removeFriendshipRequest(Member member, Member friend) {
        log.info("MemberRepository.removeFriendshipRequest() called");

        friendshipRequestDao.removeFriendshipRequest(member, friend);
    }

    public void createFriendship(Member member, Member friend) {
        log.info("MemberRepository.createFriendship() called");

        friendshipDao.createFriendship(member, friend);
    }

    public void removeFriendship(Member member, Member friend) {
        log.info("MemberRepository.removeFriendship() called");

        friendshipDao.removeFriendship(member, friend);
    }

    public boolean areFriends(Member member, Member friend) {
        log.info("MemberRepository.areFriends() called");

        return friendshipDao.areFriends(member, friend);
    }

    public List<Friendship> searchFriendships(Member member, MemberSearchCondition condition) {
        log.info("MemberRepository.searchFriendship() called");

        return friendshipDao.searchFriendships(member, condition);
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

    public Blacklist addBlacklist(Member member, Member target) {
        log.info("MemberRepository.addBlacklist() called");

        return blacklistDao.createBlacklist(member, target);
    }

    public Blacklist removeBlacklist(Member member, Member target) {
        log.info("MemberRepository.removeBlacklist() called");

        return blacklistDao.removeBlacklist(member, target);
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
