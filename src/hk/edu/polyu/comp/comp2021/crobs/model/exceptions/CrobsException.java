package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

/**
 * Base unchecked exception for CROBS domain failures.
 */
public class CrobsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a detail message.
     *
     * @param message the detail message
     */
    public CrobsException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a detail message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public CrobsException(String message, Throwable cause) {
        super(message, cause);
    }
}
