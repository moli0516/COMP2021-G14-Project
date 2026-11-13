package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Collections;
import java.time.LocalDate;
import java.time.LocalTime;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

@SuppressWarnings({"MagicNumber", "MissingJavadoc"})
class BookingRepositoryTest {
    private Booking value(String id, String name) { return new Booking(id, name, "R1", LocalDate.of(2026, 11, 10), LocalTime.of(9, 0), LocalTime.of(10, 0), "Meeting", false); }
    @SuppressWarnings("MissingJavadoc")
    @Test void addAndRequireReturnStoredValue() {
        BookingRepository repo = new BookingRepository();
        Booking value = value("X1", "first");
        repo.add(value);
        assertSame(value, repo.require("X1"));
        assertEquals(1, repo.findAll().size());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void missingLookupThrowsNotFound() {
        assertThrows(NotFoundException.class,
                () -> new BookingRepository().require("missing"));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void duplicateAddIsRejectedWithoutReplacingOriginal() {
        BookingRepository repo = new BookingRepository();
        Booking original = value("X1", "first");
        repo.add(original);
        assertThrows(ValidationException.class, () -> repo.add(value("X1", "second")));
        assertSame(original, repo.require("X1"));
        assertEquals(1, repo.findAll().size());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void replacementChangesStoredValue() {
        BookingRepository repo = new BookingRepository();
        repo.add(value("X1", "first"));
        Booking replacement = value("X1", "second");
        repo.replace(replacement);
        assertSame(replacement, repo.require("X1"));
        assertEquals(1, repo.findAll().size());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void missingReplacementThrowsWithoutAdding() {
        BookingRepository repo = new BookingRepository();
        assertThrows(NotFoundException.class, () -> repo.replace(value("X1", "first")));
        assertTrue(repo.findAll().isEmpty());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void removeDeletesStoredValue() {
        BookingRepository repo = new BookingRepository();
        repo.add(value("X1", "first"));
        repo.remove("X1");
        assertTrue(repo.findAll().isEmpty());
        assertThrows(NotFoundException.class, () -> repo.require("X1"));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void missingRemovalThrows() {
        assertThrows(NotFoundException.class,
                () -> new BookingRepository().remove("missing"));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void listIsDetachedFromLaterChanges() {
        BookingRepository repo = new BookingRepository();
        repo.add(value("X1", "first"));
        List<Booking> snapshot = repo.findAll();
        repo.add(value("X2", "second"));
        assertEquals(1, snapshot.size());
        assertEquals(2, repo.findAll().size());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void changingReturnedListCannotChangeRepository() {
        BookingRepository repo = new BookingRepository();
        repo.add(value("X1", "first"));
        List<Booking> result = repo.findAll();
        try { result.clear(); } catch (UnsupportedOperationException expected) {
            // An unmodifiable snapshot is also acceptable.
        }
        assertEquals(1, repo.findAll().size());
    }

    @SuppressWarnings("MissingJavadoc")
    @Test void ownerAndRoomQueriesFilterAndIncludeCancelledRecords() {
        BookingRepository repo = new BookingRepository();
        Booking a = value("B1", "U1");
        Booking b = new Booking("B2", "U2", "R2", LocalDate.of(2026, 11, 10),
                LocalTime.of(11, 0), LocalTime.NOON, "Meeting", true);
        repo.add(a); repo.add(b);
        assertEquals(Collections.singletonList(a), repo.findByOwner("U1"));
        assertEquals(Collections.singletonList(b), repo.findByOwner("U2"));
        assertEquals(Collections.singletonList(a), repo.findByRoom("R1"));
        assertEquals(Collections.singletonList(b), repo.findByRoom("R2"));
        assertTrue(repo.findByOwner("unknown").isEmpty());
        assertTrue(repo.findByRoom("unknown").isEmpty());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void queryListsAreDetached() {
        BookingRepository repo = new BookingRepository();
        repo.add(value("B1", "U1"));
        List<Booking> owners = repo.findByOwner("U1");
        List<Booking> rooms = repo.findByRoom("R1");
        repo.add(value("B2", "U1"));
        assertEquals(1, owners.size());
        assertEquals(1, rooms.size());
    }
}
