package junwatson.mychat.service;

import jakarta.persistence.EntityManager;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.MemberInfoRequestDto;
import junwatson.mychat.dto.request.MemberSearchRequestDto;
import junwatson.mychat.dto.request.MemberSignInRequestDto;
import junwatson.mychat.dto.request.MemberSignUpRequestDto;
import junwatson.mychat.dto.response.MemberInfoResponseDto;
import junwatson.mychat.dto.response.ReissueAccessTokenResponseDto;
import junwatson.mychat.dto.response.TokenDto;
import junwatson.mychat.exception.BlockException;
import junwatson.mychat.exception.IllegalMemberStateException;
import junwatson.mychat.exception.IllegalSearchConditionException;
import junwatson.mychat.exception.MemberNotExistsException;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.jwt.TokenType;
import junwatson.mychat.repository.dao.RefreshTokenDao;
import junwatson.mychat.util.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Component
@Transactional
@SpringBootTest
class MemberServiceTest {

    private static final String NOT_EXISTS_EMAIL = "asdc897ssa9d78cs7dc8ia5s8dc@testEmail.com";
    private static final String NOT_EXISTS_PASSWORD = "3212b1jhb1324kl4312n4g1j412j4b132134";

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
        Member member = utils.createTestMember();
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
    @DisplayName("로그인: 잘못된 이메일, 비밀번호 예외")
    void login_illegalEmailOrPassword() {
        // given: 회원 가입
        Member member = utils.createTestMember();
        utils.clearEntityManager(em);

        // case1: 잘못된 이메일로 로그인
        assertThatThrownBy(() -> memberService.signIn(MemberSignInRequestDto.builder()
                .email(NOT_EXISTS_EMAIL)
                .password(member.getPassword())
                .build()))
                .isInstanceOf(MemberNotExistsException.class);

        // case2: 잘못된 비밀번호로 로그인
        assertThatThrownBy(() -> memberService.signIn(MemberSignInRequestDto.builder()
                .email(member.getEmail())
                .password(NOT_EXISTS_PASSWORD)
                .build()))
                .isInstanceOf(MemberNotExistsException.class);
    }

    @Test
    @DisplayName("로그인: 다른 곳에서 로그인시 리프레쉬 토큰 무효화")
    void login_invalidateToken() throws InterruptedException {
        // given: 테스트용 멤버 회원가입
        Member member = utils.createTestMember();

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

        // then: 기존 로그인으로 생성된 리프레쉬 토큰 무효화
        Member findMember = em.find(Member.class, member.getId());
        assertThat(refreshTokenDao.isValidateRefreshToken(findMember, tokenDto1.getRefreshToken())).isFalse();
        assertThat(refreshTokenDao.isValidateRefreshToken(findMember, tokenDto2.getRefreshToken())).isTrue();
    }

    @Test
    @DisplayName("엑세스 토큰 재발급: 성공")
    void reissue_success() {
        // given: 회원 가입 및 토큰 획득
        Member signUpMember = utils.createTestMember();
        TokenDto tokenDto = memberService.signIn(MemberSignInRequestDto.builder()
                .email(signUpMember.getEmail())
                .password(signUpMember.getPassword())
                .build());
        utils.clearEntityManager(em);

        // when: 리프레쉬 토큰을 통해 엑세스 토큰 획득
        ReissueAccessTokenResponseDto responseDto = memberService.reissueAccessToken(tokenDto.getRefreshToken());
        String accessToken = responseDto.getAccessToken();

        // then: 엑세스 토큰 유효성 검사
        assertThat(tokenProvider.hasProperType(accessToken, TokenType.ACCESS)).isTrue();
        assertThat(tokenProvider.validateToken(accessToken)).isTrue();
    }

    @Test
    @DisplayName("로그아웃: 성공")
    void logout_success() {
        // given: 회원 생성
        Member member = utils.createTestMember();
        utils.clearEntityManager(em);

        // case1: 로그아웃 전 리프레쉬 토큰 통과
        Member testMember = memberService.findById(member.getId());
        assertThat(refreshTokenDao.isValidateRefreshToken(testMember, testMember.getRefreshToken().getToken())).isTrue();

        // case2: 로그아웃 후 리프레쉬 토큰 거절
        memberService.logout(testMember);
        assertThat(testMember.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("회원 삭제: 성공")
    void withdraw_success() {
        // given
        Member signUpMember = utils.createTestMember();
        utils.clearEntityManager(em);

        // when: 회원 삭제
        Member signInMember = memberService.findById(signUpMember.getId());
        memberService.withdrawMembership(signInMember);
        utils.clearEntityManager(em);

        // then: 조회 실패
        assertThatThrownBy(() -> memberService.findById(signInMember.getId())).isInstanceOf(MemberNotExistsException.class);
    }

    @Test
    @DisplayName("회원 검색: 이메일로 검색 성공")
    void searchMember_emailSuccess() {
        // given: 회원 생성
        Member[] testMembers = utils.createTestMembers(4);
        Member testMember1 = testMembers[0]; // 0
        Member testMember2 = testMembers[1]; // 01
        Member testMember3 = testMembers[2]; // 012
        Member testMember4 = testMembers[3]; // 0123

        // then: 검색 성공
        List<MemberInfoResponseDto> resultExpected3 = memberService.searchMembersByCondition(testMember1, MemberSearchRequestDto.builder()
                .email("0")
                .build());
        assertThat(resultExpected3.size()).isEqualTo(3);

        List<MemberInfoResponseDto> resultExpected1 = memberService.searchMembersByCondition(testMember1, MemberSearchRequestDto.builder()
                .email("3")
                .build());
        assertThat(resultExpected1.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("회원 검색: 이름으로 검색 성공")
    void searchMember_nameSuccess() {
        // given: 회원 생성
        Member[] testMembers = utils.createTestMembers(4);
        Member testMember1 = testMembers[0]; // 0
        Member testMember2 = testMembers[1]; // 01
        Member testMember3 = testMembers[2]; // 012
        Member testMember4 = testMembers[3]; // 0123

        // then: 검색 성공
        List<MemberInfoResponseDto> resultExpected3 = memberService.searchMembersByCondition(testMember1, MemberSearchRequestDto.builder()
                .name("0")
                .build());
        assertThat(resultExpected3.size()).isEqualTo(3);

        List<MemberInfoResponseDto> resultExpected1 = memberService.searchMembersByCondition(testMember1, MemberSearchRequestDto.builder()
                .name("3")
                .build());
        assertThat(resultExpected1.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("회원 검색: 모든 조건(이메일, 이름)으로 검색 성공")
    void searchMember_allConditionSuccess() {
        // given: 회원 생성
        Member[] testMembers = utils.createTestMembers(4);
        Member testMember1 = testMembers[0]; // 0
        Member testMember2 = testMembers[1]; // 01
        Member testMember3 = testMembers[2]; // 012
        Member testMember4 = testMembers[3]; // 0123

        // then: 검색 성공
        List<MemberInfoResponseDto> resultExpected2 = memberService.searchMembersByCondition(testMember1, MemberSearchRequestDto.builder()
                .email("0")
                .name("2")
                .build());
        assertThat(resultExpected2.size()).isEqualTo(2);

        List<MemberInfoResponseDto> resultExpected1 = memberService.searchMembersByCondition(testMember1, MemberSearchRequestDto.builder()
                .email("3")
                .name("0")
                .build());
        assertThat(resultExpected1.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("회원 검색: 조건 없이 검색 예외")
    void searchMember_noCondition() {
        // given: 회원 생성
        Member[] testMembers = utils.createTestMembers(2);
        Member testMember1 = testMembers[0];

        // then: 조건 없이 검색 실패
        assertThatThrownBy(() -> memberService.searchMembersByCondition(
                testMember1,
                MemberSearchRequestDto.builder().build()))
                .isInstanceOf(IllegalSearchConditionException.class);
        assertThatThrownBy(() -> memberService.searchMembersByCondition(
                testMember1,
                MemberSearchRequestDto.builder()
                        .name("")
                        .email("")
                        .build()))
                .isInstanceOf(IllegalSearchConditionException.class);
    }

    @Test
    @DisplayName("친구 요청 생성: 성공")
    void friendshipRequest_success() {
        // given: 회원 생성
        Member signUpMember1 = utils.createTestMember("1", "A");
        Member signUpMember2 = utils.createTestMember("2", "B");

        // when: 친구 요청 생성
        memberService.createFriendshipRequest(signUpMember1, MemberInfoRequestDto.builder()
                .email(signUpMember2.getEmail())
                .build());
        utils.clearEntityManager(em);

        // then: 친구 요청 조회 성공
        Member member1 = memberService.findById(signUpMember1.getId());
        Member member2 = memberService.findById(signUpMember2.getId());

        assertThat(memberService.findSentFriendshipRequests(member1).stream()
                .anyMatch(memberInfoResponseDto -> memberInfoResponseDto.getEmail().equals(member2.getEmail())))
                .isTrue();
        assertThat(memberService.findReceivedFriendshipRequests(member2).stream()
                .anyMatch(memberInfoResponseDto -> memberInfoResponseDto.getEmail().equals(member1.getEmail())))
                .isTrue();
    }

    @Test
    @DisplayName("친구 요청 생성: 존재하지 않는 회원 예외")
    void friendshipRequest_notExistsMember() {
        // given: 회원 생성
        Member member = utils.createTestMember();

        // then: 존재하지 않는 회원에게 친구 요청 전송
        assertThatThrownBy(() -> memberService.createFriendshipRequest(member, MemberInfoRequestDto.builder()
                .email(NOT_EXISTS_EMAIL)
                .build()))
                .isInstanceOf(MemberNotExistsException.class);
    }

    @Test
    @DisplayName("친구 요청 생성: 차단된 회원 예외")
    void friendshipRequest_blockedMember() {
        // given: 회원 생성
        Member signUpMember1 = utils.createTestMember("1", "A");
        Member signUpMember2 = utils.createTestMember("2", "B");

        // when: 차단(회원1 -> 회원2)
        memberService.addBlacklist(signUpMember1, MemberInfoRequestDto.builder()
                .email(signUpMember2.getEmail())
                .build());
        utils.clearEntityManager(em);

        // then: 친구 요청 실패(회원2 -> 회원1)
        Member member1 = memberService.findById(signUpMember1.getId());
        Member member2 = memberService.findById(signUpMember2.getId());
        assertThatThrownBy(() ->         memberService.createFriendshipRequest(member2, MemberInfoRequestDto.builder()
                .email(member1.getEmail())
                .build()))
                .isInstanceOf(BlockException.class);
    }

    @Test
    @DisplayName("친구 생성: 성공")
    void friendship_success() {
        // given: 회원 생성
        Member signUpMember1 = utils.createTestMember("1", "A");
        Member signUpMember2 = utils.createTestMember("2", "B");
        utils.clearEntityManager(em);

        // when: 양방향으로 친구 요청
        Member requestMember1 = memberService.findById(signUpMember1.getId());
        Member requestMember2 = memberService.findById(signUpMember2.getId());
        memberService.createFriendshipRequest(requestMember1, MemberInfoRequestDto.builder()
                .email(signUpMember2.getEmail())
                .build());
        memberService.createFriendshipRequest(requestMember2, MemberInfoRequestDto.builder()
                .email(signUpMember1.getEmail())
                .build());
        utils.clearEntityManager(em);

        // then: 친구 생성 및 친구 요청 자동 삭제
        Member member1 = memberService.findById(signUpMember1.getId());
        Member member2 = memberService.findById(signUpMember2.getId());

        assertThat(member1.getFriendships().stream()
                .anyMatch(friendship -> friendship.getFriendMember().equals(member2)))
                .isTrue();
        assertThat(member2.getFriendships().stream()
                .anyMatch(friendship -> friendship.getFriendMember().equals(member1)))
                .isTrue();

        assertThat(member1.getSentFriendshipRequests().isEmpty())
                .isTrue();
        assertThat(member1.getReceivedFriendshipRequests().isEmpty())
                .isTrue();
        assertThat(member2.getSentFriendshipRequests().isEmpty())
                .isTrue();
        assertThat(member2.getReceivedFriendshipRequests().isEmpty())
                .isTrue();
    }

    @Test
    @DisplayName("친구 생성: 자기 자신에게 요청 예외")
    void friendship_myself() {
        // given: 회원 생성
        Member member = utils.createTestMember();

        // then: 자기 자신에게 친구 요청 실패
        assertThatThrownBy(() -> memberService.createFriendshipRequest(member, MemberInfoRequestDto.builder()
                .email(member.getEmail())
                .build()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("친구 삭제: 성공")
    void deleteFriendship_success() {
        // given: 회원 생성 및 친구 등록
        Member signUpMember1 = utils.createTestMember("1", "A");
        Member signUpMember2 = utils.createTestMember("2", "B");
        utils.makeFriends(signUpMember1, signUpMember2);
        utils.clearEntityManager(em);

        // when: 친구 삭제
        Member deleteMember1 = memberService.findById(signUpMember1.getId());
        memberService.removeFriendship(deleteMember1, MemberInfoRequestDto.builder()
                .email(signUpMember2.getEmail())
                .build());
        utils.clearEntityManager(em);

        // then: 친구 조회 실패
        Member member1 = memberService.findById(signUpMember1.getId());
        Member member2 = memberService.findById(signUpMember2.getId());

        assertThat(member1.getFriendships().isEmpty())
                .isTrue();
        assertThat(member2.getFriendships().isEmpty())
                .isTrue();
    }

    @Test
    @DisplayName("친구 검색: 조건 없이 검색 성공")
    void searchFriendship_noConditionSuccess() {
        // given: 회원 생성
        Member[] testMembers = utils.createTestMembers(4);
        Member testMember1 = testMembers[0];
        Member testMember2 = testMembers[1];
        Member testMember3 = testMembers[2];
        Member testMember4 = testMembers[3];
        utils.clearEntityManager(em);

        // when: 친구 등록(회원1이 회원2, 회원3, 회원4와 친구가 되게 함)
        utils.makeFriends(testMember1, testMember2);
        utils.makeFriends(testMember1, testMember3);
        utils.makeFriends(testMember1, testMember4);
        utils.clearEntityManager(em);

        // then: 조건 없이 검색
        Member noConditionSearch = memberService.findById(testMember1.getId());

        List<MemberInfoResponseDto> resultExpected3 = memberService.searchFriendsByCondition(
                noConditionSearch,
                MemberSearchRequestDto.builder().build());
        assertThat(resultExpected3.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("친구 검색: 이름으로 검색 성공")
    void searchFriendship_nameSuccess() {
        // given: 회원 생성
        Member[] testMembers = utils.createTestMembers(4);
        Member testMember1 = testMembers[0];
        Member testMember2 = testMembers[1];
        Member testMember3 = testMembers[2];
        Member testMember4 = testMembers[3];
        utils.clearEntityManager(em);

        // when: 친구 등록(회원1이 회원2, 회원3, 회원4와 친구가 되게 함)
        utils.makeFriends(testMember1, testMember2);
        utils.makeFriends(testMember1, testMember3);
        utils.makeFriends(testMember1, testMember4);
        utils.clearEntityManager(em);

        // case2: 이름으로 검색
        Member nameSearchMember = memberService.findById(testMember1.getId());

        List<MemberInfoResponseDto> resultExpected3 = memberService.searchFriendsByCondition(
                nameSearchMember,
                MemberSearchRequestDto.builder().name("0").build());
        assertThat(resultExpected3.size()).isEqualTo(3);

        List<MemberInfoResponseDto> resultExpected2 = memberService.searchFriendsByCondition(
                nameSearchMember,
                MemberSearchRequestDto.builder().name("2").build());
        assertThat(resultExpected2.size()).isEqualTo(2);

        List<MemberInfoResponseDto> resultExpected1 = memberService.searchFriendsByCondition(
                nameSearchMember,
                MemberSearchRequestDto.builder().name("3").build());
        assertThat(resultExpected1.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("친구 검색: 이메일로 검색 성공")
    void searchFriendship_emailSuccess() {
        // given: 회원 생성
        Member[] testMembers = utils.createTestMembers(4);
        Member testMember1 = testMembers[0];
        Member testMember2 = testMembers[1];
        Member testMember3 = testMembers[2];
        Member testMember4 = testMembers[3];
        utils.clearEntityManager(em);

        // when: 친구 등록(회원1이 회원2, 회원3, 회원4와 친구가 되게 함)
        utils.makeFriends(testMember1, testMember2);
        utils.makeFriends(testMember1, testMember3);
        utils.makeFriends(testMember1, testMember4);
        utils.clearEntityManager(em);

        // case3: 이메일로 검색
        Member emailSearchMember = memberService.findById(testMember1.getId());

        List<MemberInfoResponseDto> resultExpected3 = memberService.searchFriendsByCondition(
                emailSearchMember,
                MemberSearchRequestDto.builder().email("0").build());
        assertThat(resultExpected3.size()).isEqualTo(3);

        List<MemberInfoResponseDto> resultExpected2 = memberService.searchFriendsByCondition(
                emailSearchMember,
                MemberSearchRequestDto.builder().email("2").build());
        assertThat(resultExpected2.size()).isEqualTo(2);

        List<MemberInfoResponseDto> resultExpected1 = memberService.searchFriendsByCondition(
                emailSearchMember,
                MemberSearchRequestDto.builder().email("3").build());
        assertThat(resultExpected1.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("친구 검색: 모든 조건(이름, 이메일)으로 검색 성공")
    void searchFriendship_allConditionSuccess() {
        // given: 회원 생성
        Member[] testMembers = utils.createTestMembers(4);
        Member testMember1 = testMembers[0];
        Member testMember2 = testMembers[1];
        Member testMember3 = testMembers[2];
        Member testMember4 = testMembers[3];
        utils.clearEntityManager(em);

        // when: 친구 등록(회원1이 회원2, 회원3, 회원4와 친구가 되게 함)
        utils.makeFriends(testMember1, testMember2);
        utils.makeFriends(testMember1, testMember3);
        utils.makeFriends(testMember1, testMember4);
        utils.clearEntityManager(em);

        // case4: 이름과 이메일로 검색
        Member nameAndEmailSearchMember = memberService.findById(testMember1.getId());

        List<MemberInfoResponseDto> resultExpected3 = memberService.searchFriendsByCondition(
                nameAndEmailSearchMember,
                MemberSearchRequestDto.builder().email("0").name("0").build());
        assertThat(resultExpected3.size()).isEqualTo(3);

        List<MemberInfoResponseDto> resultExpected1 = memberService.searchFriendsByCondition(
                nameAndEmailSearchMember,
                MemberSearchRequestDto.builder().email("2").name("3").build());
        assertThat(resultExpected1.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("차단 생성: 성공")
    void block_success() {
        // given: 회원 생성
        Member signUpMember1 = utils.createTestMember("1", "A");
        Member signUpMember2 = utils.createTestMember("2", "B");

        // when: 회원 차단
        memberService.addBlacklist(signUpMember1, MemberInfoRequestDto.builder()
                .email(signUpMember2.getEmail())
                .build());
        utils.clearEntityManager(em);

        // then: 차단 목록 조회
        Member member1 = memberService.findById(signUpMember1.getId());
        Member member2 = memberService.findById(signUpMember2.getId());
        assertThat(member1.getBlacklists().stream()
                .anyMatch(blacklist -> blacklist.getTargetMember().equals(member2)))
                .isTrue();
        assertThat(member2.getBlockedLists().stream()
                .anyMatch(blacklist -> blacklist.getTargetMember().equals(member2)))
                .isTrue();
    }

    @Test
    @DisplayName("차단 생성: 존재하지 않는 회원 예외")
    void block_notExistsMember() {
        // given: 회원 생성
        Member member = utils.createTestMember();

        // then: 차단 생성 실패
        assertThatThrownBy(() -> memberService.addBlacklist(member, MemberInfoRequestDto.builder()
                .email(NOT_EXISTS_EMAIL)
                .build()))
                .isInstanceOf(MemberNotExistsException.class);
    }

    @Test
    @DisplayName("차단 생성: 자기 자신에게 요청 예외")
    void block_myself() {
        // given: 회원 생성
        Member member = utils.createTestMember();

        // then: 자기 자신 차단 실패
        assertThatThrownBy(() -> memberService.addBlacklist(member, MemberInfoRequestDto.builder()
                .email(member.getEmail())
                .build()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("차단 삭제: 성공")
    void removeBlock_success() {
        // given: 회원 생성 및 차단 생성
        Member signUpMember1 = utils.createTestMember("1", "A");
        Member signUpMember2 = utils.createTestMember("2", "B");

        memberService.addBlacklist(signUpMember1, MemberInfoRequestDto.builder()
                .email(signUpMember2.getEmail())
                .build());
        utils.clearEntityManager(em);

        // when: 차단 삭제
        Member removeMember = memberService.findById(signUpMember1.getId());
        memberService.removeBlacklist(removeMember, MemberInfoRequestDto.builder()
                .email(signUpMember2.getEmail())
                .build());
        utils.clearEntityManager(em);

        // then: 차단 목록 조회 실패
        Member member1 = memberService.findById(signUpMember1.getId());
        assertThat(member1.getBlacklists().isEmpty())
                .isTrue();
    }
}