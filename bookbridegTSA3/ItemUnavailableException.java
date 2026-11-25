package bookbridge.exception;

public class ItemUnavailableException extends Exception {

    public ItemUnavailableException(String message) {
        super(message);
    }

    public ItemUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
