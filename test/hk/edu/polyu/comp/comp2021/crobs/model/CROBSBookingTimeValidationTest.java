package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.controller.CommandParser;
import hk.edu.polyu.comp.comp2021.crobs.controller.CommandProcessor;
import hk.edu.polyu.comp.comp2021.crobs.web.WebCommandResult;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

/** Deterministic reservation-time, CLI, Web-result, and history regressions. */
class CROBSBookingTimeValidationTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 15);
    private static final Clock NOON = Clock.fixed(
            Instant.parse("2026-11-15T12:00:00Z"), ZoneOffset.UTC);
    private CROBS model;
    private Room room;
    private CommandProcessor processor;

    /** Creates an authenticated system at fixed server-local noon. */
    @BeforeEach
    void setUp() {
        model = new CROBS(NOON);
        model.login("SA001");
        room = model.addRoom(RoomType.MEETING_ROOM, 8, Collections.emptySet());
        processor = new CommandProcessor(new CommandParser(model));
    }

    /**
     * Creates a reservation using explicit date and time values.
     *
     * @param date the proposed local date
     * @param start the proposed inclusive start
     * @param end the proposed exclusive end
     * @return the stored reservation
     */
    private Booking book(LocalDate date, LocalTime start, LocalTime end) {
        return model.createBooking(room.getRoomId(), date, start, end, "Meeting");
    }

    /**
     * Formats a booking command using the real fixture room ID.
     *
     * @param date the date token
     * @param start the start token
     * @param end the end token
     * @return the complete command line
     */
    private String command(String date, String start, String end) {
        return "book " + room.getRoomId() + " " + date + " " + start + " " + end + " Meeting";
    }

    /** Rejects a past date before allocating a booking ID or storing data. */
    @Test
    void pastDateDoesNotConsumeBookingId() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> book(TODAY.minusDays(1), LocalTime.of(13, 0), LocalTime.of(14, 0)));
        assertEquals("Booking date must not be in the past", error.getMessage());
        assertTrue(model.getCurrentUserBookings().isEmpty());
        assertEquals("B000001", book(TODAY.plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0)).getBookingId());
    }

    /** Rejects elapsed starts even when the proposed end is still future. */
    @Test
    void elapsedStartTodayIsRejected() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> book(TODAY, LocalTime.of(11, 0), LocalTime.of(13, 0)));
        assertEquals("Booking start time must be in the future", error.getMessage());
    }

    /** Requires the start to be strictly after the clock time. */
    @Test
    void exactNowIsRejected() {
        assertThrows(ValidationException.class,
                () -> book(TODAY, LocalTime.NOON, LocalTime.of(13, 0)));
    }

    /** Allows later today and early tomorrow without a one-day delay. */
    @Test
    void laterTodayAndTomorrowAreAllowed() {
        assertDoesNotThrow(() -> book(TODAY, LocalTime.of(13, 0), LocalTime.of(14, 0)));
        assertDoesNotThrow(() -> book(TODAY.plusDays(1), LocalTime.MIDNIGHT, LocalTime.of(1, 0)));
    }

    /** Rejects null temporal arguments without repository mutation. */
    @Test
    void nullValuesAreValidationErrors() {
        assertThrows(ValidationException.class,
                () -> book(null, LocalTime.of(13, 0), LocalTime.of(14, 0)));
        assertThrows(ValidationException.class,
                () -> book(TODAY, null, LocalTime.of(14, 0)));
        assertThrows(ValidationException.class,
                () -> book(TODAY, LocalTime.of(13, 0), null));
        assertTrue(model.getCurrentUserBookings().isEmpty());
    }

    /** Rejects equal and reversed endpoints on an empty booking repository. */
    @Test
    void invalidIntervalsAreRejected() {
        assertThrows(ValidationException.class,
                () -> book(TODAY, LocalTime.of(13, 0), LocalTime.of(13, 0)));
        assertThrows(ValidationException.class,
                () -> book(TODAY, LocalTime.of(14, 0), LocalTime.of(13, 0)));
        assertTrue(model.getCurrentUserBookings().isEmpty());
    }

    /** Keeps the original reservation after rejected temporal modifications. */
    @Test
    void failedModificationPreservesOriginal() {
        Booking original = book(TODAY.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        assertThrows(ValidationException.class, () -> model.modifyBooking(
                original.getBookingId(), room.getRoomId(), TODAY.minusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), "Changed"));
        assertSame(original, model.getCurrentUserBookings().get(0));
        assertThrows(ValidationException.class, () -> model.modifyBooking(
                original.getBookingId(), room.getRoomId(), TODAY,
                LocalTime.of(11, 0), LocalTime.of(13, 0), "Changed"));
        assertSame(original, model.getCurrentUserBookings().get(0));
    }

    /** Allows a future proposed start while preserving the booking ID. */
    @Test
    void validModificationPreservesIdentity() {
        Booking original = book(TODAY.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        Booking updated = model.modifyBooking(original.getBookingId(), room.getRoomId(), TODAY,
                LocalTime.of(13, 0), LocalTime.of(14, 0), "Changed");
        assertEquals(original.getBookingId(), updated.getBookingId());
        assertEquals(TODAY, updated.getDate());
    }

    /** Returns specific reservation-time errors through the shared processor. */
    @Test
    void cliReportsSpecificMessages() {
        assertEquals("Error: Booking date must not be in the past",
                processor.process(command("0001-01-01", "09:00", "10:00")));
        assertEquals("Error: Booking start time must be in the future",
                processor.process(command("2026-11-15", "11:00", "13:00")));
        assertEquals("Error: Start time must be before end time",
                processor.process(command("2026-11-16", "10:00", "09:00")));
        assertTrue(model.getCurrentUserBookings().isEmpty());
    }

    /** Sets Web ok false while preserving the CLI-compatible error text. */
    @Test
    void webEnvelopeReportsFailure() {
        String line = command("0001-01-01", "09:00", "10:00");
        String output = processor.process(line);
        String json = WebCommandResult.toJson(line, output, model);
        assertTrue(json.contains("\"ok\":false"));
        assertTrue(json.contains("Error: Booking date must not be in the past"));
        assertTrue(json.contains("\"command\":\"book\""));
    }

    /** Returns a failed Web modification envelope without changing the booking. */
    @Test
    void webModificationFailurePreservesData() {
        Booking original = book(TODAY.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        String line = "modifyBooking " + original.getBookingId() + " " + room.getRoomId()
                + " 0001-01-01 09:00 10:00 Changed";
        String output = processor.process(line);
        assertEquals("Error: Booking date must not be in the past", output);
        assertTrue(WebCommandResult.toJson(line, output, model).contains("\"ok\":false"));
        assertSame(original, model.getCurrentUserBookings().get(0));
    }

    /** Keeps redo history when a new reservation fails time validation. */
    @Test
    void failedValidationPreservesRedo() {
        assertFalse(processor.process(command("2026-11-16", "09:00", "10:00")).startsWith("Error:"));
        assertFalse(processor.process("undo").startsWith("Error:"));
        assertTrue(model.canRedo());
        assertTrue(processor.process(command("0001-01-01", "09:00", "10:00")).startsWith("Error:"));
        assertTrue(model.canRedo());
        assertFalse(processor.process("redo").startsWith("Error:"));
        assertEquals(1, model.getCurrentUserBookings().size());
    }

    /** Uses server-local date when it differs from the UTC date. */
    @Test
    void clockZoneDefinesToday() {
        CROBS hongKong = new CROBS(Clock.fixed(Instant.parse("2026-11-14T18:00:00Z"),
                ZoneId.of("Asia/Hong_Kong")));
        hongKong.login("SA001");
        Room target = hongKong.addRoom(RoomType.MEETING_ROOM, 8, Collections.emptySet());
        assertThrows(ValidationException.class, () -> hongKong.createBooking(target.getRoomId(),
                TODAY.minusDays(1), LocalTime.of(23, 0), LocalTime.of(23, 30), "Past"));
        assertDoesNotThrow(() -> hongKong.createBooking(target.getRoomId(), TODAY,
                LocalTime.of(3, 0), LocalTime.of(4, 0), "Future"));
    }

    /** Keeps historical entity construction independent of current time. */
    @Test
    void historicalEntitiesRemainValid() {
        assertDoesNotThrow(() -> new Booking("B1", "U1", "R1", TODAY.minusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), "Historical", false));
    }

    /** Retains date-based cancellation for today and future reservations. */
    @Test
    void roomDeletionCancelsTodayAndFuture() {
        book(TODAY, LocalTime.of(13, 0), LocalTime.of(14, 0));
        book(TODAY.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        model.deleteRoom(room.getRoomId());
        assertEquals(2, model.getCurrentUserBookings().size());
        assertTrue(model.getCurrentUserBookings().stream().allMatch(Booking::isCancelled));
    }

    /** Rejects a null injected clock explicitly at construction. */
    @Test
    void nullClockIsRejected() {
        assertThrows(NullPointerException.class, () -> new CROBS(null));
    }
}
