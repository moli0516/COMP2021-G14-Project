package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

/**
 * Reports invalid input or a violated domain constraint.
 */
public class ValidationException extends CrobsException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a detail message.
     *
     * @param message the detail message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a detail message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
