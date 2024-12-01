package junwatson.mychat.util;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.MemberInfoRequestDto;
import junwatson.mychat.dto.request.MemberSignUpRequestDto;
import junwatson.mychat.dto.response.TokenDto;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {

    @Autowired
    private MemberService memberService;
    @Autowired
    private TokenProvider tokenProvider;

    /**
     * memberA와 memberB의 모든 정보가 같은지 검사하는 메서드.<br>
     * 단, id는 검사하지 않는다.
     */
    public boolean isSameMember(Member memberA, Member memberB) {
        return memberA.getName().equals(memberB.getName()) &&
                memberA.getEmail().equals(memberB.getEmail()) &&
                memberA.getAuthorizedBy() == memberB.getAuthorizedBy() &&
                memberA.getPassword().equals(memberB.getPassword()) &&
                memberA.getRole() == memberB.getRole();
    }

    /**
     * 토큰을 통해 회원을 조회하는 메서드.
     */
    public Member findByToken(String token) {
        Claims claims = tokenProvider.parseClaims(token);
        long memberId = Long.parseLong(claims.getSubject());

        return memberService.findById(memberId);
    }

    /**
     * 정상적인 회원을 회원가입 시키는 메서드
     */
    public Member createTestMember(String extraEmail, String extraName) {
        TokenDto tokenDto = memberService.signUp(MemberSignUpRequestDto.builder()
                .email("helloImTestMember@testemail.com" + extraEmail)
                .name("testName" + extraName)
                .password("testPassword")
                .profileUrl("testProfileUrl")
                .build());
        Claims claims = tokenProvider.parseClaims(tokenDto.getAccessToken());
        long memberId = Long.parseLong(claims.getSubject());

        return memberService.findById(memberId);
    }

    /**
     * 회원 여려명을 회원가입 시키는 메서드<br>
     * TEST_EMAIL_01234, TEST_NAME_01234 형식으로 생성된다
     */
    public Member[] createTestMembers(int count) {
        Member[] members = new Member[count];
        StringBuilder extraEmail = new StringBuilder("TEST_EMAIL_");
        StringBuilder extraName = new StringBuilder("TEST_NAME_");

        for (int i = 0; i < count; i++) {
            extraEmail.append(i);
            extraName.append(i);
            members[i] = createTestMember(extraEmail.toString(), extraName.toString());
        }

        return members;
    }

    /**
     * 영속성 컨텍스트를 비우는 메서드
     */
    public void clearEntityManager(EntityManager em) {
        em.flush();
        em.clear();
    }

    /**
     * 두 Member 엔티티를 친구 관계로 등록하는 메서드<br>
     * EntityManager를 비운다는 것에 주의할 것
     */
    public void makeFriends(Member member1, Member member2, EntityManager em) {
        clearEntityManager(em);

        Member memberA = memberService.findById(member1.getId());
        memberService.createFriendshipRequest(memberA, MemberInfoRequestDto.builder()
                .email(member2.getEmail())
                .build());
        clearEntityManager(em);

        Member memberB = memberService.findById(member2.getId());
        memberService.createFriendshipRequest(memberB, MemberInfoRequestDto.builder()
                .email(member1.getEmail())
                .build());
        clearEntityManager(em);
    }
}
