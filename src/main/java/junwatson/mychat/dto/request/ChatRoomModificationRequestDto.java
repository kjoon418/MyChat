package junwatson.mychat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class ChatRoomModificationRequestDto {

    private Long id;
    private String name;
    private String profileUrl;

}