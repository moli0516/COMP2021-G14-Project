package hk.edu.polyu.comp.comp2021.crobs.model;

/** Optional validation value; avoid duplicating exception-based error handling. */
public final class ValidationResult {
    private final boolean valid;
    private final String message;

    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public static ValidationResult success() {
        throw new UnsupportedOperationException("TODO: construct success result");
    }

    public static ValidationResult failure(String message) {
        throw new UnsupportedOperationException("TODO: construct failure result");
    }

    public boolean isValid() { return valid; }
    public String getMessage() { return message; }
}
