package junwatson.mychat.exception;

public class ChatNotExistsException extends RuntimeException {
    public ChatNotExistsException(String message) {
        super(message);
    }
}
