package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.BookingConflictException;

@SuppressWarnings("MissingJavadoc")
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
    @SuppressWarnings("MissingJavadoc")
    @Test void anonymousBookingOperationsAreRejected() {
        CROBS crobs = new CROBS();
        assertThrows(AuthorizationException.class, () -> book(crobs, "R1", 9, 10));
        assertThrows(AuthorizationException.class, () -> crobs.modifyBooking("B1", "R1",
                day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Meeting"));
        assertThrows(AuthorizationException.class, () -> crobs.deleteBooking("B1"));
    }
    @SuppressWarnings("MissingJavadoc")
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
    @SuppressWarnings("MissingJavadoc")
    @Test void bookingIdsAreDistinct() {
        CROBS crobs = started();
        Room room = room(crobs);
        Booking first = book(crobs, room.getRoomId(), 9, 10);
        @SuppressWarnings("MagicNumber") Booking second = book(crobs, room.getRoomId(), 10, 11);
        assertNotEquals(first.getBookingId(), second.getBookingId());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void unknownRoomIsRejected() {
        CROBS crobs = started();
        assertThrows(NotFoundException.class, () -> book(crobs, "UNKNOWN", 9, 10));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void overlappingAndIdenticalBookingsAreRejected() {
        CROBS crobs = started();
        Room room = room(crobs);
        //noinspection MagicNumber
        book(crobs, room.getRoomId(), 9, 11);
        //noinspection MagicNumber
        assertThrows(BookingConflictException.class, () -> book(crobs, room.getRoomId(), 10, 12));
        //noinspection MagicNumber
        assertThrows(BookingConflictException.class, () -> book(crobs, room.getRoomId(), 9, 11));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void adjacencyDifferentRoomAndDifferentDateAreAllowed() {
        CROBS crobs = started();
        Room first = room(crobs);
        Room second = room(crobs);
        book(crobs, first.getRoomId(), 9, 10);
        //noinspection MagicNumber
        assertDoesNotThrow(() -> book(crobs, first.getRoomId(), 10, 11));
        assertDoesNotThrow(() -> book(crobs, second.getRoomId(), 9, 10));
        assertDoesNotThrow(() -> crobs.createBooking(first.getRoomId(), day.plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), "Next day"));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void invalidIntervalIsRejectedWithoutOccupyingTheSlot() {
        CROBS crobs = started();
        Room room = room(crobs);
        assertThrows(ValidationException.class, () -> book(crobs, room.getRoomId(), 10, 9));
        assertDoesNotThrow(() -> book(crobs, room.getRoomId(), 9, 10));
    }
    @SuppressWarnings("MissingJavadoc")
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
    @SuppressWarnings("MissingJavadoc")
    @Test void successfulModificationReleasesOldSlot() {
        CROBS crobs = started();
        Room room = room(crobs);
        Booking original = book(crobs, room.getRoomId(), 9, 10);
        //noinspection MagicNumber
        crobs.modifyBooking(original.getBookingId(), room.getRoomId(), day,
                LocalTime.of(11, 0), LocalTime.NOON, "Moved");
        assertDoesNotThrow(() -> book(crobs, room.getRoomId(), 9, 10));
        //noinspection MagicNumber
        assertThrows(BookingConflictException.class, () -> book(crobs, room.getRoomId(), 11, 12));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void rejectedModificationLeavesOriginalReservationInPlace() {
        CROBS crobs = started();
        Room room = room(crobs);
        Booking original = book(crobs, room.getRoomId(), 9, 10);
        //noinspection MagicNumber
        book(crobs, room.getRoomId(), 11, 12);
        //noinspection MagicNumber
        assertThrows(BookingConflictException.class, () -> crobs.modifyBooking(
                original.getBookingId(), room.getRoomId(), day,
                LocalTime.of(11, 30), LocalTime.of(12, 30), "Conflict"));
        assertThrows(BookingConflictException.class, () -> book(crobs, room.getRoomId(), 9, 10));
        //noinspection MagicNumber
        assertThrows(BookingConflictException.class, () -> book(crobs, room.getRoomId(), 11, 12));
        assertDoesNotThrow(() -> crobs.modifyBooking(original.getBookingId(), room.getRoomId(),
                day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Still owned"));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void regularUserCannotModifyOrDeleteAnotherUsersBooking() {
        CROBS crobs = started();
        Room room = room(crobs);
        User owner = crobs.addUser("Owner");
        User other = crobs.addUser("Other");
        crobs.logout(); crobs.login(owner.getUserId());
        Booking booking = book(crobs, room.getRoomId(), 9, 10);
        crobs.logout(); crobs.login(other.getUserId());
        //noinspection MagicNumber
        assertThrows(AuthorizationException.class, () -> crobs.modifyBooking(
                booking.getBookingId(), room.getRoomId(), day,
                LocalTime.of(11, 0), LocalTime.NOON, "Not mine"));
        assertThrows(AuthorizationException.class, () -> crobs.deleteBooking(booking.getBookingId()));
        assertThrows(BookingConflictException.class, () -> book(crobs, room.getRoomId(), 9, 10));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void evenSuperAdminCannotModifyOrDeleteAnotherUsersBooking() {
        CROBS crobs = started();
        Room room = room(crobs);
        User owner = crobs.addUser("Owner");
        crobs.logout(); crobs.login(owner.getUserId());
        Booking booking = book(crobs, room.getRoomId(), 9, 10);
        crobs.logout(); crobs.login("SA001");
        //noinspection MagicNumber
        assertThrows(AuthorizationException.class, () -> crobs.modifyBooking(
                booking.getBookingId(), room.getRoomId(), day,
                LocalTime.of(11, 0), LocalTime.NOON, "Not mine"));
        assertThrows(AuthorizationException.class, () -> crobs.deleteBooking(booking.getBookingId()));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void ownerDeletionReleasesSlot() {
        CROBS crobs = started();
        Room room = room(crobs);
        Booking booking = book(crobs, room.getRoomId(), 9, 10);
        crobs.deleteBooking(booking.getBookingId());
        assertDoesNotThrow(() -> book(crobs, room.getRoomId(), 9, 10));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void missingModificationAndDeletionReportNotFound() {
        CROBS crobs = started();
        Room room = room(crobs);
        assertThrows(NotFoundException.class, () -> crobs.modifyBooking("UNKNOWN",
                room.getRoomId(), day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Missing"));
        assertThrows(NotFoundException.class, () -> crobs.deleteBooking("UNKNOWN"));
    }
    @SuppressWarnings("MissingJavadoc")
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

    @SuppressWarnings("MissingJavadoc")
    @Test
    void anonymousUserCannotListBookings() {
        CROBS crobs = new CROBS();

        assertThrows(AuthorizationException.class,
                crobs::getCurrentUserBookings);
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void currentUserWithoutBookingsGetsEmptyList() {
        CROBS crobs = started();
        User user = crobs.addUser("Alex");

        crobs.logout();
        crobs.login(user.getUserId());

        List<Booking> result = crobs.getCurrentUserBookings();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void currentUserBookingsContainOnlyTheirOwnBookings() {
        CROBS crobs = started();
        Room room = room(crobs);
        User owner = crobs.addUser("Owner");
        User other = crobs.addUser("Other");

        crobs.logout();
        crobs.login(owner.getUserId());
        Booking ownerBooking = book(crobs, room.getRoomId(), 9, 10);

        crobs.logout();
        crobs.login(other.getUserId());
        @SuppressWarnings("MagicNumber") Booking otherBooking = book(crobs, room.getRoomId(), 10, 11);

        List<Booking> otherResult = crobs.getCurrentUserBookings();

        assertEquals(1, otherResult.size());
        assertEquals(otherBooking.getBookingId(),
                otherResult.get(0).getBookingId());
        assertEquals(other.getUserId(),
                otherResult.get(0).getOwnerUserId());

        crobs.logout();
        crobs.login(owner.getUserId());

        List<Booking> ownerResult = crobs.getCurrentUserBookings();

        assertEquals(1, ownerResult.size());
        assertEquals(ownerBooking.getBookingId(),
                ownerResult.get(0).getBookingId());
        assertEquals(owner.getUserId(),
                ownerResult.get(0).getOwnerUserId());
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void currentUserBookingsAreSortedByDateThenStartTime() {
        CROBS crobs = started();
        Room room = room(crobs);

        Booking nextDay = crobs.createBooking(
                room.getRoomId(), day.plusDays(1),
                LocalTime.of(8, 0), LocalTime.of(9, 0),
                "Next day meeting");

        @SuppressWarnings("MagicNumber") Booking laterToday = book(crobs, room.getRoomId(), 14, 15);
        Booking earlierToday = book(crobs, room.getRoomId(), 9, 10);

        List<String> actualIds = crobs.getCurrentUserBookings()
                .stream()
                .map(Booking::getBookingId)
                .collect(Collectors.toList());

        assertEquals(
                Arrays.asList(
                        earlierToday.getBookingId(),
                        laterToday.getBookingId(),
                        nextDay.getBookingId()),
                actualIds);
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void superAdminListingContainsOnlyTheirOwnBookings() {
        CROBS crobs = started();
        Room room = room(crobs);
        User regularUser = crobs.addUser("Regular user");

        Booking adminBooking = book(crobs, room.getRoomId(), 9, 10);

        crobs.logout();
        crobs.login(regularUser.getUserId());
        //noinspection MagicNumber
        book(crobs, room.getRoomId(), 10, 11);

        crobs.logout();
        crobs.login("SA001");

        List<Booking> result = crobs.getCurrentUserBookings();

        assertEquals(1, result.size());
        assertEquals(adminBooking.getBookingId(),
                result.get(0).getBookingId());
        assertEquals("SA001", result.get(0).getOwnerUserId());
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void modifyingReturnedListDoesNotRemoveStoredBookings() {
        CROBS crobs = started();
        Room room = room(crobs);
        Booking booking = book(crobs, room.getRoomId(), 9, 10);

        List<Booking> result = crobs.getCurrentUserBookings();
        result.clear();

        List<Booking> stored = crobs.getCurrentUserBookings();

        assertEquals(1, stored.size());
        assertEquals(booking.getBookingId(),
                stored.get(0).getBookingId());

        assertThrows(BookingConflictException.class,
                () -> book(crobs, room.getRoomId(), 9, 10));
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void deletedBookingIsAbsentFromCurrentUserBookings() {
        CROBS crobs = started();
        Room room = room(crobs);
        Booking deleted = book(crobs, room.getRoomId(), 9, 10);
        @SuppressWarnings("MagicNumber") Booking remaining = book(crobs, room.getRoomId(), 10, 11);

        crobs.deleteBooking(deleted.getBookingId());

        List<Booking> result = crobs.getCurrentUserBookings();

        assertEquals(1, result.size());
        assertEquals(remaining.getBookingId(),
                result.get(0).getBookingId());
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void loggedOutUserCannotListPreviouslyAccessibleBookings() {
        CROBS crobs = started();
        Room room = room(crobs);
        book(crobs, room.getRoomId(), 9, 10);

        assertEquals(1, crobs.getCurrentUserBookings().size());

        crobs.logout();

        assertThrows(AuthorizationException.class,
                crobs::getCurrentUserBookings);
    }
}
