package junwatson.mychat.service;

import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import junwatson.mychat.dto.request.*;
import junwatson.mychat.dto.response.ChatRoomInfoResponseDto;
import junwatson.mychat.dto.response.MemberInfoResponseDto;
import junwatson.mychat.exception.ChatRoomNotExistsException;
import junwatson.mychat.exception.IllegalChatRoomStateException;
import junwatson.mychat.exception.IllegalMemberStateException;
import junwatson.mychat.exception.MemberNotExistsException;
import junwatson.mychat.repository.ChatRoomRepository;
import junwatson.mychat.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        MemberChatRoom memberChatRoom = chatRoomRepository.createMemberChatRoom(requsetMember, chatRoom);

        List<Member> members = new ArrayList<>();
        for (ChatRoomCreateRequestDto.Friend friend : requestDto.getFriends()) {
            members.add(memberRepository.findByEmail(friend.getEmail())
                    .orElseThrow(() -> new MemberNotExistsException("해당 이메일을 지닌 회원이 존재하지 않습니다: " + friend.getEmail())));
        }
        for (Member member : members) {
            chatRoomRepository.createMemberChatRoom(member, chatRoom);
        }

        return ChatRoomInfoResponseDto.from(memberChatRoom);
    }

    public ChatRoom findChatRoomByDto(ChatRoomInfoRequestDto requestDto) {
        return chatRoomRepository.findById(requestDto.getId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));
    }

    public List<ChatRoomInfoResponseDto> findChatRooms(Member member) {
        log.info("ChatRoomService.findChatRooms() called");

        List<ChatRoomInfoResponseDto> responseDto = new ArrayList<>();
        for (MemberChatRoom memberChatRoom : member.getMemberChatRooms()) {
            responseDto.add(ChatRoomInfoResponseDto.from(memberChatRoom));
        }

        return responseDto;
    }

    public List<MemberInfoResponseDto> findMembersInChatRoom(Member requestMember, ChatRoom chatRoom) {
        log.info("ChatRoomService.findMembersInChatRoom() called");

        boolean isPresent = requestMember.getMemberChatRooms().stream()
                .map(MemberChatRoom::getChatRoom)
                .anyMatch(findChatRoom -> findChatRoom.equals(chatRoom));
        if (!isPresent) {
            throw new IllegalMemberStateException("해당 채팅방에 소속되어있지 않습니다.");
        }

        List<Member> members = chatRoom.getMemberChatRooms().stream()
                .map(MemberChatRoom::getMember)
                .toList();
        List<MemberInfoResponseDto> responseDto = new ArrayList<>();
        for (Member member : members) {
            responseDto.add(MemberInfoResponseDto.from(member));
        }

        return responseDto;
    }

    /**
     * 방의 이름이나 프로필을 본인에게만 변경할 것이기 때문에, ChatRoom 엔티티의 정보를 수정하지 않고 MemberChatRoom 엔티티의 정보를 수정한다
     */
    public ChatRoomInfoResponseDto modifyChatRoom(Member member, ChatRoomModificationRequestDto requestDto) {
        log.info("ChatRoomService.modifyChatRoom() called");

        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));
        MemberChatRoom findMemberChatRoom = member.getMemberChatRooms().stream()
                .filter(memberChatRoom -> memberChatRoom.getChatRoom().equals(chatRoom))
                .findAny()
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방에 소속되지 않았습니다."));

        String name = requestDto.getName();
        String profileUrl = requestDto.getProfileUrl();
        if (StringUtils.hasText(name)) {
            findMemberChatRoom.setAliasName(name);
        }
        if (StringUtils.hasText(profileUrl)) {
            findMemberChatRoom.setAliasProfileUrl(profileUrl);
        }

        return ChatRoomInfoResponseDto.from(findMemberChatRoom);
    }

    public ChatRoomInfoResponseDto leaveChatRoom(Member member, ChatRoomInfoRequestDto requestDto) {
        log.info("ChatRoomService.leaveChatRoom() called");

        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));

        MemberChatRoom memberChatRoom = chatRoomRepository.leaveChatRoom(member, chatRoom);

        return ChatRoomInfoResponseDto.from(memberChatRoom);
    }

    public ChatRoomInfoResponseDto inviteChatRoom(Member requestMember, ChatRoomInviteRequestDto requestDto) {
        log.info("ChatRoomService.inviteChatRoom() called");

        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));
        MemberChatRoom findMemberChatRoom = requestMember.getMemberChatRooms().stream()
                .filter(memberChatRoom -> memberChatRoom.getChatRoom().equals(chatRoom))
                .findAny()
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방에 소속되어 있지 않습니다."));

        List<Member> members = new ArrayList<>();
        for (ChatRoomInviteRequestDto.Friend friend : requestDto.getFriends()) {
            members.add(memberRepository.findByEmail(friend.getEmail())
                    .orElseThrow(() -> new MemberNotExistsException("초대하고자 하는 회원이 존재하지 않습니다.")));
        }
        for (Member member : members) {
            chatRoomRepository.createMemberChatRoom(member, chatRoom);
        }

        return ChatRoomInfoResponseDto.from(findMemberChatRoom);
    }
}
