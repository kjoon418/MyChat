package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.FriendshipRequest;
import junwatson.mychat.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class FriendshipRequestDao {

    public List<FriendshipRequest> findSentFriendshipRequests(Member member) {
        log.info("FriendshipRequestDao.findSentFriendshipRequest() called");

        return member.getSentFriendshipRequests();
    }

    public List<FriendshipRequest> findReceivedFriendshipRequests(Member member) {
        log.info("FriendshipRequestDao.findReceivedFriendshipRequest() called");

        return member.getReceivedFriendshipRequests();
    }

    public boolean isReceivedFriendshipRequestExists(Member member, Member friend) {
        log.info("FriendshipRequestDao.isReceivedFriendshipRequestExists() called");

        return member.getReceivedFriendshipRequests()
                .stream()
                .anyMatch((request) -> request.getRequestMember().equals(friend));
    }

    public boolean isSentFriendshipRequestExists(Member member, Member friend) {
        log.info("FriendshipRequestDao.isSentFriendshipRequestExists() called");

        return member.getSentFriendshipRequests()
                .stream()
                .anyMatch((request) -> request.getResponseMember().equals(friend));
    }

    public void createFriendshipRequest(Member member, Member friend) {
        log.info("FriendshipRequestDao.createFriendshipRequest() called");

        FriendshipRequest friendshipRequest = FriendshipRequest.builder()
                .requestMember(member)
                .responseMember(friend)
                .build();

        member.getSentFriendshipRequests().add(friendshipRequest);
    }

    public void removeFriendshipRequest(Member member, Member friend) {
        log.info("FriendshipRequestDao.removeRequest() called");

        member.getSentFriendshipRequests().stream()
                .filter((request) -> request.getResponseMember().equals(friend))
                .findAny()
                .ifPresent(friendshipRequest -> member.getSentFriendshipRequests()
                .remove(friendshipRequest));
    }
}
