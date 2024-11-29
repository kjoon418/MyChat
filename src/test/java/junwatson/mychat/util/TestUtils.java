package junwatson.mychat.util;

import io.jsonwebtoken.Claims;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.response.TokenDto;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.service.MemberService;
import junwatson.mychat.testdto.TestMemberSignUpRequestDto;
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
    public Member createSignUpMember(String extraEmail) {
        TokenDto tokenDto = memberService.signUp(TestMemberSignUpRequestDto.builder()
                .email("helloImTestMember@testemail.com" + extraEmail)
                .name("testMember")
                .password("testPassword")
                .profileUrl("testProfileUrl")
                .build());
        Claims claims = tokenProvider.parseClaims(tokenDto.getAccessToken());
        long memberId = Long.parseLong(claims.getSubject());

        return memberService.findById(memberId);
    }
}
