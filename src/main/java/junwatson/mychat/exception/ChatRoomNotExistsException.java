package junwatson.mychat.exception;

public class ChatRoomNotExistsException extends RuntimeException {
    public ChatRoomNotExistsException(String message) {
        super(message);
    }
}
