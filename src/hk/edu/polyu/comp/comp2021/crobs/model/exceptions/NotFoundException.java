package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

/**
 * Reports a requested entity that does not exist.
 */
public class NotFoundException extends CrobsException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a detail message.
     *
     * @param message the detail message
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a detail message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
