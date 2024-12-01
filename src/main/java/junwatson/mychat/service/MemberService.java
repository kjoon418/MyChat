package junwatson.mychat.service;

import junwatson.mychat.domain.Blacklist;
import junwatson.mychat.domain.Friendship;
import junwatson.mychat.domain.FriendshipRequest;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.type.MemberAuthorizationType;
import junwatson.mychat.dto.request.*;
import junwatson.mychat.dto.response.MemberInfoResponseDto;
import junwatson.mychat.dto.response.ReissueAccessTokenResponseDto;
import junwatson.mychat.dto.response.TokenDto;
import junwatson.mychat.exception.*;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.repository.ChatRoomRepository;
import junwatson.mychat.repository.MemberRepository;
import junwatson.mychat.repository.condition.MemberSearchCondition;
import junwatson.mychat.repository.dao.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberService {

    private final HashSet<Character> allowedWords = new HashSet<>(Set.of('!', '@', '#', '$', '%', '^', '&', '~', '.'));
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatDao chatDao;
    private final BlacklistDao blacklistDao;
    private final RefreshTokenDao refreshTokenDao;
    private final FriendshipDao friendshipDao;
    private final FriendshipRequestDao friendshipRequestDao;

    public TokenDto signUp(MemberSignUpRequestDto requestDto) {
        log.info("MemberService.signUp() called");

        Member member = requestDto.toEntity();

        // 유효성 검사
        if (!validate(member)) {
            throw new IllegalMemberStateException("이메일이 중복되거나, 이름 혹은 이메일의 형식이 부적절합니다.");
        }

        // 회원가입
        memberRepository.save(member);
        String accessTokenString = tokenProvider.createAccessToken(member);
        String refreshTokenString = refreshTokenDao.createRefreshToken(member).getToken();

        return TokenDto.builder()
                .accessToken(accessTokenString)
                .refreshToken(refreshTokenString)
                .build();
    }

    public TokenDto signIn(MemberSignInRequestDto requestDto) {
        log.info("MemberService.signIn() called");

        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        // 유효성 검사
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("부적절한 이메일 혹은 비밀번호입니다.");
        }
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new MemberNotExistsException("이메일과 비밀번호를 다시 확인해주세요"));
        if (!member.getPassword().equals(requestDto.getPassword())) {
            throw new MemberNotExistsException("이메일과 비밀번호를 다시 확인해주세요");
        }

        // 토큰 발급
        String accessTokenString = tokenProvider.createAccessToken(member);
        String refreshTokenString = refreshTokenDao.createRefreshToken(member).getToken();

        return TokenDto.builder()
                .accessToken(accessTokenString)
                .refreshToken(refreshTokenString)
                .build();
    }

    public void logout(Member member) {
        log.info("MemberService.logout() called");

        refreshTokenDao.removeRefreshToken(member);
    }

    public MemberInfoResponseDto integrate(Member member, MemberIntegrationRequestDto requestDto) {
        log.info("MemberService.integrate() called");

        memberRepository.updatePassword(member, requestDto.getPassword());

        return MemberInfoResponseDto.from(member);
    }

    public MemberInfoResponseDto withdrawMembership(Member member) {
        log.info("MemberService.withdrawMembership() called");

        // 관련 정보를 전부 삭제
        friendshipDao.removeAllFriendships(member);
        chatRoomRepository.leaveAllChatRooms(member);
        chatDao.removeAllChats(member);

        // 회원을 삭제
        Member deletedMember = memberRepository.delete(member);

        return MemberInfoResponseDto.from(deletedMember);
    }

    public MemberInfoResponseDto updateMember(Member member, MemberModificationRequestDto requestDto) {
        log.info("MemberService.updateMember()");

        String email = requestDto.getEmail();
        String password = requestDto.getPassword();
        String name = requestDto.getName();
        String profileUrl = requestDto.getProfileUrl();

        // 유효성 검사
        if (!StringUtils.hasText(email) &&
                !StringUtils.hasText(password) &&
                !StringUtils.hasText(name) &&
                !StringUtils.hasText(profileUrl)) {
            throw new IllegalArgumentException("아무 정보도 전달되지 않았습니다.");
        }

        // 값이 있을 경우에만 수정하도록 하고, 값이 없다면 해당 정보에 대한 수정을 원치 않는것으로 판단함
        if (StringUtils.hasText(email)) {
            // 구글 회원의 이메일은 수정할 수 없게 함
            if (member.getAuthorizedBy() == MemberAuthorizationType.GOOGLE) {
                throw new IllegalMemberStateException("구글을 통해 회원가입한 회원은 이메일을 수정할 수 없습니다.");
            }
            if (isIllegalString(email) || memberRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("형식이 부적절하거나 이미 사용되고 있는 이메일입니다.");
            }
            memberRepository.updateEmail(member, email);
        }
        if (StringUtils.hasText(password)) {
            if (isIllegalString(password)) {
                throw new IllegalArgumentException("부적절한 비밀번호입니다.");
            }
            memberRepository.updatePassword(member, password);
        }
        if (StringUtils.hasText(name)) {
            if (isIllegalString(name)) {
                throw new IllegalArgumentException("부적절한 이름입니다.");
            }
            memberRepository.updateName(member, name);
        }
        if (StringUtils.hasText(profileUrl)) {
            memberRepository.updateProfileUrl(member, profileUrl);
        }

        return MemberInfoResponseDto.from(member);
    }

    @Transactional(readOnly = true)
    public Member findById(Long memberId) {
        log.info("MemberService.findById() called");

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotExistsException("해당 ID를 지닌 회원이 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public ReissueAccessTokenResponseDto reissueAccessToken(String refreshTokenString) {
        log.info("MemberService.reissueAccessToken() called");

        // 유효성 검사
        Member member = memberRepository.findById(Long.parseLong(tokenProvider.parseClaims(refreshTokenString).getSubject()))
                .orElseThrow(() -> new MemberNotExistsException("해당 토큰으로 회원을 조회할 수 없습니다."));
        if (!refreshTokenDao.isValidateRefreshToken(member, refreshTokenString)) {
            throw new IllegalRefreshTokenException("부적절한 리프레시 토큰입니다.");
        }

        // 엑세스 토큰 생성
        String accessTokenString = tokenProvider.createAccessToken(member);

        return ReissueAccessTokenResponseDto.from(accessTokenString);
    }

    public MemberInfoResponseDto createFriendshipRequest(Member member, MemberInfoRequestDto requestDto) {
        log.info("MemberService.createFriendship() called");

        String friendEmail = requestDto.getEmail();

        // 유효성 검사
        Member friend = memberRepository.findByEmail(friendEmail)
                .orElseThrow(() -> new MemberNotExistsException("해당 이메일을 지닌 회원이 존재하지 않습니다."));
        if (blacklistDao.isBlocked(member, friend)) {
            throw new BlockException("나를 차단한 회원에게는 친구 요청을 보낼 수 없습니다.");
        }
        if (friend == member) {
            throw new IllegalArgumentException("본인에게는 친구 요청을 보낼 수 없습니다.");
        }

        // 차단한 상대를 친구 추가하려 할 경우, 차단을 해제한다
        if (blacklistDao.isBlacklistExists(member, friend)) {
            blacklistDao.removeBlacklist(member, friend);
        }

        // 이미 상대 측에게로부터 친구 요청이 와 있었다면, 둘을 친구로 등록한다
        if (friendshipRequestDao.isReceivedFriendshipRequestExists(member, friend)) {
            friendshipRequestDao.removeFriendshipRequest(friend, member);
            friendshipDao.createFriendship(member, friend);
        } else {
            friendshipRequestDao.createFriendshipRequest(member, friend);
        }

        return MemberInfoResponseDto.from(friend);
    }

    public MemberInfoResponseDto removeFriendship(Member member, MemberInfoRequestDto requestDto) {
        log.info("MemberService.removeFriendship() called");

        String email = requestDto.getEmail();

        // 유효성 검사
        Member friend = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotExistsException("대상 회원과 친구가 아닙니다."));

        // 친구 삭제
        friendshipDao.removeFriendship(member, friend);

        return MemberInfoResponseDto.from(friend);
    }

    public void rejectFriendshipRequest(Member member, MemberInfoRequestDto requestDto) {
        log.info("MemberService.rejectFriendshipRequest()");

        // 유효성 검사
        Member friend = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new MemberNotExistsException("해당 회원이 존재하지 않습니다."));
        if (!friendshipRequestDao.isReceivedFriendshipRequestExists(member, friend)) {
            throw new IllegalMemberStateException("해당 회원으로부터의 친구 요청이 존재하지 않습니다.");
        }

        // 상대로부터 온 친구 요청 삭제(거절)
        friendshipRequestDao.removeFriendshipRequest(friend, member);
    }

    public List<MemberInfoResponseDto> findAllFriends(Member member) {
        log.info("MemberService.findAllFriends() called");

        // 조건 없이 모든 친구 조회
        List<Friendship> friendships = friendshipDao.searchFriendships(member, MemberSearchCondition.noCondition());

        return friendships.stream()
                .map(Friendship::getFriendMember)
                .map(MemberInfoResponseDto::from)
                .toList();
    }

    public List<MemberInfoResponseDto> searchFriendsByCondition(Member member, SearchMemberRequestDto requestDto) {
        log.info("MemberService.searchFriendsByCondition() called");

        // 조건에 부합하는 친구 검색
        List<Friendship> friendships = friendshipDao.searchFriendships(member, requestDto.toCondition());

        return friendships.stream()
                .map(Friendship::getFriendMember)
                .map(MemberInfoResponseDto::from)
                .toList();
    }

    public List<MemberInfoResponseDto> searchMembersByCondition(Member member, SearchMemberRequestDto requestDto) {
        log.info("MemberService.searchMembersByCondition() called");

        MemberSearchCondition condition = requestDto.toCondition();

        // 유효성 검사(전체 회원은 조건 없이 검색하지 못하도록 함)
        if (!StringUtils.hasText(condition.getEmail()) && !StringUtils.hasText(condition.getName())) {
            throw new IllegalSearchConditionException("조건 없이 검색할 수 없습니다.");
        }

        // 조건에 부합하는 회원 검색
        List<Member> members = memberRepository.searchMembers(member, condition);

        return members.stream()
                .map(MemberInfoResponseDto::from)
                .toList();
    }

    public List<MemberInfoResponseDto> findSentFriendshipRequests(Member member) {
        log.info("MemberService.findSentFriendshipRequests() called");

        // 보낸 친구 요청 조회
        List<FriendshipRequest> sentFriendshipRequests = member.getSentFriendshipRequests();

        return sentFriendshipRequests.stream()
                .map((friendshipRequest) -> MemberInfoResponseDto.from(friendshipRequest.getResponseMember()))
                .toList();
    }

    public List<MemberInfoResponseDto> findReceivedFriendshipRequests(Member member) {
        log.info("MemberService.findReceivedFriendshipRequests() called");

        // 받은 친구 요청 조회
        List<FriendshipRequest> receivedFriendshipRequests = member.getReceivedFriendshipRequests();

        return receivedFriendshipRequests.stream()
                .map((friendshipRequest) -> MemberInfoResponseDto.from(friendshipRequest.getRequestMember()))
                .toList();
    }

    public MemberInfoResponseDto addBlacklist(Member member, MemberInfoRequestDto requestDto) {
        log.info("MemberService.addBlacklist() called");

        String targetEmail = requestDto.getEmail();

        // 유효성 감사
        Member target = memberRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new MemberNotExistsException("대상 회원이 존재하지 않습니다."));
        if (member == target) {
            throw new IllegalArgumentException("본인은 차단할 수 없습니다.");
        }

        // 차단 데이터 생성
        Blacklist blacklist = blacklistDao.createBlacklist(member, target);

        // 만약 기존에 친구 관계였다면, 친구 관계를 삭제함
        if (friendshipDao.areFriends(member, target)) {
            friendshipDao.removeFriendship(member, target);
        }

        // 만약 기존에 친구 요청을 보냈었다면, 친구 요청을 삭제함
        if (friendshipRequestDao.isSentFriendshipRequestExists(member, target)) {
            friendshipRequestDao.removeFriendshipRequest(member, target);
        }

        return MemberInfoResponseDto.from(blacklist.getTargetMember());
    }

    public MemberInfoResponseDto removeBlacklist(Member member, MemberInfoRequestDto requestDto) {
        log.info("MemberService.removeBlacklist() called");

        String targetEmail = requestDto.getEmail();

        // 유효성 검사
        Member target = memberRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new MemberNotExistsException("대상 회원이 존재하지 않습니다."));

        // 차단 데이터 삭제
        Blacklist blacklist = blacklistDao.removeBlacklist(member, target);

        return MemberInfoResponseDto.from(blacklist.getTargetMember());
    }

    /**
     * 해당 정보로 회원가입이 가능한지 여부를 반환하는 메서드
     */
    private boolean validate(Member member) {
        log.info("MemberService.validate() called");

        String email = member.getEmail();
        String password = member.getPassword();
        String name = member.getName();

        // 이미 사용중인 이메일이라면 false 반환
        if (memberRepository.findByEmail(email).isPresent()) {
            return false;
        }

        // 비어있는 값이 있다면 false 반환
        if (!StringUtils.hasText(email) ||
                !StringUtils.hasText(password) ||
                !StringUtils.hasText(name)) {
            return false;
        }

        // 이메일 혹은 비밀번호에 허용하지 않은 단어가 들어갔다면 false 반환
        if (isIllegalString(email) || isIllegalString(password)) {
            return false;
        }

        return !name.contains(" ");
    }

    /**
     * 파라미터로 전달된 문자열에, 허용되지 않은 문자가 포함되어 있는지 여부를 반환하는 메서드
     */
    private boolean isIllegalString(String string) {
        log.info("MemberService.isIllegalString() called");

        for (int i = 0; i < string.length(); i++) {
            char word = string.charAt(i);
            if (Character.isAlphabetic(word) || Character.isDigit(word)) {
                continue;
            }
            if (!allowedWords.contains(word)) {
                return true;
            }
        }

        return false;
    }
}
