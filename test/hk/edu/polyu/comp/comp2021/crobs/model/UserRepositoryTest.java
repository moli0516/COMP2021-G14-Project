package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

@SuppressWarnings("MissingJavadoc")
class UserRepositoryTest {
    private User value(String id, String name) { return new RegularUser(id, name); }
    @SuppressWarnings("MissingJavadoc")
    @Test void addAndRequireReturnStoredValue() {
        UserRepository repo = new UserRepository();
        User value = value("X1", "first");
        repo.add(value);
        assertSame(value, repo.require("X1"));
        assertEquals(1, repo.findAll().size());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void missingLookupThrowsNotFound() {
        assertThrows(NotFoundException.class,
                () -> new UserRepository().require("missing"));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void duplicateAddIsRejectedWithoutReplacingOriginal() {
        UserRepository repo = new UserRepository();
        User original = value("X1", "first");
        repo.add(original);
        assertThrows(ValidationException.class, () -> repo.add(value("X1", "second")));
        assertSame(original, repo.require("X1"));
        assertEquals(1, repo.findAll().size());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void replacementChangesStoredValue() {
        UserRepository repo = new UserRepository();
        repo.add(value("X1", "first"));
        User replacement = value("X1", "second");
        repo.replace(replacement);
        assertSame(replacement, repo.require("X1"));
        assertEquals(1, repo.findAll().size());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void missingReplacementThrowsWithoutAdding() {
        UserRepository repo = new UserRepository();
        assertThrows(NotFoundException.class, () -> repo.replace(value("X1", "first")));
        assertTrue(repo.findAll().isEmpty());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void removeDeletesStoredValue() {
        UserRepository repo = new UserRepository();
        repo.add(value("X1", "first"));
        repo.remove("X1");
        assertTrue(repo.findAll().isEmpty());
        assertThrows(NotFoundException.class, () -> repo.require("X1"));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void missingRemovalThrows() {
        assertThrows(NotFoundException.class,
                () -> new UserRepository().remove("missing"));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void listIsDetachedFromLaterChanges() {
        UserRepository repo = new UserRepository();
        repo.add(value("X1", "first"));
        List<User> snapshot = repo.findAll();
        repo.add(value("X2", "second"));
        assertEquals(1, snapshot.size());
        assertEquals(2, repo.findAll().size());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void changingReturnedListCannotChangeRepository() {
        UserRepository repo = new UserRepository();
        repo.add(value("X1", "first"));
        List<User> result = repo.findAll();
        try { result.clear(); } catch (UnsupportedOperationException expected) {
            // An unmodifiable snapshot is also acceptable.
        }
        assertEquals(1, repo.findAll().size());
    }
}
