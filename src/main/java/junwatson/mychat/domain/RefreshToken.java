package junwatson.mychat.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String token;

    @Builder
    private RefreshToken(String token) {
        this.token = token;
    }
}
