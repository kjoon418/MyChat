package junwatson.mychat.controller;

import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.*;
import junwatson.mychat.dto.response.ChatRoomInfoResponseDto;
import junwatson.mychat.service.ChatRoomService;
import junwatson.mychat.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/room")
@Slf4j
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ChatRoomInfoResponseDto> createChatRoom(@RequestBody ChatRoomCreateRequestDto requestDto, Principal principal) {
        log.info("ChatRoomController.createChatRoom() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        ChatRoomInfoResponseDto responseDto = chatRoomService.createChatRoom(member, requestDto);

        return ResponseEntity.status(CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomInfoResponseDto>> findChatRooms(Principal principal) {
        log.info("ChatRoomController.findChatRooms() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        List<ChatRoomInfoResponseDto> responseDto = chatRoomService.findChatRooms(member);

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping
    public ResponseEntity<ChatRoomInfoResponseDto> leaveChatRoom(@RequestBody ChatRoomInfoRequestDto requestDto, Principal principal) {
        log.info("ChatRoomController.leaveChatRoom() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        ChatRoomInfoResponseDto responseDto = chatRoomService.leaveChatRoom(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/invitation")
    public ResponseEntity<ChatRoomInfoResponseDto> inviteChatRoom(@RequestBody ChatRoomInviteRequestDto requestDto, Principal principal) {
        log.info("ChatRoomController.inviteChatRoom() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        ChatRoomInfoResponseDto responseDto = chatRoomService.inviteChatRoom(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping
    public ResponseEntity<ChatRoomInfoResponseDto> modifyChatRoom(@RequestBody ChatRoomModificationRequestDto requestDto, Principal principal) {
        log.info("ChatRoomController.modifyChatRoom() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        ChatRoomInfoResponseDto responseDto = chatRoomService.modifyChatRoom(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }
}
