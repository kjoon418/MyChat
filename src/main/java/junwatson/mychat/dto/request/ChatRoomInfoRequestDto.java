package junwatson.mychat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PUBLIC;

@Getter
@Builder
@AllArgsConstructor(access = PUBLIC)
public class ChatRoomInfoRequestDto {

    private Long id;

}
