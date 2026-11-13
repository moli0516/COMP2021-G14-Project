package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

/**
 * Reports an operation denied by the authorization policy.
 */
public class AuthorizationException extends CrobsException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a detail message.
     *
     * @param message the detail message
     */
    public AuthorizationException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a detail message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
