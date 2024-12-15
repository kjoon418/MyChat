package junwatson.mychat.service;

import junwatson.mychat.domain.Blacklist;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.domain.MemberChatRoom;
import junwatson.mychat.dto.request.*;
import junwatson.mychat.dto.response.ChatRoomInfoResponseDto;
import junwatson.mychat.dto.response.MemberInfoResponseDto;
import junwatson.mychat.exception.BlockException;
import junwatson.mychat.exception.ChatRoomNotExistsException;
import junwatson.mychat.exception.IllegalMemberStateException;
import junwatson.mychat.exception.MemberNotExistsException;
import junwatson.mychat.repository.ChatRoomRepository;
import junwatson.mychat.repository.MemberRepository;
import junwatson.mychat.repository.condition.MemberChatRoomSearchCondition;
import junwatson.mychat.repository.dao.ChatDao;
import junwatson.mychat.repository.dao.MemberChatRoomDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatDao chatDao;
    private final MemberChatRoomDao memberChatRoomDao;

    public ChatRoomInfoResponseDto createChatRoom(Member requsetMember, ChatRoomCreateRequestDto requestDto) {
        log.info("ChatRoomService.createChatRoom() called");

        ChatRoom chatRoom = chatRoomRepository.save(requestDto.toEntity());

        // 유효성 검사 및 requestDto를 통해 회원 조회
        if (requestDto.getFriends().isEmpty()) {
            throw new IllegalArgumentException("다른 회원 잆이 채팅방을 만들 수 없습니다.");
        }

        List<Member> members = new ArrayList<>();
        members.add(requsetMember);
        for (ChatRoomCreateRequestDto.Friend friend : requestDto.getFriends()) {
            Member member = memberRepository.findByEmail(friend.getEmail())
                    .orElseThrow(() -> new MemberNotExistsException("해당 이메일을 지닌 회원이 존재하지 않습니다: " + friend.getEmail()));

            if (members.contains(member) || member.equals(requsetMember)) {
                throw new IllegalArgumentException("회원 정보가 중복되었습니다.");
            }
            if (isBlocked(requsetMember, member)) {
                throw new BlockException("나를 차단한 회원입니다.");
            }
            // 차단한 회원과 채팅방을 만드려 할 경우 프론트 측에서 경고창을 띄울 수 있도록 알림
            if (hasBlock(requsetMember, member)) {
                throw new BlockException("내가 차단한 회원입니다: " + member.getEmail());
            }

            members.add(member);
        }

        // 각 회원을 채팅방에 참여시킴
        StringBuilder systemChatBuilder = new StringBuilder("새로운 채팅방이 생성되었습니다. 구성원: ");
        for (Member member : members) {
            memberChatRoomDao.createMemberChatRoom(member, chatRoom);
            systemChatBuilder.append(member.getName()).append(", ");
        }

        // 시스템 채팅 추가
        systemChatBuilder.delete(systemChatBuilder.length() - 2, systemChatBuilder.length());
        chatDao.createSystemChat(chatRoom, systemChatBuilder.toString());

        return ChatRoomInfoResponseDto.from(memberChatRoomDao.findByMemberAndChatRoom(requsetMember, chatRoom)
                .orElseThrow(() -> new RuntimeException("채팅방을 만드는 과정에서 문제가 발생했습니다.")));
    }

    public List<ChatRoomInfoResponseDto> findChatRooms(Member member) {
        log.info("ChatRoomService.findChatRooms() called");

        List<ChatRoomInfoResponseDto> responseDto = new ArrayList<>();

        // 엔티티를 DTO로 변환
        for (MemberChatRoom memberChatRoom : member.getMemberChatRooms()) {
            responseDto.add(ChatRoomInfoResponseDto.from(memberChatRoom));
        }

        return responseDto;
    }

    public List<ChatRoomInfoResponseDto> searchChatRooms(Member member, ChatRoomSearchRequestDto requestDto) {
        log.info("ChatRoomService.searchChatRooms() called");

        // 채팅방 이름을 통해 검색
        MemberChatRoomSearchCondition condition = requestDto.toCondition();
        List<MemberChatRoom> memberChatRooms = memberChatRoomDao.filterWithCondition(member.getMemberChatRooms(), condition);

        return memberChatRooms.stream()
                .map(ChatRoomInfoResponseDto::from)
                .toList();
    }

    /**
     * 채팅방의 정보를 수정하는 메서드<br>
     * 방의 이름이나 프로필을 본인에게만 변경할 것이기 때문에, ChatRoom 엔티티의 정보를 수정하지 않고 MemberChatRoom 엔티티의 정보를 수정한다
     */
    public ChatRoomInfoResponseDto modifyChatRoom(Member member, ChatRoomModificationRequestDto requestDto) {
        log.info("ChatRoomService.modifyChatRoom() called");

        String name = requestDto.getName();
        String profileUrl = requestDto.getProfileUrl();

        // 유효성 검사
        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));
        MemberChatRoom memberChatRoom = memberChatRoomDao.findByMemberAndChatRoom(member, chatRoom)
                .orElseThrow(() -> new IllegalMemberStateException("해당 채팅방에 소속되어 있지 않습니다."));

        // 수정하고자 하는 값이 전달된 것에 한해 채팅방의 정보를 수정
        if (StringUtils.hasText(name)) {
            memberChatRoom.setAliasName(name);
        }
        if (StringUtils.hasText(profileUrl)) {
            memberChatRoom.setAliasProfileUrl(profileUrl);
        }

        return ChatRoomInfoResponseDto.from(memberChatRoom);
    }

    public ChatRoomInfoResponseDto leaveChatRoom(Member member, ChatRoomInfoRequestDto requestDto) {
        log.info("ChatRoomService.leaveChatRoom() called");

        // 유효성 검사
        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));
        MemberChatRoom memberChatRoom = memberChatRoomDao.findByMemberAndChatRoom(member, chatRoom)
                .orElseThrow(() -> new IllegalMemberStateException("해당 채팅방에 소속되어 있지 않습니다."));

        // 채팅방에서 나가게 함
        chatRoomRepository.leaveChatRoom(memberChatRoom);

        // 시스템 채팅 추가
        chatDao.createSystemChat(chatRoom, member.getName()+"님이 채팅방에서 나갔습니다");

        return ChatRoomInfoResponseDto.from(memberChatRoom);
    }

    public ChatRoomInfoResponseDto inviteChatRoom(Member requestMember, ChatRoomInviteRequestDto requestDto) {
        log.info("ChatRoomService.inviteChatRoom() called");

        // 유효성 검사
        if (requestDto.getFriends().isEmpty()) {
            throw new IllegalArgumentException("초대하고자 하는 회원이 없습니다.");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getId())
                .orElseThrow(() -> new ChatRoomNotExistsException("해당 채팅방이 존재하지 않습니다."));
        MemberChatRoom findMemberChatRoom = memberChatRoomDao.findByMemberAndChatRoom(requestMember, chatRoom)
                .orElseThrow(() -> new IllegalMemberStateException("해당 채팅방에 소속되어 있지 않습니다."));

        List<Member> members = new ArrayList<>();
        for (ChatRoomInviteRequestDto.Friend friend : requestDto.getFriends()) {
            members.add(memberRepository.findByEmail(friend.getEmail())
                    .orElseThrow(() -> new MemberNotExistsException("초대하고자 하는 회원이 존재하지 않습니다.")));
        }

        // 회원 초대 및 시스템 채팅 추가
        StringBuilder systemChatBuilder = new StringBuilder();
        for (Member member : members) {
            memberChatRoomDao.createMemberChatRoom(member, chatRoom);
            systemChatBuilder.append(member.getName()).append(", ");
        }

        systemChatBuilder.delete(systemChatBuilder.length() - 2, systemChatBuilder.length());
        systemChatBuilder.append("님이 채팅방에 초대되었습니다.");
        chatDao.createSystemChat(chatRoom, systemChatBuilder.toString());

        return ChatRoomInfoResponseDto.from(findMemberChatRoom);
    }

    /**
     * 사용자가 해당 회원을 차단했는지 여부를 반환하는 메서드
     */
    private boolean hasBlock(Member fromMember, Member toMember) {
        return fromMember.getBlacklists().stream()
                .map(Blacklist::getTargetMember)
                .anyMatch(targetMember -> targetMember.equals(toMember));
    }

    /**
     * 사용자가 해당 회원에게 차단당했는지 여부를 반환하는 메서드
     */
    private boolean isBlocked(Member fromMember, Member toMember) {
        return fromMember.getBlockedLists().stream()
                .map(Blacklist::getMember)
                .anyMatch(targetMember -> targetMember.equals(toMember));
    }
}
