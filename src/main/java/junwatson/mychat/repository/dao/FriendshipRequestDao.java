package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.FriendshipRequest;
import junwatson.mychat.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class FriendshipRequestDao {

    public boolean isReceivedFriendshipRequestExists(Member fromMember, Member toMember) {
        log.info("FriendshipRequestDao.isReceivedFriendshipRequestExists() called");

        return fromMember.getReceivedFriendshipRequests()
                .stream()
                .anyMatch((request) -> request.getRequestMember().equals(toMember));
    }

    public boolean isSentFriendshipRequestExists(Member fromMember, Member toMember) {
        log.info("FriendshipRequestDao.isSentFriendshipRequestExists() called");

        return fromMember.getSentFriendshipRequests()
                .stream()
                .anyMatch((request) -> request.getResponseMember().equals(toMember));
    }

    public void createFriendshipRequest(Member fromMember, Member toMember) {
        log.info("FriendshipRequestDao.createFriendshipRequest() called");

        FriendshipRequest friendshipRequest = FriendshipRequest.builder()
                .requestMember(fromMember)
                .responseMember(toMember)
                .build();

        fromMember.getSentFriendshipRequests().add(friendshipRequest);
    }

    public void removeFriendshipRequest(Member fromMember, Member toMember) {
        log.info("FriendshipRequestDao.removeRequest() called");

        fromMember.getSentFriendshipRequests().stream()
                .filter(friendshipRequest -> friendshipRequest.getResponseMember().equals(toMember))
                .findAny()
                .ifPresent(friendshipRequest -> {
                    fromMember.getSentFriendshipRequests()
                            .remove(friendshipRequest);
                });

        toMember.getReceivedFriendshipRequests().stream()
                .filter(friendshipRequest -> friendshipRequest.getRequestMember().equals(fromMember))
                .findAny()
                .ifPresent(friendshipRequest -> {
                    toMember.getReceivedFriendshipRequests()
                            .remove(friendshipRequest);
                });
    }
}
