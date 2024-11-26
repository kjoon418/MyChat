package junwatson.mychat.service;

import junwatson.mychat.domain.Chat;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import junwatson.mychat.dto.request.ChatCreateRequestDto;
import junwatson.mychat.dto.response.ChatInfoResponseDto;
import junwatson.mychat.exception.ChatRoomNotExistsException;
import junwatson.mychat.exception.IllegalMemberStateException;
import junwatson.mychat.repository.ChatRoomRepository;
import junwatson.mychat.repository.MemberRepository;
import junwatson.mychat.repository.dao.MemberChatRoomDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    public ChatInfoResponseDto createChat(Member member, ChatCreateRequestDto requestDto) {
        log.info("ChatService.createChat() called");

        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getChatRoomId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));
        MemberChatRoom memberChatRoom = chatRoomRepository.findMemberChatRoom(member, chatRoom)
                .orElseThrow(() -> new IllegalMemberStateException("해당 채팅방에 소속되지 않았습니다."));
        Chat chat = requestDto.toEntityWithMemberChatRoom(memberChatRoom);
        memberChatRoom.getChats().add(chat);

        return ChatInfoResponseDto.from(chat);
    }
}
