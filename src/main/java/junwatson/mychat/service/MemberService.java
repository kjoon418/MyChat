package junwatson.mychat.service;

import jakarta.servlet.http.HttpServletRequest;
import junwatson.mychat.domain.Blacklist;
import junwatson.mychat.domain.Friendship;
import junwatson.mychat.domain.FriendshipRequest;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.type.MemberAuthorizationType;
import junwatson.mychat.dto.request.*;
import junwatson.mychat.dto.response.MemberInfoResponseDto;
import junwatson.mychat.dto.response.TokenDto;
import junwatson.mychat.dto.response.ReissueAccessTokenResponseDto;
import junwatson.mychat.exception.BlockException;
import junwatson.mychat.exception.IllegalMemberStateException;
import junwatson.mychat.exception.IllegalSearchConditionException;
import junwatson.mychat.exception.MemberNotExistsException;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.repository.MemberRepository;
import junwatson.mychat.repository.condition.MemberSearchCondition;
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

    private final MemberRepository memberRepository;
    private final HashSet<Character> allowedWords = new HashSet<>(Set.of('!', '@', '#', '$', '%', '^', '&', '~', '.'));
    private final TokenProvider tokenProvider;

    public TokenDto signUp(MemberSignUpRequestDto requestDto) {
        log.info("MemberService.signUp() called");

        Member member = requestDto.toEntity();
        if (!validate(member)) {
            throw new IllegalMemberStateException("이메일이 중복되거나, 이름 혹은 이메일의 형식이 부적절합니다.");
        }
        memberRepository.save(member);
        String accessToken = tokenProvider.createAccessToken(member);
        String refreshToken = memberRepository.createRefreshToken(member);

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public TokenDto signIn(MemberSignInRequestDto requestDto) {
        log.info("MemberService.signIn() called");

        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new MemberNotExistsException("이메일과 비밀번호를 다시 확인해주세요"));

        if (!member.getPassword().equals(requestDto.getPassword())) {
            throw new MemberNotExistsException("이메일과 비밀번호를 다시 확인해주세요");
        }

        String accessToken = tokenProvider.createAccessToken(member);
        String refreshToken = memberRepository.createRefreshToken(member);

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional(readOnly = true)
    public Member findById(Long memberId) {
        log.info("MemberService.findById() called");

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotExistsException("해당 ID를 지닌 회원이 존재하지 않습니다."));
    }

    public MemberInfoResponseDto updateMember(Member member, MemberModificationRequestDto requestDto) {
        log.info("MemberService.updateMember()");

        // Google 인증을 통한 회원은 수정할 수 없게 함
        if (member.getAuthorizedBy() == MemberAuthorizationType.GOOGLE) {
            throw new IllegalMemberStateException("구글 회원의 정보는 수정할 수 없습니다.");
        }

        String email = requestDto.getEmail();
        String password = requestDto.getPassword();
        String name = requestDto.getName();
        String profileUrl = requestDto.getProfileUrl();
        boolean updated = false;

        if (StringUtils.hasText(email)) {
            if (isIllegalString(email) || memberRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("형식이 부적절하거나 이미 사용되고 있는 이메일입니다.");
            }
            memberRepository.updateEmail(member, email);
            updated = true;
        }
        if (StringUtils.hasText(password)) {
            if (isIllegalString(password)) {
                throw new IllegalArgumentException("부적절한 비밀번호입니다.");
            }
            memberRepository.updatePassword(member, password);
            updated = true;
        }
        if (StringUtils.hasText(name)) {
            if (isIllegalString(name)) {
                throw new IllegalArgumentException("부적절한 이름입니다.");
            }
            memberRepository.updateName(member, name);
            updated = true;
        }
        if (StringUtils.hasText(profileUrl)) {
            memberRepository.updateProfileUrl(member, profileUrl);
            updated = true;
        }

        if (!updated) {
            throw new IllegalArgumentException("아무 정보도 전달되지 않았습니다.");
        }

        return MemberInfoResponseDto.from(member);
    }

    public ReissueAccessTokenResponseDto reissueAccessToken(HttpServletRequest request) {
        log.info("MemberService.reissueAccessToken() called");

        String token = memberRepository.reissueAccessToken(request);

        return ReissueAccessTokenResponseDto.from(token);
    }

    public MemberInfoResponseDto createFriendship(MemberInfoRequestDto requestDto, Member member) {
        log.info("MemberService.createFriendship() called");

        String friendEmail = requestDto.getEmail();

        Member friend = memberRepository.findByEmail(friendEmail)
                .orElseThrow(() -> new MemberNotExistsException("해당 이메일을 지닌 회원이 존재하지 않습니다."));

        // 상대로부터 차단당했다면 친구 요청을 보낼 수 없게 한다
        if (memberRepository.isBlocked(member, friend)) {
            throw new BlockException("나를 차단한 회원에게는 친구 요청을 보낼 수 없습니다.");
        }

        // 차단한 상대를 친구 추가하려 할 경우, 차단을 해제한다
        if (memberRepository.isBlacklistExists(member, friend)) {
            memberRepository.removeBlacklist(member, friend);
        }

        // 이미 상대 측에게로부터 친구 요청이 와 있었다면, 둘을 친구로 등록한다
        if (memberRepository.isReceivedFriendshipRequestExists(member, friend)) {
            memberRepository.removeFriendshipRequest(friend, member);
            memberRepository.createFriendship(member, friend);
        } else {
            memberRepository.createFriendshipRequest(member, friend);
        }

        return MemberInfoResponseDto.from(friend);
    }

    public MemberInfoResponseDto removeFriendship(Member member, MemberInfoRequestDto requestDto) {
        log.info("MemberService.removeFriendship() called");

        String email = requestDto.getEmail();
        Member friend = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotExistsException("대상 회원과 친구가 아닙니다."));

        memberRepository.removeFriendship(member, friend);

        return MemberInfoResponseDto.from(friend);
    }

    public void rejectFriendshipRequest(Member member, MemberInfoRequestDto requestDto) {
        log.info("MemberService.rejectFriendshipRequest()");

        Member friend = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new MemberNotExistsException("해당 회원이 존재하지 않습니다."));

        System.out.println("member.getEmail() = " + member.getEmail());
        System.out.println("friend.getEmail() = " + friend.getEmail());

        if (!memberRepository.isReceivedFriendshipRequestExists(member, friend)) {
            throw new IllegalMemberStateException("해당 회원으로부터의 친구 요청이 존재하지 않습니다.");
        }

        memberRepository.removeFriendshipRequest(friend, member);
    }

    public List<MemberInfoResponseDto> findAllFriends(Member member) {
        log.info("MemberService.findAllFriend() called");

        List<Friendship> friendships = memberRepository.searchFriendships(member, MemberSearchCondition.noCondition());

        return friendships.stream()
                .map(Friendship::getFriendMember)
                .map(MemberInfoResponseDto::from)
                .toList();
    }

    public List<MemberInfoResponseDto> searchFriendsByCondition(Member member, SearchMemberRequestDto requestDto) {
        log.info("MemberService.searchFriendByCondition() called");

        List<Friendship> friendships = memberRepository.searchFriendships(member, requestDto.toCondition());

        return friendships.stream()
                .map(Friendship::getFriendMember)
                .map(MemberInfoResponseDto::from)
                .toList();
    }

    public List<MemberInfoResponseDto> searchMembersByCondition(Member member, SearchMemberRequestDto requestDto) {
        log.info("MemberService.searchMemberByCondition() called");

        MemberSearchCondition condition = requestDto.toCondition();

        // 전체 회원 조회는 조건 없이 검색하지 못하도록 함
        if (!StringUtils.hasText(condition.getEmail()) && !StringUtils.hasText(condition.getName())) {
            throw new IllegalSearchConditionException("회원은 조건 없이 검색할 수 없습니다.");
        }

        List<Member> members = memberRepository.searchMembers(member, condition);

        return members.stream()
                .map(MemberInfoResponseDto::from)
                .toList();
    }

    public List<MemberInfoResponseDto> findSentFriendshipRequests(Member member) {
        log.info("MemberService.findSentFriendshipRequest() called");

        List<FriendshipRequest> sentFriendshipRequest = memberRepository.findSentFriendshipRequests(member);

        return sentFriendshipRequest.stream()
                .map((friendshipRequest) -> MemberInfoResponseDto.from(friendshipRequest.getResponseMember()))
                .toList();
    }

    public List<MemberInfoResponseDto> findReceivedFriendshipRequests(Member member) {
        log.info("MemberService.findReceivedFriendshipRequest() called");

        List<FriendshipRequest> sentFriendshipRequest = memberRepository.findReceivedFriendshipRequests(member);

        return sentFriendshipRequest.stream()
                .map((friendshipRequest) -> MemberInfoResponseDto.from(friendshipRequest.getRequestMember()))
                .toList();
    }

    public MemberInfoResponseDto addBlacklist(Member member, MemberInfoRequestDto requestDto) {
        log.info("MemberService.addBlacklist() called");

        String targetEmail = requestDto.getEmail();
        Member target = memberRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new MemberNotExistsException("대상 회원이 존재하지 않습니다."));
        Blacklist blacklist = memberRepository.addBlacklist(member, target);

        // 만약 기존에 친구 관계였다면, 친구 관계를 삭제함
        if (memberRepository.areFriends(member, target)) {
            memberRepository.removeFriendship(member, target);
        }
        // 만약 기존에 친구 요청을 보냈었다면, 친구 요청을 삭제함
        if (memberRepository.isSentFriendshipRequestExists(member, target)) {
            memberRepository.removeFriendshipRequest(member, target);
        }

        return MemberInfoResponseDto.from(blacklist.getTargetMember());
    }

    public MemberInfoResponseDto removeBlacklist(Member member, MemberInfoRequestDto requestDto) {
        log.info("MemberService.removeBlacklist() called");

        String targetEmail = requestDto.getEmail();
        Member target = memberRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new MemberNotExistsException("대상 회원이 존재하지 않습니다."));
        Blacklist blacklist = memberRepository.removeBlacklist(member, target);

        return MemberInfoResponseDto.from(blacklist.getTargetMember());
    }

    private boolean validate(Member member) {
        log.info("MemberService.validate() called");

        String email = member.getEmail();
        String password = member.getPassword();
        String name = member.getName();

        if (memberRepository.findByEmail(email).isPresent()) {
            return false;
        }
        if (!StringUtils.hasText(email) ||
                !StringUtils.hasText(password) ||
                !StringUtils.hasText(name)) {
            return false;
        }
        if (isIllegalString(email) || isIllegalString(password)) {
            return false;
        }

        return !name.contains(" ");
    }

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
