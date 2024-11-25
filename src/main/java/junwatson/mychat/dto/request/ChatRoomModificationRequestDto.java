package junwatson.mychat.dto.request;

import lombok.Getter;

@Getter
public class ChatRoomModificationRequestDto {

    private Long id;
    private String name;
    private String profileUrl;

}