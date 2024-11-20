package junwatson.mychat.exception;

public class MemberNotExistsException extends RuntimeException {
    public MemberNotExistsException(String message) {
        super(message);
    }
}
