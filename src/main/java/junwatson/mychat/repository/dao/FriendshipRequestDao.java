package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.FriendshipRequest;
import junwatson.mychat.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FriendshipRequestDao {

    public boolean isRequestExists(Member member, Member friend) {
        Optional<FriendshipRequest> findRequest = member.getReceivedFriendshipRequests()
                .stream()
                .filter((request) -> request.getRequestMember().equals(friend))
                .findAny();

        return findRequest.isPresent();
    }

    public void createFriendshipRequest(Member member, Member friend) {
        FriendshipRequest friendshipRequest = FriendshipRequest.builder()
                .requestMember(member)
                .responseMember(friend)
                .build();

        member.getSentFriendshipRequests().add(friendshipRequest);
    }

    /**
     * 친구 추가 요청을 삭제하는 메서드
     */
    public void removeRequest(Member member, Member friend) {
        Optional<FriendshipRequest> findRequest = member.getSentFriendshipRequests().stream()
                .filter((request) -> request.getResponseMember().equals(friend))
                .findAny();
        findRequest.ifPresent(friendshipRequest -> member.getSentFriendshipRequests()
                .remove(friendshipRequest));
    }
}
