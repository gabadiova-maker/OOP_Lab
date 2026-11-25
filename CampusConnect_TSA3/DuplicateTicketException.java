package campusconnect.exception;

public class DuplicateTicketException extends Exception {

    public DuplicateTicketException(String message) {
        super(message);
    }

    public DuplicateTicketException(String message, Throwable cause) {
        super(message, cause);
    }
}
