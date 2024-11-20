package junwatson.mychat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
public class Chat {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String content;
    private LocalDateTime input_date;
}
