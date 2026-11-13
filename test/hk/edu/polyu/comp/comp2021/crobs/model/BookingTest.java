package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalTime;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

@SuppressWarnings("MissingJavadoc")
class BookingTest {
    @SuppressWarnings("MagicNumber")
    private Booking booking() {
        return new Booking("B1", "U1", "R1", LocalDate.of(2026, 11, 10),
                LocalTime.of(9, 0), LocalTime.of(10, 0), "Meeting", false);
    }
    @SuppressWarnings({"MagicNumber", "MissingJavadoc"})
    @Test void constructorStoresAllFields() {
        Booking b = booking();
        assertAll(() -> assertEquals("B1", b.getBookingId()),
                () -> assertEquals("U1", b.getOwnerUserId()),
                () -> assertEquals("R1", b.getRoomId()),
                () -> assertEquals(LocalDate.of(2026, 11, 10), b.getDate()),
                () -> assertEquals(LocalTime.of(9, 0), b.getStartTime()),
                () -> assertEquals(LocalTime.of(10, 0), b.getEndTime()),
                () -> assertEquals("Meeting", b.getPurpose()),
                () -> assertFalse(b.isCancelled()));
    }
    @SuppressWarnings({"MagicNumber", "MissingJavadoc"})
    @Test void partialOverlapOnEitherSideIsDetected() {
        assertTrue(booking().overlaps(LocalTime.of(8, 30), LocalTime.of(9, 30)));
        assertTrue(booking().overlaps(LocalTime.of(9, 30), LocalTime.of(10, 30)));
    }
    @SuppressWarnings({"MagicNumber", "MissingJavadoc"})
    @Test void containingContainedAndIdenticalIntervalsOverlap() {
        assertTrue(booking().overlaps(LocalTime.of(8, 0), LocalTime.of(11, 0)));
        assertTrue(booking().overlaps(LocalTime.of(9, 15), LocalTime.of(9, 45)));
        assertTrue(booking().overlaps(LocalTime.of(9, 0), LocalTime.of(10, 0)));
    }
    @SuppressWarnings({"MagicNumber", "MissingJavadoc"})
    @Test void adjacentAndSeparatedIntervalsDoNotOverlap() {
        assertFalse(booking().overlaps(LocalTime.of(8, 0), LocalTime.of(9, 0)));
        assertFalse(booking().overlaps(LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertFalse(booking().overlaps(LocalTime.of(6, 0), LocalTime.of(7, 0)));
        assertFalse(booking().overlaps(LocalTime.of(12, 0), LocalTime.of(13, 0)));
    }
    @SuppressWarnings({"MagicNumber", "MissingJavadoc"})
    @Test void detailsReplacementPreservesIdentityAndOriginal() {
        Booking original = booking();
        Booking changed = original.withDetails("R2", LocalDate.of(2026, 11, 11),
                LocalTime.of(14, 0), LocalTime.of(15, 0), "New purpose");
        assertAll(() -> assertEquals("B1", changed.getBookingId()),
                () -> assertEquals("U1", changed.getOwnerUserId()),
                () -> assertEquals("R2", changed.getRoomId()),
                () -> assertEquals(LocalDate.of(2026, 11, 11), changed.getDate()),
                () -> assertEquals(LocalTime.of(14, 0), changed.getStartTime()),
                () -> assertEquals(LocalTime.of(15, 0), changed.getEndTime()),
                () -> assertEquals("New purpose", changed.getPurpose()),
                () -> assertFalse(changed.isCancelled()),
                () -> assertEquals("R1", original.getRoomId()),
                () -> assertEquals("Meeting", original.getPurpose()));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void cancellationPreservesDetailsAndOriginal() {
        Booking original = booking();
        Booking cancelled = original.cancel();
        assertTrue(cancelled.isCancelled());
        assertFalse(original.isCancelled());
        assertEquals(original.getBookingId(), cancelled.getBookingId());
        assertEquals(original.getOwnerUserId(), cancelled.getOwnerUserId());
        assertEquals(original.getRoomId(), cancelled.getRoomId());
        assertEquals(original.getDate(), cancelled.getDate());
        assertEquals(original.getStartTime(), cancelled.getStartTime());
        assertEquals(original.getEndTime(), cancelled.getEndTime());
        assertEquals(original.getPurpose(), cancelled.getPurpose());
    }
    @SuppressWarnings({"MagicNumber", "MissingJavadoc"})
    @Test void equalAndReversedTimesAreRejected() {
        assertThrows(ValidationException.class, () -> new Booking("B", "U", "R",
                LocalDate.of(2026, 11, 10), LocalTime.NOON, LocalTime.NOON, "P", false));
        assertThrows(ValidationException.class, () -> new Booking("B", "U", "R",
                LocalDate.of(2026, 11, 10), LocalTime.NOON, LocalTime.of(11, 0), "P", false));
    }
    @SuppressWarnings({"MagicNumber", "MissingJavadoc"})
    @Test void blankPurposeAndNullDateAreRejected() {
        assertThrows(ValidationException.class, () -> new Booking("B", "U", "R",
                LocalDate.of(2026, 11, 10), LocalTime.of(9, 0), LocalTime.of(10, 0), " ", false));
        assertThrows(ValidationException.class, () -> new Booking("B", "U", "R",
                null, LocalTime.of(9, 0), LocalTime.of(10, 0), "P", false));
    }
    @SuppressWarnings({"MagicNumber", "MissingJavadoc"})
    @Test void blankIdsAndNullTimesAreRejected() {
        assertThrows(ValidationException.class, () -> new Booking(" ", "U", "R",
                LocalDate.of(2026, 11, 10), LocalTime.of(9, 0), LocalTime.of(10, 0), "P", false));
        assertThrows(ValidationException.class, () -> new Booking("B", " ", "R",
                LocalDate.of(2026, 11, 10), LocalTime.of(9, 0), LocalTime.of(10, 0), "P", false));
        assertThrows(ValidationException.class, () -> new Booking("B", "U", " ",
                LocalDate.of(2026, 11, 10), LocalTime.of(9, 0), LocalTime.of(10, 0), "P", false));
        assertThrows(ValidationException.class, () -> new Booking("B", "U", "R",
                LocalDate.of(2026, 11, 10), null, LocalTime.of(10, 0), "P", false));
        assertThrows(ValidationException.class, () -> new Booking("B", "U", "R",
                LocalDate.of(2026, 11, 10), LocalTime.of(9, 0), null, "P", false));
    }
}
