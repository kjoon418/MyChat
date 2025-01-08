package junwatson.mychat.service;

import jakarta.persistence.EntityManager;
import junwatson.mychat.domain.ChatRoom;
import junwatson.mychat.domain.Member;
import junwatson.mychat.dto.request.ChatRoomCreateRequestDto;
import junwatson.mychat.dto.request.ChatRoomInfoRequestDto;
import junwatson.mychat.dto.request.ChatRoomSearchRequestDto;
import junwatson.mychat.dto.response.ChatRoomInfoResponseDto;
import junwatson.mychat.exception.BlockException;
import junwatson.mychat.exception.ChatRoomNotExistsException;
import junwatson.mychat.repository.ChatRoomRepository;
import junwatson.mychat.util.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class ChatRoomServiceTest {

    private static final String BASIC_PROFILE_URL = "";

    @Autowired
    private EntityManager em;
    @Autowired
    private TestUtils utils;
    @Autowired
    private ChatRoomService chatRoomService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Test
    @DisplayName("채팅방 생성: 성공")
    void createChatRoom_success() {
        // given: 멤버 생성
        Member[] members = utils.createTestMembers(3);
        Long id0 = members[0].getId();
        Long id1 = members[1].getId();
        Long id2 = members[2].getId();
        utils.clearEntityManager(em);

        // when: 채팅방 생성
        Member requestMember0 = memberService.findById(id0);
        ChatRoomCreateRequestDto requestDto = utils.createChatRoomCreateRequestDto(List.of(members[1], members[2]));
        chatRoomService.createChatRoom(requestMember0, requestDto);
        utils.clearEntityManager(em);

        // then: 채팅방이 있으며, 모든 멤버가 해당 채팅방에 들어 있는지 확인
        Member member0 = memberService.findById(id0);
        Member member1 = memberService.findById(id1);
        Member member2 = memberService.findById(id2);
        ChatRoom chatRoom = member0.getMemberChatRooms()
                .getFirst()
                .getChatRoom();

        // case1: 채팅방이 1개 생성되었는지 확인
        assertThat(member0.getMemberChatRooms().size()).isEqualTo(1);
        assertThat(member1.getMemberChatRooms().size()).isEqualTo(1);
        assertThat(member2.getMemberChatRooms().size()).isEqualTo(1);

        // case2: 채팅방에 회원이 2명 있는지 확인
        assertThat(chatRoom.getMemberChatRooms().size()).isEqualTo(3);

        // case3: 채팅방에 모든 회원이 있는지 확인
        assertThat(chatRoom.getMemberChatRooms().contains(member0.getMemberChatRooms().getFirst())).isTrue();
        assertThat(chatRoom.getMemberChatRooms().contains(member1.getMemberChatRooms().getFirst())).isTrue();
        assertThat(chatRoom.getMemberChatRooms().contains(member2.getMemberChatRooms().getFirst())).isTrue();
    }

    @Test
    @DisplayName("채팅방 생성: 회원 자신의 정보 포함 예외")
    void createChatRoom_inviteMyself() {
        // given: 회원 생성
        Member[] members = utils.createTestMembers(3);

        // when: 자기 자신까지 담은 Request DTO 생성
        ChatRoomCreateRequestDto requestDto = utils.createChatRoomCreateRequestDto(List.of(members[0], members[1], members[2]));

        // given: 채팅방 생성시 예외 발생
        assertThatThrownBy(() -> chatRoomService.createChatRoom(members[0], requestDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("채팅방 생성: 다른 회원 없이 생성 예외")
    void createChatRoom_alone() {
        // given: 회원 생성
        Member member = utils.createTestMember();

        // when: 다른 회원이 없는 Request DTO 생성
        ChatRoomCreateRequestDto requestDto = utils.createChatRoomCreateRequestDto(List.of());

        // then: 채팅방 생성 시 예외 발생
        assertThatThrownBy(() -> chatRoomService.createChatRoom(member, requestDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("채팅방 생성: 회원 중복 예외")
    void createChatRoom_memberDuplicate() {
        // given: 회원 생성
        Member[] members = utils.createTestMembers(3);

        // when: 한 회원의 정보가 중복되어 들어간 Request DTO 생성
        ChatRoomCreateRequestDto requestDto = utils.createChatRoomCreateRequestDto(List.of(members[1], members[1], members[2]));

        // then: 채팅방 생성 시 예외 발생
        assertThatThrownBy(() -> chatRoomService.createChatRoom(members[0], requestDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("채팅방 생성: 나를 차단한 회원 예외")
    void createChatRoom_blocked() {
        // given: 회원 생성 및 차단
        Member[] members = utils.createTestMembers(2);
        memberService.addBlacklist(members[1], utils.createMemberInfoRequestDto(members[0]));

        // when: 나를 차단한 회원의 정보가 포함된 Request DTO 생성
        ChatRoomCreateRequestDto requestDto = utils.createChatRoomCreateRequestDto(List.of(members[1]));

        // then: 채팅방 생성 시 예외 발생
        assertThatThrownBy(() -> chatRoomService.createChatRoom(members[0], requestDto))
                .isInstanceOf(BlockException.class);
    }

    @Test
    @DisplayName("채팅방 생성: 내가 차단한 회원 예외")
    void createChatRoom_block() {
        // given: 회원 생성 및 차단
        Member[] members = utils.createTestMembers(2);
        memberService.addBlacklist(members[0], utils.createMemberInfoRequestDto(members[1]));

        // when: 내가 차단한 회원의 정보가 포함된 Request DTO 생성
        ChatRoomCreateRequestDto requestDto = utils.createChatRoomCreateRequestDto(List.of(members[1]));

        // then: 채팅방 생성 시 예외 발생
        assertThatThrownBy(() -> chatRoomService.createChatRoom(members[0], requestDto))
                .isInstanceOf(BlockException.class);
    }

    @Test
    @DisplayName("채팅방 조회: 성공")
    void findChatRoom_success() {
        // given: 회원 및 Request DTO 생성
        Member[] members = utils.createTestMembers(6);
        ChatRoomCreateRequestDto requestDto1 = utils.createChatRoomCreateRequestDto(List.of(members[1], members[2], members[3], members[4]));
        ChatRoomCreateRequestDto requestDto2 = utils.createChatRoomCreateRequestDto(List.of(members[1], members[2]));

        // when: 채팅방 생성
        chatRoomService.createChatRoom(members[0], requestDto1);
        chatRoomService.createChatRoom(members[0], requestDto2);

        // then: 채팅방 조회 성공
        assertThat(chatRoomService.findChatRooms(members[0]).size()).isEqualTo(2);
        assertThat(chatRoomService.findChatRooms(members[1]).size()).isEqualTo(2);
        assertThat(chatRoomService.findChatRooms(members[2]).size()).isEqualTo(2);
        assertThat(chatRoomService.findChatRooms(members[3]).size()).isEqualTo(1);
        assertThat(chatRoomService.findChatRooms(members[4]).size()).isEqualTo(1);
        assertThat(chatRoomService.findChatRooms(members[5]).size()).isEqualTo(0);
    }

    @Test
    @DisplayName("채팅방 검색: 성공")
    void searchChatRoom_success() {
        // given: 회원 및 채팅방 생성을 위한 Request DTO 생성
        Member[] members = utils.createTestMembers(5);
        ChatRoomCreateRequestDto[] requestDto = new ChatRoomCreateRequestDto[4];
        requestDto[0] = utils.createChatRoomCreateRequestDto(List.of(members[1]), "ChatRoom1", BASIC_PROFILE_URL);
        requestDto[1] = utils.createChatRoomCreateRequestDto(List.of(members[2]), "ChatRoom12", BASIC_PROFILE_URL);
        requestDto[2] = utils.createChatRoomCreateRequestDto(List.of(members[3]), "ChatRoom123", BASIC_PROFILE_URL);
        requestDto[3] = utils.createChatRoomCreateRequestDto(List.of(members[4]), "ChatRoom1234", BASIC_PROFILE_URL);

        // when: 채팅방 생성
        chatRoomService.createChatRoom(members[0], requestDto[0]);
        chatRoomService.createChatRoom(members[0], requestDto[1]);
        chatRoomService.createChatRoom(members[0], requestDto[2]);
        chatRoomService.createChatRoom(members[0], requestDto[3]);

        // then: 검색 성공
        List<ChatRoomInfoResponseDto> responseDto1 = chatRoomService.searchChatRooms(members[0], ChatRoomSearchRequestDto.builder().name("1").build());
        assertThat(responseDto1.size()).isEqualTo(4);
        List<ChatRoomInfoResponseDto> responseDto2 = chatRoomService.searchChatRooms(members[0], ChatRoomSearchRequestDto.builder().name("2").build());
        assertThat(responseDto2.size()).isEqualTo(3);
        List<ChatRoomInfoResponseDto> responseDto3 = chatRoomService.searchChatRooms(members[0], ChatRoomSearchRequestDto.builder().name("3").build());
        assertThat(responseDto3.size()).isEqualTo(2);
        List<ChatRoomInfoResponseDto> responseDto4 = chatRoomService.searchChatRooms(members[0], ChatRoomSearchRequestDto.builder().name("4").build());
        assertThat(responseDto4.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("채팅방 검색: 부적절한 값 예외")
    void searchChatRoom_illegalValue() {
        // given: 회원 및 채팅방 생성을 위한 Request DTO 생성
        Member[] members = utils.createTestMembers(3);
        ChatRoomCreateRequestDto[] requestDto = new ChatRoomCreateRequestDto[2];
        requestDto[0] = utils.createChatRoomCreateRequestDto(List.of(members[1]));
        requestDto[1] = utils.createChatRoomCreateRequestDto(List.of(members[2]));

        // when: 채팅방 생성
        chatRoomService.createChatRoom(members[0], requestDto[0]);
        chatRoomService.createChatRoom(members[0], requestDto[1]);

        // then: null 값이나 빈 문자열로 검색시 모든 채팅방 조회
        List<ChatRoomInfoResponseDto> responseDto1 = chatRoomService.searchChatRooms(members[0], ChatRoomSearchRequestDto.builder().name(null).build());
        assertThat(responseDto1.size()).isEqualTo(2);
        List<ChatRoomInfoResponseDto> responseDto2 = chatRoomService.searchChatRooms(members[0], ChatRoomSearchRequestDto.builder().name("").build());
        assertThat(responseDto2.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("채팅방 나가기: 성공")
    void leaveChatRoom_success() {
        // given: 회원 및 Request DTO 생성
        Member[] members = utils.createTestMembers(3);
        ChatRoomCreateRequestDto requestDto1 = utils.createChatRoomCreateRequestDto(List.of(members[1]));
        ChatRoomCreateRequestDto requestDto2 = utils.createChatRoomCreateRequestDto(List.of(members[2]));

        // when: 채팅방 생성
        ChatRoomInfoResponseDto responseDto1 = chatRoomService.createChatRoom(members[0], requestDto1);
        ChatRoomInfoResponseDto responseDto2 = chatRoomService.createChatRoom(members[0], requestDto2);
        Long chatRoomId1 = responseDto1.getId();
        Long chatRoomId2 = responseDto2.getId();

        // then: 채팅방을 탈퇴하며, 채팅방 갯수가 하나씩 감소
        assertThat(members[0].getMemberChatRooms().size()).isEqualTo(2);
        chatRoomService.leaveChatRoom(members[0], ChatRoomInfoRequestDto.builder().id(chatRoomId1).build());
        assertThat(members[0].getMemberChatRooms().size()).isEqualTo(1);
        chatRoomService.leaveChatRoom(members[0], ChatRoomInfoRequestDto.builder().id(chatRoomId2).build());
        assertThat(members[0].getMemberChatRooms().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("채팅방 나가기: 빈 채팅방 삭제")
    void leaveChatRoom_remove() {
        // given: 회원 및 Request DTO 생성
        Member[] members = utils.createTestMembers(2);
        ChatRoomCreateRequestDto requestDto = utils.createChatRoomCreateRequestDto(List.of(members[1]));

        // when: 채팅방 생성
        ChatRoomInfoResponseDto responseDto = chatRoomService.createChatRoom(members[0], requestDto);
        Long chatRoomId = responseDto.getId();

        // given: 모든 회원이 나간 채팅방 자동 삭제
        ChatRoomInfoRequestDto chatRoomInfoRequestDto = ChatRoomInfoRequestDto.builder().id(chatRoomId).build();
        chatRoomService.leaveChatRoom(members[0], chatRoomInfoRequestDto);
        chatRoomService.leaveChatRoom(members[1], chatRoomInfoRequestDto);

        assertThat(chatRoomRepository.findById(chatRoomInfoRequestDto.getId()))
                .isEmpty(); // 조회 실패
    }
}