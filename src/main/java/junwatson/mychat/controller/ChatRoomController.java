package junwatson.mychat.controller;

import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.*;
import junwatson.mychat.dto.response.ChatRoomInfoResponseDto;
import junwatson.mychat.dto.response.MemberInfoResponseDto;
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
    private final ControllerUtil util;

    @PostMapping
    public ResponseEntity<ChatRoomInfoResponseDto> createChatRoom(@RequestBody ChatRoomCreateRequestDto requestDto, Principal principal) {
        log.info("ChatRoomController.createChatRoom() called");

        Member member = util.findMemberByPrincipal(principal);
        ChatRoomInfoResponseDto responseDto = chatRoomService.createChatRoom(member, requestDto);

        return ResponseEntity.status(CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomInfoResponseDto>> findChatRooms(Principal principal) {
        log.info("ChatRoomController.findChatRooms() called");

        Member member = util.findMemberByPrincipal(principal);
        List<ChatRoomInfoResponseDto> responseDto = chatRoomService.findChatRooms(member);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ChatRoomInfoResponseDto>> searchChatRoom(@RequestBody ChatRoomSearchRequestDto requestDto, Principal principal) {
        log.info("ChatRoomController.searchChatRoom() called");

        Member member = util.findMemberByPrincipal(principal);
        List<ChatRoomInfoResponseDto> responseDto = chatRoomService.searchChatRooms(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping
    public ResponseEntity<ChatRoomInfoResponseDto> leaveChatRoom(@RequestBody ChatRoomInfoRequestDto requestDto, Principal principal) {
        log.info("ChatRoomController.leaveChatRoom() called");

        Member member = util.findMemberByPrincipal(principal);
        ChatRoomInfoResponseDto responseDto = chatRoomService.leaveChatRoom(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/invitation")
    public ResponseEntity<ChatRoomInfoResponseDto> inviteChatRoom(@RequestBody ChatRoomInviteRequestDto requestDto, Principal principal) {
        log.info("ChatRoomController.inviteChatRoom() called");

        Member member = util.findMemberByPrincipal(principal);
        ChatRoomInfoResponseDto responseDto = chatRoomService.inviteChatRoom(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberInfoResponseDto>> findMembersByChatRoom(@RequestBody ChatRoomInfoRequestDto requestDto, Principal principal) {
        log.info("ChatRoomController.findMembersByChatRoom() called");

        Member member = util.findMemberByPrincipal(principal);
        ChatRoom chatRoom = chatRoomService.findChatRoomById(requestDto.getId());
        List<MemberInfoResponseDto> responseDto = chatRoomService.findMembersInChatRoom(member, chatRoom);

        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping
    public ResponseEntity<ChatRoomInfoResponseDto> modifyChatRoom(@RequestBody ChatRoomModificationRequestDto requestDto, Principal principal) {
        log.info("ChatRoomController.modifyChatRoom() called");

        Member member = util.findMemberByPrincipal(principal);
        ChatRoomInfoResponseDto responseDto = chatRoomService.modifyChatRoom(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception exception) {
        return MyChatExceptionHandler.handle(exception);
    }
}
