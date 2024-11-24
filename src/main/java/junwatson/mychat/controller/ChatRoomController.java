package junwatson.mychat.controller;

import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.ChatRoomCreateRequestDto;
import junwatson.mychat.dto.response.ChatRoomInfoResponseDto;
import junwatson.mychat.service.ChatRoomService;
import junwatson.mychat.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

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

}
