package junwatson.mychat.controller;

import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.*;
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
        List<MemberInfoResponseDto> responseDto = memberService.findAllFriends(member);

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/friend")
    public ResponseEntity<MemberInfoResponseDto> removeFriend(@RequestBody MemberInfoRequestDto requestDto, Principal principal) {
        log.info("MemberController.removeFriend() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        MemberInfoResponseDto responseDto = memberService.removeFriendship(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/friend/search")
    public ResponseEntity<List<MemberInfoResponseDto>> searchFriends(@RequestBody SearchFriendRequestDto requestDto, Principal principal) {
        log.info("MemberController.searchFriends() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        List<MemberInfoResponseDto> responseDto = memberService.searchFriendsByCondition(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/friend/sent")
    public ResponseEntity<List<MemberInfoResponseDto>> findSentFriendRequests(Principal principal) {
        log.info("MemberController.findSentFriendRequest() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        List<MemberInfoResponseDto> responseDto = memberService.findSentFriendshipRequests(member);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/friend/received")
    public ResponseEntity<List<MemberInfoResponseDto>> findReceivedFriendRequests(Principal principal) {
        log.info("MemberController.findReceivedFriendRequest() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        List<MemberInfoResponseDto> responseDto = memberService.findReceivedFriendshipRequests(member);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MemberInfoResponseDto>> searchMembers(@RequestBody SearchMemberRequestDto requestDto, Principal principal) {
        log.info("MemberController.searchMember() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        List<MemberInfoResponseDto> responseDto = memberService.searchMembersByCondition(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/blacklist")
    public ResponseEntity<MemberInfoResponseDto> addBlacklist(@RequestBody BlacklistInfoRequestDto requestDto, Principal principal) {
        log.info("MemberController.createBlacklist() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        MemberInfoResponseDto responseDto = memberService.addBlacklist(member, requestDto);

        return ResponseEntity.status(CREATED).body(responseDto);
    }

    @DeleteMapping("/blacklist")
    public ResponseEntity<MemberInfoResponseDto> deleteBlacklist(@RequestBody BlacklistInfoRequestDto requestDto, Principal principal) {
        log.info("MemberController.deleteBlacklist() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        MemberInfoResponseDto responseDto = memberService.removeBlacklist(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }
}
