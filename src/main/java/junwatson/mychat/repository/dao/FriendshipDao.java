package junwatson.mychat.repository.dao;

import junwatson.mychat.domain.Friendship;
import junwatson.mychat.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
