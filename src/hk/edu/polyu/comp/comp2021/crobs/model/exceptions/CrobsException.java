package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

public class CrobsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CrobsException(String message) {
        super(message);
    }

    public CrobsException(String message, Throwable cause) {
        super(message, cause);
    }
}
