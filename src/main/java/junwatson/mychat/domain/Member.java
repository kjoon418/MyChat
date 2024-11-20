package junwatson.mychat.domain;

import jakarta.persistence.*;
import junwatson.mychat.domain.type.MemberRole;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class Member {

    @Builder
    private Member(MemberRole role, String email, String name, String profileUrl) {
        this.role = role;
        this.email = email;
        this.name = name;
        this.profileUrl = profileUrl;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(orphanRemoval = true, fetch = LAZY, cascade = ALL)
    @Setter
    private RefreshToken refreshToken;

    @OneToMany(mappedBy = "member", fetch = LAZY)
    private List<Friendship> friendships = new ArrayList<>();

    @OneToMany(mappedBy = "requestMember", fetch = LAZY)
    private List<FriendshipRequest> sentFriendshipRequests = new ArrayList<>();

    @OneToMany(mappedBy = "responseMember", fetch = LAZY)
    private List<FriendshipRequest> receivedFriendshipRequests = new ArrayList<>();

    @OneToMany(mappedBy = "member", fetch = LAZY)
    private List<MemberChatRoom> memberChatRooms = new ArrayList<>();

    @Enumerated(STRING)
    private MemberRole role;

    @Column(unique = true)
    private String email;
    private String name;
    private String profileUrl;
}
