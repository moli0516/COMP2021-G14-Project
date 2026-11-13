package hk.edu.polyu.comp.comp2021.crobs.model;

/** Optional validation value; avoid duplicating exception-based error handling. */
public final class ValidationResult {
    private final boolean valid;
    private final String message;

    /**
     * Creates a validation result.
     *
     * @param valid whether validation succeeded
     * @param message the optional failure message
     */
    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    /**
     * Creates a successful validation result.
     *
     * @return a valid result with a null message
     */
    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    /**
     * Creates a failed validation result.
     *
     * @param message the failure message, stored without validation
     * @return an invalid result with the supplied message
     */
    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }

    /**
     * Reports whether validation succeeded.
     *
     * @return the stored validity flag
     */
    public boolean isValid() { return valid; }
    /**
     * Returns the validation message.
     *
     * @return the stored message, which may be null
     */
    public String getMessage() { return message; }
}
