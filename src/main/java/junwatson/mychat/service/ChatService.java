package junwatson.mychat.service;

import junwatson.mychat.domain.UserChat;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import junwatson.mychat.dto.request.ChatCreateRequestDto;
import junwatson.mychat.dto.response.ChatInfoResponseDto;
import junwatson.mychat.exception.ChatRoomNotExistsException;
import junwatson.mychat.exception.IllegalMemberStateException;
import junwatson.mychat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatInfoResponseDto createChat(Member member, ChatCreateRequestDto requestDto) {
        log.info("ChatService.createChat() called");

        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getChatRoomId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));
        MemberChatRoom memberChatRoom1 = member.getMemberChatRooms().stream()
                .filter(memberChatRoom -> memberChatRoom.getChatRoom().equals(chatRoom))
                .findAny()
                .orElseThrow(() -> new IllegalMemberStateException("해당 채팅방에 소속되어 있지 않습니다."));
        memberChatRoom1.setViewDate(LocalDateTime.now());

        UserChat userChat = requestDto.toEntityWithMemberChatRoom(member, chatRoom);
        member.getUserChats().add(userChat);
        chatRoom.getUserChats().add(userChat);

        return ChatInfoResponseDto.of(userChat, calculateUnconfirmedCounter(userChat, chatRoom));
    }

    public List<ChatInfoResponseDto> readChats(Member member, ChatRoom chatRoom) {
        log.info("ChatService.readChats() called");

        MemberChatRoom memberChatRoom = chatRoomRepository.findMemberChatRoom(member, chatRoom)
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));
        memberChatRoom.setViewDate(LocalDateTime.now());

        return chatRoom.getUserChats().stream()
                .sorted()
                .map(chat -> {
                    int unconfirmedCounter = calculateUnconfirmedCounter(chat, chatRoom);
                    return ChatInfoResponseDto.of(chat, unconfirmedCounter);
                })
                .toList();
    }

    /**
     * 채팅방의 회원 중 몇 명이나 해당 채팅을 읽지 않았는지를 반환하는 메서드
     */
    private int calculateUnconfirmedCounter(UserChat userChat, ChatRoom chatRoom) {
        log.info("ChatService.calculateUnconfirmedCounter() called");

        int count = 0;
        List<LocalDateTime> viewDates = chatRoom.getMemberChatRooms().stream()
                .map(MemberChatRoom::getViewDate)
                .toList();
        for (LocalDateTime viewDate : viewDates) {
            if (userChat.getInputDate().isAfter(viewDate)) {
                count++;
            }
        }

        return count;
    }
}
