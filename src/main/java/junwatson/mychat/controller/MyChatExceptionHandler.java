package junwatson.mychat.controller;

import junwatson.mychat.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
public class MyChatExceptionHandler {

    public static ResponseEntity<String> handle(RuntimeException e) {
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

        throw e;
    }
}
