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

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(orphanRemoval = true, fetch = LAZY, cascade = ALL)
    @Setter
    private RefreshToken refreshToken;

    @OneToMany(fetch = LAZY, cascade = ALL, orphanRemoval = true)
    private final List<Friendship> friendships = new ArrayList<>();

    @OneToMany(mappedBy = "requestMember", fetch = LAZY, cascade = ALL, orphanRemoval = true)
    private final List<FriendshipRequest> sentFriendshipRequests = new ArrayList<>();

    @OneToMany(mappedBy = "responseMember", fetch = LAZY, cascade = ALL, orphanRemoval = true)
    private final List<FriendshipRequest> receivedFriendshipRequests = new ArrayList<>();

    @OneToMany(mappedBy = "member", fetch = LAZY)
    private final List<MemberChatRoom> memberChatRooms = new ArrayList<>();

    @OneToMany(mappedBy = "member", fetch = LAZY, cascade = ALL, orphanRemoval = true)
    private final List<Blacklist> blacklists = new ArrayList<>();

    @OneToMany(mappedBy = "targetMember", fetch = LAZY, cascade = ALL, orphanRemoval = true)
    private final List<Blacklist> blockedLists = new ArrayList<>();

    @Enumerated(STRING)
    private MemberRole role;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;
    private String password;
    private String profileUrl;

    @Builder
    private Member(MemberRole role, String email, String name, String password, String profileUrl) {
        this.role = role;
        this.email = email;
        this.name = name;
        this.password = password;
        this.profileUrl = profileUrl;
    }
}
