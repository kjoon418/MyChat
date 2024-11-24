package junwatson.mychat.exception;

public class IllegalChatRoomStateException extends RuntimeException {
    public IllegalChatRoomStateException(String message) {
        super(message);
    }
}
