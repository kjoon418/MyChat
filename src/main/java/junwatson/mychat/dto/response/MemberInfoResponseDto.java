package junwatson.mychat.dto.response;

import junwatson.mychat.domain.Member;
import lombok.*;

import static lombok.AccessLevel.*;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MemberInfoResponseDto {

    private String email;
    private String name;
    private String profile_url;

    public static MemberInfoResponseDto from(Member member) {
        return MemberInfoResponseDto.builder()
                .email(member.getEmail())
                .name(member.getName())
                .profile_url(member.getProfileUrl())
                .build();
    }
}
