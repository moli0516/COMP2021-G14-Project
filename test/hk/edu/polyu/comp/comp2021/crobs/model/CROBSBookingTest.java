package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.BookingConflictException;

class CROBSBookingTest {
    private final LocalDate day = LocalDate.of(2026, 11, 10);

    private CROBS started() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        return crobs;
    }
    private Room room(CROBS crobs) {
        return crobs.addRoom(RoomType.MEETING_ROOM, 8, Collections.singleton("projector"));
    }
    private Booking book(CROBS crobs, String roomId, int start, int end) {
        return crobs.createBooking(roomId, day, LocalTime.of(start, 0),
                LocalTime.of(end, 0), "Project meeting");
    }
    @Test void anonymousBookingOperationsAreRejected() {
        CROBS crobs = new CROBS();
        assertThrows(AuthorizationException.class, () -> book(crobs, "R1", 9, 10));
        assertThrows(AuthorizationException.class, () -> crobs.modifyBooking("B1", "R1",
                day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Meeting"));
        assertThrows(AuthorizationException.class, () -> crobs.deleteBooking("B1"));
    }
    @Test void newBookingIsActiveAndOwnedByCurrentUser() {
        CROBS crobs = started();
        Room room = room(crobs);
        User owner = crobs.addUser("Alex");
        crobs.logout(); crobs.login(owner.getUserId());
        Booking booking = book(crobs, room.getRoomId(), 9, 10);
        assertFalse(booking.isCancelled());
        assertEquals(owner.getUserId(), booking.getOwnerUserId());
        assertEquals(room.getRoomId(), booking.getRoomId());
        assertNotNull(booking.getBookingId());
        assertFalse(booking.getBookingId().trim().isEmpty());
    }
    @Test void bookingIdsAreDistinct() {
        CROBS crobs = started();
        Room room = room(crobs);
        Booking first = book(crobs, room.getRoomId(), 9, 10);
        Booking second = book(crobs, room.getRoomId(), 10, 11);
        assertNotEquals(first.getBookingId(), second.getBookingId());
    }
    @Test void unknownRoomIsRejected() {
        CROBS crobs = started();
        assertThrows(NotFoundException.class, () -> book(crobs, "UNKNOWN", 9, 10));
    }
    @Test void overlappingAndIdenticalBookingsAreRejected() {
        CROBS crobs = started();
        Room room = room(crobs);
        book(crobs, room.getRoomId(), 9, 11);
        assertThrows(BookingConflictException.class, () -> book(crobs, room.getRoomId(), 10, 12));
        assertThrows(BookingConflictException.class, () -> book(crobs, room.getRoomId(), 9, 11));
    }
    @Test void adjacencyDifferentRoomAndDifferentDateAreAllowed() {
        CROBS crobs = started();
        Room first = room(crobs);
        Room second = room(crobs);
        book(crobs, first.getRoomId(), 9, 10);
        assertDoesNotThrow(() -> book(crobs, first.getRoomId(), 10, 11));
        assertDoesNotThrow(() -> book(crobs, second.getRoomId(), 9, 10));
        assertDoesNotThrow(() -> crobs.createBooking(first.getRoomId(), day.plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), "Next day"));
    }
    @Test void invalidIntervalIsRejectedWithoutOccupyingTheSlot() {
        CROBS crobs = started();
        Room room = room(crobs);
        assertThrows(ValidationException.class, () -> book(crobs, room.getRoomId(), 10, 9));
        assertDoesNotThrow(() -> book(crobs, room.getRoomId(), 9, 10));
    }
    @Test void modificationPreservesOwnerAndIdAndExcludesSelf() {
        CROBS crobs = started();
        Room room = room(crobs);
        Booking original = book(crobs, room.getRoomId(), 9, 10);
        Booking updated = crobs.modifyBooking(original.getBookingId(), room.getRoomId(),
                day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Updated purpose");
        assertEquals(original.getBookingId(), updated.getBookingId());
        assertEquals(original.getOwnerUserId(), updated.getOwnerUserId());
        assertEquals("Updated purpose", updated.getPurpose());
        assertFalse(updated.isCancelled());
    }
    @Test void successfulModificationReleasesOldSlot() {
        CROBS crobs = started();
        Room room = room(crobs);
        Booking original = book(crobs, room.getRoomId(), 9, 10);
        crobs.modifyBooking(original.getBookingId(), room.getRoomId(), day,
                LocalTime.of(11, 0), LocalTime.NOON, "Moved");
        assertDoesNotThrow(() -> book(crobs, room.getRoomId(), 9, 10));
        assertThrows(BookingConflictException.class, () -> book(crobs, room.getRoomId(), 11, 12));
    }
    @Test void rejectedModificationLeavesOriginalReservationInPlace() {
        CROBS crobs = started();
        Room room = room(crobs);
        Booking original = book(crobs, room.getRoomId(), 9, 10);
        book(crobs, room.getRoomId(), 11, 12);
        assertThrows(BookingConflictException.class, () -> crobs.modifyBooking(
                original.getBookingId(), room.getRoomId(), day,
                LocalTime.of(11, 30), LocalTime.of(12, 30), "Conflict"));
        assertThrows(BookingConflictException.class, () -> book(crobs, room.getRoomId(), 9, 10));
        assertThrows(BookingConflictException.class, () -> book(crobs, room.getRoomId(), 11, 12));
        assertDoesNotThrow(() -> crobs.modifyBooking(original.getBookingId(), room.getRoomId(),
                day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Still owned"));
    }
    @Test void regularUserCannotModifyOrDeleteAnotherUsersBooking() {
        CROBS crobs = started();
        Room room = room(crobs);
        User owner = crobs.addUser("Owner");
        User other = crobs.addUser("Other");
        crobs.logout(); crobs.login(owner.getUserId());
        Booking booking = book(crobs, room.getRoomId(), 9, 10);
        crobs.logout(); crobs.login(other.getUserId());
        assertThrows(AuthorizationException.class, () -> crobs.modifyBooking(
                booking.getBookingId(), room.getRoomId(), day,
                LocalTime.of(11, 0), LocalTime.NOON, "Not mine"));
        assertThrows(AuthorizationException.class, () -> crobs.deleteBooking(booking.getBookingId()));
        assertThrows(BookingConflictException.class, () -> book(crobs, room.getRoomId(), 9, 10));
    }
    @Test void evenSuperAdminCannotModifyOrDeleteAnotherUsersBooking() {
        CROBS crobs = started();
        Room room = room(crobs);
        User owner = crobs.addUser("Owner");
        crobs.logout(); crobs.login(owner.getUserId());
        Booking booking = book(crobs, room.getRoomId(), 9, 10);
        crobs.logout(); crobs.login("SA001");
        assertThrows(AuthorizationException.class, () -> crobs.modifyBooking(
                booking.getBookingId(), room.getRoomId(), day,
                LocalTime.of(11, 0), LocalTime.NOON, "Not mine"));
        assertThrows(AuthorizationException.class, () -> crobs.deleteBooking(booking.getBookingId()));
    }
    @Test void ownerDeletionReleasesSlot() {
        CROBS crobs = started();
        Room room = room(crobs);
        Booking booking = book(crobs, room.getRoomId(), 9, 10);
        crobs.deleteBooking(booking.getBookingId());
        assertDoesNotThrow(() -> book(crobs, room.getRoomId(), 9, 10));
    }
    @Test void missingModificationAndDeletionReportNotFound() {
        CROBS crobs = started();
        Room room = room(crobs);
        assertThrows(NotFoundException.class, () -> crobs.modifyBooking("UNKNOWN",
                room.getRoomId(), day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Missing"));
        assertThrows(NotFoundException.class, () -> crobs.deleteBooking("UNKNOWN"));
    }
    @Test void removingRegularUserReleasesTheirReservations() {
        CROBS crobs = started();
        Room room = room(crobs);
        User owner = crobs.addUser("Owner");
        crobs.logout(); crobs.login(owner.getUserId());
        book(crobs, room.getRoomId(), 9, 10);
        crobs.logout(); crobs.login("SA001");
        crobs.removeUser(owner.getUserId());
        assertDoesNotThrow(() -> book(crobs, room.getRoomId(), 9, 10));
    }
}
