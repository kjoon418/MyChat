package junwatson.mychat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PUBLIC;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ChatRoomModificationRequestDto {

    private Long id;
    private String name;
    private String profileUrl;

}