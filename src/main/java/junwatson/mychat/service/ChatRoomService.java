package junwatson.mychat.service;

import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import junwatson.mychat.dto.request.ChatRoomCreateRequestDto;
import junwatson.mychat.dto.request.ChatRoomInfoRequestDto;
import junwatson.mychat.dto.request.ChatRoomInviteRequestDto;
import junwatson.mychat.dto.request.MemberInfoRequestDto;
import junwatson.mychat.dto.response.ChatRoomInfoResponseDto;
import junwatson.mychat.exception.ChatRoomNotExistsException;
import junwatson.mychat.exception.IllegalChatRoomStateException;
import junwatson.mychat.exception.MemberNotExistsException;
import junwatson.mychat.repository.ChatRoomRepository;
import junwatson.mychat.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    public ChatRoomInfoResponseDto createChatRoom(Member requsetMember, ChatRoomCreateRequestDto requestDto) {
        log.info("ChatRoomService.createChatRoom() called");

        ChatRoom chatRoom = chatRoomRepository.save(requestDto.toEntity());

        List<Member> members = new ArrayList<>();
        members.add(requsetMember);
        for (ChatRoomCreateRequestDto.Friend friend : requestDto.getFriends()) {
            members.add(memberRepository.findByEmail(friend.getEmail())
                    .orElseThrow(() -> new MemberNotExistsException("해당 이메일을 지닌 회원이 존재하지 않습니다: " + friend.getEmail())));
        }
        for (Member member : members) {
            chatRoomRepository.createMemberChatRoom(member, chatRoom);
        }

        return ChatRoomInfoResponseDto.from(chatRoom);
    }

    public ChatRoomInfoResponseDto leaveChatRoom(Member member, ChatRoomInfoRequestDto requestDto) {
        log.info("ChatRoomService.leaveChatRoom() called");

        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));

        chatRoomRepository.leaveChatRoom(member, chatRoom);

        return ChatRoomInfoResponseDto.from(chatRoom);
    }

    public ChatRoomInfoResponseDto inviteChatRoom(Member requestMember, ChatRoomInviteRequestDto requestDto) {
        log.info("ChatRoomService.inviteChatRoom() called");

        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));

        List<Member> members = new ArrayList<>();
        for (ChatRoomInviteRequestDto.Friend friend : requestDto.getFriends()) {
            members.add(memberRepository.findByEmail(friend.getEmail())
                    .orElseThrow(() -> new MemberNotExistsException("초대하고자 하는 회원이 존재하지 않습니다.")));
        }
        for (Member member : members) {
            chatRoomRepository.createMemberChatRoom(member, chatRoom);
        }

        return ChatRoomInfoResponseDto.from(chatRoom);
    }
}
