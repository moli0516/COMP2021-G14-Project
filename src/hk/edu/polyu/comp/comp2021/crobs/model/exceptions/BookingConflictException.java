package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

public class BookingConflictException extends CrobsException {
    private static final long serialVersionUID = 1L;

    public BookingConflictException(String message) {
        super(message);
    }

    public BookingConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
