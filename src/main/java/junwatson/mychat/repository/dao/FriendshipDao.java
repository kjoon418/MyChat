package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.Friendship;
import junwatson.mychat.domain.Member;
import junwatson.mychat.exception.MemberNotExistsException;
import junwatson.mychat.repository.condition.MemberSearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Repository
@Slf4j
public class FriendshipDao {

    public void createFriendship(Member member, Member friend) {
        log.info("FriendshipDao.createFriendship() called");

        List<Friendship> friendships1 = member.getFriendships();
        List<Friendship> friendships2 = friend.getFriendships();

        Friendship friendship1 = Friendship.builder()
                .member(member)
                .friendMember(friend)
                .build();
        Friendship friendship2 = Friendship.builder()
                .member(friend)
                .friendMember(member)
                .build();

        friendships1.add(friendship1);
        friendships2.add(friendship2);
    }

    public void removeFriendship(Member member, Member friend) {
        log.info("FriendshipDao.removeFriendship() called");

        Friendship memberFriendship = member.getFriendships()
                .stream()
                .filter(friendship -> friendship.getFriendMember().equals(friend))
                .findAny()
                .orElseThrow(() -> new MemberNotExistsException("상대 회원과 친구 상태가 아닙니다."));

        Friendship friendFriendship = friend.getFriendships()
                .stream()
                .filter(friendship -> friendship.getFriendMember().equals(member))
                .findAny()
                .orElseThrow(() -> new MemberNotExistsException("상대 회원과 친구 상태가 아닙니다."));

        member.getFriendships().remove(memberFriendship);
        friend.getFriendships().remove(friendFriendship);
    }

    public List<Friendship> searchFriendships(Member member, MemberSearchCondition condition) {
        log.info("FriendshipDao.searchFriendships() called");

        Stream<Friendship> stream = member.getFriendships().stream();

        String email = condition.getEmail();
        String name = condition.getName();

        if (StringUtils.hasText(email)) {
            stream = stream.filter(friendship -> friendship.getFriendMember().getEmail().contains(email));
        }
        if (StringUtils.hasText(name)) {
            stream = stream.filter((friendship -> friendship.getFriendMember().getName().contains(name)));
        }

        return stream.toList();
    }

    public boolean areFriends(Member member, Member friend) {
        boolean memberToFriend = member.getFriendships()
                .stream()
                .anyMatch(friendship -> friendship.getFriendMember().equals(friend));

        boolean friendToMember = friend.getFriendships()
                .stream()
                .anyMatch(friendship -> friendship.getFriendMember().equals(member));

        return memberToFriend && friendToMember;
    }

    public void removeAllFriendships(Member member) {
        log.info("FriendshipDao.removeAllFriendships() called");

        List<Friendship> removeFriendships = new ArrayList<>(member.getFriendships());
        for (Friendship friendship : removeFriendships) {
            removeFriendship(member, friendship.getFriendMember());
        }
    }

}
