package junwatson.mychat.controller;

import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.CreateFriendshipRequestDto;
import junwatson.mychat.dto.response.CreateFriendshipResponseDto;
import junwatson.mychat.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

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
}
