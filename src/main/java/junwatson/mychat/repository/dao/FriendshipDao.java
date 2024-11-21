package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.Friendship;
import junwatson.mychat.domain.Member;
import junwatson.mychat.repository.condition.MemberSearchCondition;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Stream;

@Repository
public class FriendshipDao {

    /**
     * 두 멤버간 양방향 친구관계를 생성하는 메서드
     */
    public void createFriendship(Member member, Member friend) {
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

    /**
     * 조건에 따라 Friendship 을 조회하는 메서드
     */
    public List<Friendship> searchFriendships(Member member, MemberSearchCondition condition) {
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
