package junwatson.mychat.controller;

import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.ChatCreateRequestDto;
import junwatson.mychat.dto.request.ChatInfoRequestDto;
import junwatson.mychat.dto.request.ChatRoomInfoRequestDto;
import junwatson.mychat.dto.request.ChatSearchRequestDto;
import junwatson.mychat.dto.response.ChatInfoResponseDto;
import junwatson.mychat.service.ChatRoomService;
import junwatson.mychat.service.ChatService;
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
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final MemberService memberService;
    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatInfoResponseDto> createChat(@RequestBody ChatCreateRequestDto requestDto, Principal principal) {
        log.info("ChatController.createChat() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        ChatInfoResponseDto responseDto = chatService.createUserChat(member, requestDto);

        return ResponseEntity.status(CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<ChatInfoResponseDto>> findChats(@RequestBody ChatRoomInfoRequestDto requestDto, Principal principal) {
        log.info("ChatController.findChats() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        ChatRoom chatRoom = chatRoomService.findChatRoomByDto(requestDto);
        List<ChatInfoResponseDto> responseDto = chatService.readChats(member, chatRoom);

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping
    public ResponseEntity<ChatInfoResponseDto> deleteChat(@RequestBody ChatInfoRequestDto requestDto, Principal principal) {
        log.info("ChatController.deleteChat() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        ChatInfoResponseDto responseDto = chatService.deleteChat(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ChatInfoResponseDto>> searchChats(@RequestBody ChatSearchRequestDto requestDto, Principal principal) {
        log.info("ChatController.searchChats() called");

        long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);
        List<ChatInfoResponseDto> responseDto = chatService.searchChats(member, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handle(RuntimeException exception) {
        return MyChatExceptionHandler.handle(exception);
    }
}
