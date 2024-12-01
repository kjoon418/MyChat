package junwatson.mychat.service;

import jakarta.persistence.EntityManager;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.MemberSignInRequestDto;
import junwatson.mychat.dto.request.MemberSignUpRequestDto;
import junwatson.mychat.dto.response.TokenDto;
import junwatson.mychat.exception.IllegalMemberStateException;
import junwatson.mychat.exception.MemberNotExistsException;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.repository.dao.RefreshTokenDao;
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

    private static final String BASIC_EMAIL = "";

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
    void signUp_success() {
        // given: 정상적인 정보로 회원가입
        MemberSignUpRequestDto requestDto = MemberSignUpRequestDto.builder()
                .email("hellotest@hellotestemail.com")
                .name("testMemberA")
                .password("123123")
                .profileUrl("testProfileUrl")
                .build();
        TokenDto tokenDto = memberService.signUp(requestDto);
        utils.clearEntityManager(em);

        // when: 엑세스 토큰을 기반으로 회원 조회
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
        MemberSignUpRequestDto requestDto1 = MemberSignUpRequestDto.builder()
                .email("sametest@testemail.com")
                .name("testMember1")
                .password("testPassword")
                .build();
        MemberSignUpRequestDto requestDto2 = MemberSignUpRequestDto.builder()
                .email("sametest@testemail.com")
                .name("testMember2")
                .password("testPassword")
                .build();

        // when
        memberService.signUp(requestDto1);

        // then: 중복 이메일로 회원 생성
        assertThatThrownBy(() -> memberService.signUp(requestDto2)).isInstanceOf(IllegalMemberStateException.class);
    }

    @Test
    @DisplayName("회원가입: 부적절한 단어 예외")
    void signUp_illegalWord() {
        // given
        MemberSignUpRequestDto illegalEmailRequestDto = MemberSignUpRequestDto.builder()
                .email("(email@testemail.com1")
                .name("testMemberA")
                .password("test")
                .build();
        MemberSignUpRequestDto illegalNameRequestDto = MemberSignUpRequestDto.builder()
                .email("email@testemail.com2")
                .name("") // 회원 이름에는 특수문자가 허용됨
                .password("test")
                .build();
        MemberSignUpRequestDto illegalPasswordRequestDto = MemberSignUpRequestDto.builder()
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
        MemberSignUpRequestDto illegalEmailRequestDto = MemberSignUpRequestDto.builder()
                .email("email@testemail.com1 ")
                .name("testMemberA")
                .password("test")
                .build();
        MemberSignUpRequestDto illegalNameRequestDto = MemberSignUpRequestDto.builder()
                .email("email@testemail.com2")
                .name("im jun")
                .password("test")
                .build();
        MemberSignUpRequestDto illegalPasswordRequestDto = MemberSignUpRequestDto.builder()
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
        MemberSignUpRequestDto illegalEmailRequestDto = MemberSignUpRequestDto.builder()
                .email("")
                .name("testMemberA")
                .password("test")
                .build();
        MemberSignUpRequestDto illegalNameRequestDto = MemberSignUpRequestDto.builder()
                .email("email@testemail.com2")
                .name("")
                .password("test")
                .build();
        MemberSignUpRequestDto illegalPasswordRequestDto = MemberSignUpRequestDto.builder()
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
        MemberSignUpRequestDto illegalEmailRequestDto = MemberSignUpRequestDto.builder()
                .email(null)
                .name("testMemberA")
                .password("test")
                .build();
        MemberSignUpRequestDto illegalNameRequestDto = MemberSignUpRequestDto.builder()
                .email("email@testemail.com2")
                .name(null)
                .password("test")
                .build();
        MemberSignUpRequestDto illegalPasswordRequestDto = MemberSignUpRequestDto.builder()
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
        MemberSignUpRequestDto requestDto1 = MemberSignUpRequestDto.builder()
                .email("email@testemail.com1")
                .name("sameName")
                .password("samePassword")
                .profileUrl("sameProfileUrl")
                .build();
        MemberSignUpRequestDto requestDto2 = MemberSignUpRequestDto.builder()
                .email("email@testemail.com2")
                .name("sameName")
                .password("samePassword")
                .profileUrl("sameProfileUrl")
                .build();

        // when
        TokenDto tokenDto1 = memberService.signUp(requestDto1);
        TokenDto tokenDto2 = memberService.signUp(requestDto2);

        utils.clearEntityManager(em);

        Member member1 = utils.findByToken(tokenDto1.getAccessToken());
        Member member2 = utils.findByToken(tokenDto2.getAccessToken());

        // then
        assertThat(utils.isSameMember(member1, requestDto1.toEntity())).isTrue();
        assertThat(utils.isSameMember(member2, requestDto2.toEntity())).isTrue();
    }

    @Test
    @DisplayName("로그인: 정상")
    void login_success() {
        // given: 테스트용 멥버 회원가입
        Member member = utils.createTestMember(BASIC_EMAIL);
        utils.clearEntityManager(em);
        

        // when: 테스트용 멤버 로그인
        TokenDto tokenDto = memberService.signIn(MemberSignInRequestDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .build());

        // then
        assertThat(refreshTokenDao.isValidateRefreshToken(member, tokenDto.getRefreshToken())).isTrue();
    }

    @Test
    @DisplayName("로그인: 다른 곳에서 로그인시 리프레시 토큰 무효화")
    void login_invalidateToken() throws InterruptedException {
        // given: 테스트용 멤버 회원가입
        Member member = utils.createTestMember(BASIC_EMAIL);

        // when: 테스트용 멤버를 2번 로그인시킴
        TokenDto tokenDto1 = memberService.signIn(MemberSignInRequestDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .build());

        Thread.sleep(1000); // 다른 내용의 토큰을 발급받을 수 있도록 스레드를 대기시킴

        TokenDto tokenDto2 = memberService.signIn(MemberSignInRequestDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .build());
        utils.clearEntityManager(em);

        // then: 기존 로그인으로 생성된 리프레시 토큰 무효화
        Member findMember = em.find(Member.class, member.getId());
        assertThat(refreshTokenDao.isValidateRefreshToken(findMember, tokenDto1.getRefreshToken())).isFalse();
        assertThat(refreshTokenDao.isValidateRefreshToken(findMember, tokenDto2.getRefreshToken())).isTrue();
    }

    @Test
    @DisplayName("로그아웃: 성공")
    void logout_success() {
        // given: 회원 생성
        Member member = utils.createTestMember(BASIC_EMAIL);
        utils.clearEntityManager(em);

        // case1: 로그아웃 전 리프레시 토큰 통과
        Member testMember = memberService.findById(member.getId());
        assertThat(refreshTokenDao.isValidateRefreshToken(testMember, testMember.getRefreshToken().getToken())).isTrue();

        // case2: 로그아웃 후 리프레시 토큰 거절
        memberService.logout(testMember);
        assertThat(testMember.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("회원 삭제: 성공")
    void withdraw_success() {
        // given
        Member signUpMember = utils.createTestMember(BASIC_EMAIL);
        utils.clearEntityManager(em);

        // when: 회원 삭제
        Member signInMember = memberService.findById(signUpMember.getId());
        memberService.withdrawMembership(signInMember);
        utils.clearEntityManager(em);

        // then: 조회 실패
        assertThatThrownBy(() -> memberService.findById(signInMember.getId())).isInstanceOf(MemberNotExistsException.class);
    }

}