package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    @Test
    void successResultIsValidWithoutMessage() {
        ValidationResult result = ValidationResult.success();

        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    void failureResultIsInvalidWithMessage() {
        ValidationResult result = ValidationResult.failure("Invalid room");

        assertFalse(result.isValid());
        assertEquals("Invalid room", result.getMessage());
    }
}
