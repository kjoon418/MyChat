package junwatson.mychat.service;

import jakarta.persistence.EntityManager;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.response.TokenDto;
import junwatson.mychat.exception.IllegalMemberStateException;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.repository.dao.RefreshTokenDao;
import junwatson.mychat.testdto.TestMemberSignInRequestDto;
import junwatson.mychat.testdto.TestMemberSignUpRequestDto;
import junwatson.mychat.util.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@Component
@Transactional
@SpringBootTest
class MemberServiceTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private TestUtils utils;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private MemberService memberService;
    @Autowired
    private RefreshTokenDao refreshTokenDao;

    @Test
    @DisplayName("회원가입: 정상")
    void signUp_normal() {
        // given: 정상적인 정보로 회원가입
        TestMemberSignUpRequestDto requestDto = TestMemberSignUpRequestDto.builder()
                .email("hellotest@hellotestemail.com")
                .name("testMemberA")
                .password("123123")
                .profileUrl("testProfileUrl")
                .build();

        // when
        TokenDto tokenDto = memberService.signUp(requestDto);

        em.flush(); // 1차 캐시 초기화
        em.clear();

        Member member = utils.findByToken(tokenDto.getAccessToken());

        // then: 토큰의 유효성 및 회원이 제대로 저장되었는지 확인
        assertThat(tokenProvider.validateToken(tokenDto.getAccessToken())).isTrue();
        assertThat(tokenProvider.validateToken(tokenDto.getRefreshToken())).isTrue();
        assertThat(utils.isSameMember(member, requestDto.toEntity())).isTrue();
    }

    @Test
    @DisplayName("회원가입: 이메일 중복 예외")
    void signUp_emailDuplicate() {
        // given: 이메일이 같은 두 회원 회원가입
        TestMemberSignUpRequestDto requestDto1 = TestMemberSignUpRequestDto.builder()
                .email("sametest@testemail.com")
                .name("testMember1")
                .password("testPassword")
                .build();
        TestMemberSignUpRequestDto requestDto2 = TestMemberSignUpRequestDto.builder()
                .email("sametest@testemail.com")
                .name("testMember2")
                .password("testPassword")
                .build();

        // then
        assertThatThrownBy(() -> {
            TokenDto tokenDto1 = memberService.signUp(requestDto1);
            TokenDto tokenDto2 = memberService.signUp(requestDto2);
        }).isInstanceOf(IllegalMemberStateException.class);
    }

    @Test
    @DisplayName("회원가입: 부적절한 단어 예외")
    void signUp_illegalWord() {
        // given
        TestMemberSignUpRequestDto illegalEmailRequestDto = TestMemberSignUpRequestDto.builder()
                .email("(email@testemail.com1")
                .name("testMemberA")
                .password("test")
                .build();
        TestMemberSignUpRequestDto illegalNameRequestDto = TestMemberSignUpRequestDto.builder()
                .email("email@testemail.com2")
                .name("") // 회원 이름에는 특수문자가 허용됨
                .password("test")
                .build();
        TestMemberSignUpRequestDto illegalPasswordRequestDto = TestMemberSignUpRequestDto.builder()
                .email("email@testemail.com3")
                .name("testMemberA")
                .password("*illegalPassword*")
                .build();

        // then
        assertThatThrownBy(() -> memberService.signUp(illegalEmailRequestDto))
                .isInstanceOf(IllegalMemberStateException.class);
        assertThatThrownBy(() -> memberService.signUp(illegalNameRequestDto))
                .isInstanceOf(IllegalMemberStateException.class);
        assertThatThrownBy(() -> memberService.signUp(illegalPasswordRequestDto))
                .isInstanceOf(IllegalMemberStateException.class);
    }

    @Test
    @DisplayName("회원가입: 공백 예외")
    void signUp_illegalSpace() {
        // given
        TestMemberSignUpRequestDto illegalEmailRequestDto = TestMemberSignUpRequestDto.builder()
                .email("email@testemail.com1 ")
                .name("testMemberA")
                .password("test")
                .build();
        TestMemberSignUpRequestDto illegalNameRequestDto = TestMemberSignUpRequestDto.builder()
                .email("email@testemail.com2")
                .name("im jun")
                .password("test")
                .build();
        TestMemberSignUpRequestDto illegalPasswordRequestDto = TestMemberSignUpRequestDto.builder()
                .email("email@testemail.com3")
                .name("testMemberA")
                .password(" illegalPassword")
                .build();

        // then
        assertThatThrownBy(() -> memberService.signUp(illegalEmailRequestDto))
                .isInstanceOf(IllegalMemberStateException.class);
        assertThatThrownBy(() -> memberService.signUp(illegalNameRequestDto))
                .isInstanceOf(IllegalMemberStateException.class);
        assertThatThrownBy(() -> memberService.signUp(illegalPasswordRequestDto))
                .isInstanceOf(IllegalMemberStateException.class);
    }

    @Test
    @DisplayName("회원가입: 빈 값 예외")
    void signUp_illegalEmptyString() {
        // given
        TestMemberSignUpRequestDto illegalEmailRequestDto = TestMemberSignUpRequestDto.builder()
                .email("")
                .name("testMemberA")
                .password("test")
                .build();
        TestMemberSignUpRequestDto illegalNameRequestDto = TestMemberSignUpRequestDto.builder()
                .email("email@testemail.com2")
                .name("")
                .password("test")
                .build();
        TestMemberSignUpRequestDto illegalPasswordRequestDto = TestMemberSignUpRequestDto.builder()
                .email("email@testemail.com3")
                .name("testMemberA")
                .password("")
                .build();

        // then
        assertThatThrownBy(() -> memberService.signUp(illegalEmailRequestDto))
                .isInstanceOf(IllegalMemberStateException.class);
        assertThatThrownBy(() -> memberService.signUp(illegalNameRequestDto))
                .isInstanceOf(IllegalMemberStateException.class);
        assertThatThrownBy(() -> memberService.signUp(illegalPasswordRequestDto))
                .isInstanceOf(IllegalMemberStateException.class);
    }

    @Test
    @DisplayName("회원가입: null 전달 예외")
    void signUp_illegalNull() {
        // given
        TestMemberSignUpRequestDto illegalEmailRequestDto = TestMemberSignUpRequestDto.builder()
                .email(null)
                .name("testMemberA")
                .password("test")
                .build();
        TestMemberSignUpRequestDto illegalNameRequestDto = TestMemberSignUpRequestDto.builder()
                .email("email@testemail.com2")
                .name(null)
                .password("test")
                .build();
        TestMemberSignUpRequestDto illegalPasswordRequestDto = TestMemberSignUpRequestDto.builder()
                .email("email@testemail.com3")
                .name("testMemberA")
                .password(null)
                .build();

        // then
        assertThatThrownBy(() -> memberService.signUp(illegalEmailRequestDto))
                .isInstanceOf(IllegalMemberStateException.class);
        assertThatThrownBy(() -> memberService.signUp(illegalNameRequestDto))
                .isInstanceOf(IllegalMemberStateException.class);
        assertThatThrownBy(() -> memberService.signUp(illegalPasswordRequestDto))
                .isInstanceOf(IllegalMemberStateException.class);
    }

    @Test
    @DisplayName("회원가입: 비밀번호, 이름 중복 허용")
    void signUp_allowedDuplicate() {
        // given
        TestMemberSignUpRequestDto requestDto1 = TestMemberSignUpRequestDto.builder()
                .email("email@testemail.com1")
                .name("sameName")
                .password("samePassword")
                .profileUrl("sameProfileUrl")
                .build();
        TestMemberSignUpRequestDto requestDto2 = TestMemberSignUpRequestDto.builder()
                .email("email@testemail.com2")
                .name("sameName")
                .password("samePassword")
                .profileUrl("sameProfileUrl")
                .build();

        // when
        TokenDto tokenDto1 = memberService.signUp(requestDto1);
        TokenDto tokenDto2 = memberService.signUp(requestDto2);

        em.flush();
        em.clear();

        Member member1 = utils.findByToken(tokenDto1.getAccessToken());
        Member member2 = utils.findByToken(tokenDto2.getAccessToken());

        // then
        assertThat(utils.isSameMember(member1, requestDto1.toEntity())).isTrue();
        assertThat(utils.isSameMember(member2, requestDto2.toEntity())).isTrue();
    }

    @Test
    @DisplayName("로그인: 정상")
    void login_normal() {
        // given: 테스트용 멥버 회원가입
        Member member = utils.createSignUpMember("");
        em.flush();
        em.clear();

        // when: 테스트용 멤버 로그인
        TokenDto tokenDto = memberService.signIn(TestMemberSignInRequestDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .build());

        // then
        assertThat(refreshTokenDao.isValidateRefreshToken(member, tokenDto.getRefreshToken())).isTrue();
    }

    @Test
    @DisplayName("로그인: 다른 곳에서 로그인시 리프레시 토큰 무효화")
    void login_invalidateToken() {
        // given: 테스트용 멤버 회원가입
        Member member = utils.createSignUpMember("");
        em.flush();
        em.clear();

        // when: 테스트용 멤버를 2번 로그인시킴
        TokenDto tokenDto1 = memberService.signIn(TestMemberSignInRequestDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .build());
        TokenDto tokenDto2 = memberService.signIn(TestMemberSignInRequestDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .build());

        // then: 기존 로그인으로 생성된 리프레시 토큰 무효화
        assertThat(refreshTokenDao.isValidateRefreshToken(member, tokenDto1.getRefreshToken())).isFalse();
        assertThat(refreshTokenDao.isValidateRefreshToken(member, tokenDto2.getRefreshToken())).isTrue();
    }
}