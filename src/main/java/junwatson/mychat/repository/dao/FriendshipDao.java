package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.Friendship;
import junwatson.mychat.domain.Member;
import junwatson.mychat.repository.condition.MemberSearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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
}
