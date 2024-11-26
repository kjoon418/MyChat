package junwatson.mychat.service;

import junwatson.mychat.domain.Chat;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import junwatson.mychat.domain.type.ChatType;
import junwatson.mychat.dto.request.ChatCreateRequestDto;
import junwatson.mychat.dto.request.ChatInfoRequestDto;
import junwatson.mychat.dto.request.ChatSearchRequestDto;
import junwatson.mychat.dto.response.ChatInfoResponseDto;
import junwatson.mychat.exception.ChatNotExistsException;
import junwatson.mychat.exception.ChatRoomNotExistsException;
import junwatson.mychat.exception.IllegalChatRoomStateException;
import junwatson.mychat.exception.IllegalMemberStateException;
import junwatson.mychat.repository.ChatRoomRepository;
import junwatson.mychat.repository.condition.ChatSearchCondition;
import junwatson.mychat.repository.dao.ChatDao;
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
    private final ChatDao chatDao;

    public ChatInfoResponseDto createUserChat(Member member, ChatCreateRequestDto requestDto) {
        log.info("ChatService.createChat() called");

        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getChatRoomId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));
        MemberChatRoom memberChatRoom1 = member.getMemberChatRooms().stream()
                .filter(memberChatRoom -> memberChatRoom.getChatRoom().equals(chatRoom))
                .findAny()
                .orElseThrow(() -> new IllegalMemberStateException("해당 채팅방에 소속되어 있지 않습니다."));
        memberChatRoom1.setViewDate(LocalDateTime.now());

        Chat chat = requestDto.toEntityWithMemberChatRoom(member, chatRoom);
        member.getChats().add(chat);
        chatRoom.getChats().add(chat);

        return ChatInfoResponseDto.of(chat, calculateUnconfirmedCounter(chat, chatRoom));
    }

    public ChatInfoResponseDto deleteChat(Member member, ChatInfoRequestDto requestDto) {
        log.info("ChatService.deleteChat() called");

        // 유효성 검사
        Chat chat = chatDao.findChatById(member, requestDto.getChatId())
                .orElseThrow(() -> new ChatNotExistsException("해당 채팅이 존재하지 않습니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getChatRoomId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));
        if (!chatRoom.getChats().contains(chat)) {
            throw new IllegalChatRoomStateException("채팅방에 해당 채팅이 존재하지 않습니다.");
        }

        // 보낸지 5분이 넘은 채팅일 경우, '삭제된 메시지입니다'로 변경
        if (chat.getInputDate().isBefore(LocalDateTime.now().plusMinutes(-5))) {
            chat.setContent("삭제된 메시지입니다.");
        } else {
            chatDao.remove(chat);
        }

        return ChatInfoResponseDto.of(chat, calculateUnconfirmedCounter(chat, chatRoom));
    }

    public List<ChatInfoResponseDto> readChats(Member member, ChatRoom chatRoom) {
        log.info("ChatService.readChats() called");

        MemberChatRoom memberChatRoom = chatRoomRepository.findMemberChatRoom(member, chatRoom)
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방에 소속되지 않았습니다."));
        memberChatRoom.setViewDate(LocalDateTime.now());

        return chatDao.searchByCondition(chatRoom, ChatSearchCondition.noCondition()).stream()
                .sorted()
                .map(chat -> {
                    int unconfirmedCounter = calculateUnconfirmedCounter(chat, chatRoom);
                    return ChatInfoResponseDto.of(chat, unconfirmedCounter);
                })
                .toList();
    }

    public List<ChatInfoResponseDto> searchChats(Member member, ChatSearchRequestDto requestDto) {
        log.info("ChatService.searchChats() called");

        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));

        return chatDao.searchByCondition(chatRoom, requestDto.toCondition()).stream()
                .map(chat -> {
                    int unconfirmedCounter = calculateUnconfirmedCounter(chat, chatRoom);
                    return ChatInfoResponseDto.of(chat, unconfirmedCounter);
                })
                .toList();
    }

    /**
     * 채팅방의 회원 중 몇 명이나 해당 채팅을 읽지 않았는지를 반환하는 메서드
     */
    private int calculateUnconfirmedCounter(Chat chat, ChatRoom chatRoom) {
        log.info("ChatService.calculateUnconfirmedCounter() called");

        int count = 0;
        List<LocalDateTime> viewDates = chatRoom.getMemberChatRooms().stream()
                .map(MemberChatRoom::getViewDate)
                .toList();
        for (LocalDateTime viewDate : viewDates) {
            if (chat.getInputDate().isAfter(viewDate)) {
                count++;
            }
        }

        return count;
    }
}
