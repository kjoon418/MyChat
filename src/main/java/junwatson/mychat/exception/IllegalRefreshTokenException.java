package junwatson.mychat.exception;

public class IllegalRefreshTokenException extends RuntimeException {
    public IllegalRefreshTokenException(String message) {
        super(message);
    }
}
