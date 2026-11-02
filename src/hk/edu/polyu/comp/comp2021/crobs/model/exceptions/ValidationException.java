package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

public class ValidationException extends CrobsException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
