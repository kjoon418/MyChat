package junwatson.mychat.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.*;

@NoArgsConstructor(access = PRIVATE)
@Getter
public class MemberInfoRequestDto {

    private String email;
}
