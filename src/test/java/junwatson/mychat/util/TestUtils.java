package junwatson.mychat.util;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.ChatRoomCreateRequestDto;
import junwatson.mychat.dto.request.MemberInfoRequestDto;
import junwatson.mychat.dto.request.MemberSignUpRequestDto;
import junwatson.mychat.dto.response.TokenDto;
import junwatson.mychat.exception.IllegalMemberStateException;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.repository.MemberRepository;
import junwatson.mychat.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
     * 회원을 회원가입 시키는 메서드.<br>
     * 이메일, 이름 등의 값은 임의의 값으로 지정된다.<br>
     * 이메일은 중복되지 않는다.
     */
    public Member createTestMember() {

        TokenDto tokenDto = null;
        while(true) {
            MemberSignUpRequestDto requestDto = MemberSignUpRequestDto.builder()
                    .email(getRandomIntegerString())
                    .name(getRandomIntegerString())
                    .password(getRandomIntegerString())
                    .profileUrl(getRandomIntegerString())
                    .build();

            try {
                tokenDto = memberService.signUp(requestDto);
            } catch (IllegalMemberStateException e) {
                continue;
            }
            break;
        }
        Claims claims = tokenProvider.parseClaims(tokenDto.getAccessToken());
        long memberId = Long.parseLong(claims.getSubject());

        return memberService.findById(memberId);
    }

    /**
     * 회원을 회원가입 시키는 메서드.<br>
     * 기본 이메일: helloImTestMember@testemail.com<br>
     * 기본 이름: testName<br>
     * 기본 비밀번호: testPassword<br>
     * 기본 프로필 URL: testProfileUrl
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
     * 회원 여려명을 회원가입 시키는 메서드.<br>
     * TEST_EMAIL_01234, TEST_NAME_01234 형식으로 생성된다.<br>
     * 이메일/이름 뒤의 숫자는 0부터 시작해서 01, 012, 0123처럼 증가한다.
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
     * 두 Member 엔티티를 친구 관계로 등록하는 메서드
     */
    public void makeFriends(Member member1, Member member2) {
        Member memberA = memberService.findById(member1.getId());
        memberService.createFriendshipRequest(memberA, MemberInfoRequestDto.builder()
                .email(member2.getEmail())
                .build());

        Member memberB = memberService.findById(member2.getId());
        memberService.createFriendshipRequest(memberB, MemberInfoRequestDto.builder()
                .email(member1.getEmail())
                .build());
    }

    /**
     * ChatRoomCreateRequestDto를 생성하는 메서드
     * name과 profileUrl은 임의의 이름으로 지정된다
     */
    public ChatRoomCreateRequestDto createChatRoomCreateRequestDto(List<Member> members) {
        ChatRoomCreateRequestDto requestDto = ChatRoomCreateRequestDto.builder()
                .name(getRandomIntegerString())
                .profileUrl(getRandomIntegerString())
                .build();

        for (Member member : members) {
            requestDto.getFriends().add(ChatRoomCreateRequestDto.Friend.builder()
                    .email(member.getEmail())
                    .build());
        }

        return requestDto;
    }

    /**
     * ChatRoomCreateRequestDto를 생성하는 메서드
     */
    public ChatRoomCreateRequestDto createChatRoomCreateRequestDto(List<Member> members, String name, String profileUrl) {
        ChatRoomCreateRequestDto requestDto = ChatRoomCreateRequestDto.builder()
                .name(name)
                .profileUrl(profileUrl)
                .build();

        for (Member member : members) {
            requestDto.getFriends().add(ChatRoomCreateRequestDto.Friend.builder()
                    .email(member.getEmail())
                    .build());
        }

        return requestDto;
    }

    /**
     * MemberInfoRequestDto를 생성하는 메서드
     */
    public MemberInfoRequestDto createMemberInfoRequestDto(Member member) {
        return MemberInfoRequestDto.builder()
                .email(member.getEmail())
                .build();
    }

    /**
     * 무작위 숫자로 구성된 문자열을 반환하는 메서드
     */
    private String getRandomIntegerString() {
        return Double.toString(Math.random()).split("\\.")[1];
    }
}
