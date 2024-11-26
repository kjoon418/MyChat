package junwatson.mychat.controller;

import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.ChatCreateRequestDto;
import junwatson.mychat.dto.response.ChatInfoResponseDto;
import junwatson.mychat.service.ChatRoomService;
import junwatson.mychat.service.ChatService;
import junwatson.mychat.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

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
        ChatInfoResponseDto responseDto = chatService.createChat(member, requestDto);

        return ResponseEntity.status(CREATED).body(responseDto);
    }
}
