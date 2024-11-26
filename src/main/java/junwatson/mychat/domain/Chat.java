package junwatson.mychat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public abstract class Chat implements Comparable<Chat> {

    @Column(nullable = false)
    protected LocalDateTime inputDate;
    @Column(nullable = false)
    protected String content;

    /**
     * 입력 날짜에 대해 내림차순으로 정렬하게 함
     */
    @Override
    public int compareTo(Chat o) {
        if (this.inputDate.isAfter(o.inputDate)) {
            return -1;
        } else if (this.inputDate.isEqual(o.inputDate)) {
            return 0;
        }

        return 1;
    }
}
