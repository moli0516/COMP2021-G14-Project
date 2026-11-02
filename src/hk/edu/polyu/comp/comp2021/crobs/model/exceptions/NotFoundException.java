package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

public class NotFoundException extends CrobsException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
