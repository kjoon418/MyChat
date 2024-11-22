package junwatson.mychat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.*;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class BlacklistInfoRequestDto {

    private String email;
}
