package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Collections;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

@SuppressWarnings("MissingJavadoc")
class RoomRepositoryTest {
    private Room value(String id, String name) { return new Room(id, RoomType.MEETING_ROOM, 8, new FeatureSet(Collections.singleton(name))); }
    @SuppressWarnings("MissingJavadoc")
    @Test void addAndRequireReturnStoredValue() {
        RoomRepository repo = new RoomRepository();
        Room value = value("X1", "first");
        repo.add(value);
        assertSame(value, repo.require("X1"));
        assertEquals(1, repo.findAll().size());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void missingLookupThrowsNotFound() {
        assertThrows(NotFoundException.class,
                () -> new RoomRepository().require("missing"));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void duplicateAddIsRejectedWithoutReplacingOriginal() {
        RoomRepository repo = new RoomRepository();
        Room original = value("X1", "first");
        repo.add(original);
        assertThrows(ValidationException.class, () -> repo.add(value("X1", "second")));
        assertSame(original, repo.require("X1"));
        assertEquals(1, repo.findAll().size());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void replacementChangesStoredValue() {
        RoomRepository repo = new RoomRepository();
        repo.add(value("X1", "first"));
        Room replacement = value("X1", "second");
        repo.replace(replacement);
        assertSame(replacement, repo.require("X1"));
        assertEquals(1, repo.findAll().size());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void missingReplacementThrowsWithoutAdding() {
        RoomRepository repo = new RoomRepository();
        assertThrows(NotFoundException.class, () -> repo.replace(value("X1", "first")));
        assertTrue(repo.findAll().isEmpty());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void removeDeletesStoredValue() {
        RoomRepository repo = new RoomRepository();
        repo.add(value("X1", "first"));
        repo.remove("X1");
        assertTrue(repo.findAll().isEmpty());
        assertThrows(NotFoundException.class, () -> repo.require("X1"));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void missingRemovalThrows() {
        assertThrows(NotFoundException.class,
                () -> new RoomRepository().remove("missing"));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void listIsDetachedFromLaterChanges() {
        RoomRepository repo = new RoomRepository();
        repo.add(value("X1", "first"));
        List<Room> snapshot = repo.findAll();
        repo.add(value("X2", "second"));
        assertEquals(1, snapshot.size());
        assertEquals(2, repo.findAll().size());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void changingReturnedListCannotChangeRepository() {
        RoomRepository repo = new RoomRepository();
        repo.add(value("X1", "first"));
        List<Room> result = repo.findAll();
        try { result.clear(); } catch (UnsupportedOperationException expected) {
            // An unmodifiable snapshot is also acceptable.
        }
        assertEquals(1, repo.findAll().size());
    }
}
