package junwatson.mychat.service;

import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.ChatRoomCreateRequestDto;
import junwatson.mychat.dto.response.ChatRoomInfoResponseDto;
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
}
