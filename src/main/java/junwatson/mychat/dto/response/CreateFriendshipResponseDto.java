package junwatson.mychat.dto.response;

import junwatson.mychat.domain.Member;
import lombok.*;

@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateFriendshipResponseDto {

    String friendEmail;

    public static CreateFriendshipResponseDto from(Member member) {
        return CreateFriendshipResponseDto.builder()
                .friendEmail(member.getEmail())
                .build();
    }
}
