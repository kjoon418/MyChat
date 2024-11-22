package junwatson.mychat.service;

import jakarta.servlet.http.HttpServletRequest;
import junwatson.mychat.domain.Friendship;
import junwatson.mychat.domain.FriendshipRequest;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.*;
import junwatson.mychat.dto.response.MemberInfoResponseDto;
import junwatson.mychat.dto.response.TokenDto;
import junwatson.mychat.dto.response.CreateFriendshipResponseDto;
import junwatson.mychat.dto.response.ReissueAccessTokenResponseDto;
import junwatson.mychat.exception.IllegalMemberStateException;
import junwatson.mychat.exception.IllegalSearchConditionException;
import junwatson.mychat.exception.MemberNotExistsException;
import junwatson.mychat.jwt.TokenProvider;
import junwatson.mychat.repository.MemberRepository;
import junwatson.mychat.repository.condition.MemberSearchCondition;
import junwatson.mychat.repository.dao.FriendshipRequestDao;
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

    public ReissueAccessTokenResponseDto reissueAccessToken(HttpServletRequest request) {
        log.info("MemberService.reissueAccessToken() called");

        String token = memberRepository.reissueAccessToken(request);

        return ReissueAccessTokenResponseDto.from(token);
    }

    public CreateFriendshipResponseDto createFriendship(CreateFriendshipRequestDto requestDto, Member member) {
        log.info("MemberService.createFriendship() called");

        String friendEmail = requestDto.getEmail();

        Member friend = memberRepository.findByEmail(friendEmail)
                .orElseThrow(() -> new MemberNotExistsException("해당 이메일을 지닌 회원이 존재하지 않습니다."));

        // 이미 상대 측에게로부터 친구 요청이 와 있었다면, 둘을 친구로 등록한다
        if (memberRepository.isExistFriendshipRequest(member, friend)) {
            memberRepository.removeFriendshipRequest(friend, member);
            memberRepository.createFriendship(member, friend);
        } else {
            memberRepository.createFriendshipRequest(member, friend);
        }

        return CreateFriendshipResponseDto.from(friend);
    }

    public List<MemberInfoResponseDto> findAllFriend(Member member) {
        log.info("MemberService.findAllFriend() called");

        List<Friendship> friendships = memberRepository.searchFriendship(member, MemberSearchCondition.noCondition());

        return friendships.stream()
                .map(Friendship::getFriendMember)
                .map(MemberInfoResponseDto::from)
                .toList();
    }

    public List<MemberInfoResponseDto> searchFriendByCondition(Member member, SearchFriendRequestDto requestDto) {
        log.info("MemberService.searchFriendByCondition() called");

        List<Friendship> friendships = memberRepository.searchFriendship(member, requestDto.toCondition());

        return friendships.stream()
                .map(Friendship::getFriendMember)
                .map(MemberInfoResponseDto::from)
                .toList();
    }

    public List<MemberInfoResponseDto> searchMemberByCondition(Member requestMember, SearchMemberRequestDto requestDto) {
        log.info("MemberService.searchMemberByCondition() called");

        MemberSearchCondition condition = requestDto.toCondition();

        // 전체 회원 조회는 조건 없이 검색하지 못하도록 함
        if (!StringUtils.hasText(condition.getEmail()) && !StringUtils.hasText(condition.getName())) {
            throw new IllegalSearchConditionException("회원은 조건 없이 검색할 수 없습니다.");
        }

        List<Member> members = memberRepository.searchMember(requestMember, condition);

        return members.stream()
                .map(MemberInfoResponseDto::from)
                .toList();
    }

    public List<MemberInfoResponseDto> findSentFriendshipRequest(Member member) {
        log.info("MemberService.findSentFriendshipRequest() called");

        List<FriendshipRequest> sentFriendshipRequest = memberRepository.findSentFriendshipRequest(member);

        return sentFriendshipRequest.stream()
                .map((friendshipRequest) -> MemberInfoResponseDto.from(friendshipRequest.getResponseMember()))
                .toList();
    }

    public List<MemberInfoResponseDto> findReceivedFriendshipRequest(Member member) {
        log.info("MemberService.findReceivedFriendshipRequest() called");

        List<FriendshipRequest> sentFriendshipRequest = memberRepository.findReceivedFriendshipRequest(member);

        return sentFriendshipRequest.stream()
                .map((friendshipRequest) -> MemberInfoResponseDto.from(friendshipRequest.getRequestMember()))
                .toList();
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
