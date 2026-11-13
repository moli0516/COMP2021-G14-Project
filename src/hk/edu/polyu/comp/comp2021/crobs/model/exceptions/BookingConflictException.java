package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

/**
 * Reports a booking interval that conflicts with an existing booking.
 */
public class BookingConflictException extends CrobsException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a detail message.
     *
     * @param message the detail message
     */
    public BookingConflictException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a detail message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public BookingConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
