package junwatson.mychat.controller;

import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.CreateFriendshipRequestDto;
import junwatson.mychat.dto.request.SearchFriendsRequestDto;
import junwatson.mychat.dto.response.CreateFriendshipResponseDto;
import junwatson.mychat.dto.response.MemberInfoResponseDto;
import junwatson.mychat.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/friend")
    public ResponseEntity<CreateFriendshipResponseDto> createFriendship(@RequestBody CreateFriendshipRequestDto requestDto, Principal principal) {
        log.info("MemberController.createFriendship() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        CreateFriendshipResponseDto responseDto = memberService.createFriendship(requestDto, member);

        return ResponseEntity.status(CREATED).body(responseDto);
    }

    @GetMapping("/friend")
    public ResponseEntity<List<MemberInfoResponseDto>> findAllFriends(Principal principal) {
        log.info("MemberController.findAllFriends() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        List<MemberInfoResponseDto> responseDto = memberService.searchAllFriends(member);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/friend/search")
    public ResponseEntity<List<MemberInfoResponseDto>> searchFriends(@RequestBody SearchFriendsRequestDto requestDto, Principal principal) {
        log.info("MemberController.searchFriends() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        List<MemberInfoResponseDto> responseDto = memberService.searchFriendsByCondition(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }
}
