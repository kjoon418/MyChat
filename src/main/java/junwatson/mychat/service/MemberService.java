package junwatson.mychat.service;

import jakarta.servlet.http.HttpServletRequest;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.CreateFriendshipRequestDto;
import junwatson.mychat.dto.response.CreateFriendshipResponseDto;
import junwatson.mychat.dto.response.ReissueAccessTokenResponseDto;
import junwatson.mychat.exception.MemberNotExistsException;
import junwatson.mychat.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;

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
}
