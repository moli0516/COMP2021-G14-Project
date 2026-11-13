package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.*;

@SuppressWarnings("MissingJavadoc")
class PartAExceptionTest {
    @SuppressWarnings("MissingJavadoc")
    @Test void crobsExceptionPreservesMessageAndCause() {
        CrobsException plain = new CrobsException("details");
        assertEquals("details", plain.getMessage());
        assertNull(plain.getCause());
        Throwable cause = new IllegalStateException("cause");
        CrobsException wrapped = new CrobsException("wrapped", cause);
        assertEquals("wrapped", wrapped.getMessage());
        assertSame(cause, wrapped.getCause());
        assertInstanceOf(CrobsException.class, wrapped);
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void authorizationExceptionPreservesMessageAndCause() {
        AuthorizationException plain = new AuthorizationException("details");
        assertEquals("details", plain.getMessage());
        assertNull(plain.getCause());
        Throwable cause = new IllegalStateException("cause");
        AuthorizationException wrapped = new AuthorizationException("wrapped", cause);
        assertEquals("wrapped", wrapped.getMessage());
        assertSame(cause, wrapped.getCause());
        assertInstanceOf(CrobsException.class, wrapped);
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void notFoundExceptionPreservesMessageAndCause() {
        NotFoundException plain = new NotFoundException("details");
        assertEquals("details", plain.getMessage());
        assertNull(plain.getCause());
        Throwable cause = new IllegalStateException("cause");
        NotFoundException wrapped = new NotFoundException("wrapped", cause);
        assertEquals("wrapped", wrapped.getMessage());
        assertSame(cause, wrapped.getCause());
        assertInstanceOf(CrobsException.class, wrapped);
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void validationExceptionPreservesMessageAndCause() {
        ValidationException plain = new ValidationException("details");
        assertEquals("details", plain.getMessage());
        assertNull(plain.getCause());
        Throwable cause = new IllegalStateException("cause");
        ValidationException wrapped = new ValidationException("wrapped", cause);
        assertEquals("wrapped", wrapped.getMessage());
        assertSame(cause, wrapped.getCause());
        assertInstanceOf(CrobsException.class, wrapped);
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void bookingConflictExceptionPreservesMessageAndCause() {
        BookingConflictException plain = new BookingConflictException("details");
        assertEquals("details", plain.getMessage());
        assertNull(plain.getCause());
        Throwable cause = new IllegalStateException("cause");
        BookingConflictException wrapped = new BookingConflictException("wrapped", cause);
        assertEquals("wrapped", wrapped.getMessage());
        assertSame(cause, wrapped.getCause());
        assertInstanceOf(CrobsException.class, wrapped);
    }
}
