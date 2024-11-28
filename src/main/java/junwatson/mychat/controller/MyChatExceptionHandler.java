package junwatson.mychat.controller;

import junwatson.mychat.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.sql.SQLIntegrityConstraintViolationException;

import static org.springframework.http.HttpStatus.*;

@Slf4j
public class MyChatExceptionHandler {

    public static ResponseEntity<String> handle(Exception e) {
        log.info("MyChatExceptionHandler.handle() called");

        if (e instanceof IllegalArgumentException ||
                e instanceof IllegalMemberStateException ||
                e instanceof IllegalRefreshTokenException ||
                e instanceof BlockException ||
                e instanceof IllegalChatRoomStateException ||
                e instanceof IllegalSearchConditionException) {
            return ResponseEntity.status(BAD_REQUEST).body(e.getMessage());
        }
        if (e instanceof MemberNotExistsException ||
                e instanceof ChatNotExistsException ||
                e instanceof ChatRoomNotExistsException) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        }
        if (e instanceof SQLIntegrityConstraintViolationException) {
            return ResponseEntity.status(BAD_REQUEST).body("다른 사용자가 이미 사용하고 있는 값이거나, 이미 처리된 요청입니다.");
        }

        log.error("Unhandled error occurred", e);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("서버 측에 문제가 발생했습니다.");
    }
}
