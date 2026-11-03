package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

class CROBSUserTest {
    @Test void startupHasNoLoggedInUser() {
        assertNull(new CROBS().getCurrentUser());
    }
    @Test void bootstrapSuperAdminCanLogIn() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        assertEquals(UserRole.SUPERADMIN, crobs.getCurrentUser().getRole());
    }
    @Test void unknownLoginReportsNotFoundAndKeepsAnonymousSession() {
        CROBS crobs = new CROBS();
        assertThrows(NotFoundException.class, () -> crobs.login("UNKNOWN"));
        assertNull(crobs.getCurrentUser());
    }
    @Test void nullAndBlankLoginAreValidationErrors() {
        CROBS crobs = new CROBS();
        assertThrows(ValidationException.class, () -> crobs.login(null));
        assertThrows(ValidationException.class, () -> crobs.login(" "));
        assertNull(crobs.getCurrentUser());
    }
    @Test void cannotSwitchAccountsWithoutLogout() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        User other = crobs.addUser("Alex");
        assertThrows(AuthorizationException.class, () -> crobs.login(other.getUserId()));
        assertEquals("SA001", crobs.getCurrentUser().getUserId());
    }
    @Test void logoutClearsSession() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        crobs.logout();
        assertNull(crobs.getCurrentUser());
    }
    @Test void anonymousLogoutIsRejectedUnderTeamPolicy() {
        assertThrows(AuthorizationException.class, () -> new CROBS().logout());
    }
    @Test void anonymousUserCannotManageUsers() {
        CROBS crobs = new CROBS();
        assertThrows(AuthorizationException.class, () -> crobs.addUser("Alex"));
        assertThrows(AuthorizationException.class, () -> crobs.removeUser("U1"));
    }
    @Test void superAdminCreatesDistinctRegularUsersWhoCanLogIn() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        User first = crobs.addUser("Alex");
        User second = crobs.addUser("Sam");
        assertNotEquals(first.getUserId(), second.getUserId());
        assertEquals(UserRole.REGULAR, first.getRole());
        assertEquals("Alex", first.getName());
        crobs.logout();
        crobs.login(first.getUserId());
        assertEquals(first.getUserId(), crobs.getCurrentUser().getUserId());
    }
    @Test void regularUserCannotManageUsers() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        User first = crobs.addUser("Alex");
        User second = crobs.addUser("Sam");
        crobs.logout(); crobs.login(first.getUserId());
        assertThrows(AuthorizationException.class, () -> crobs.addUser("Other"));
        assertThrows(AuthorizationException.class, () -> crobs.removeUser(second.getUserId()));
    }
    @Test void blankNamesAreRejected() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        assertThrows(ValidationException.class, () -> crobs.addUser(" "));
        assertThrows(ValidationException.class, () -> crobs.addUser(null));
    }
    @Test void removedRegularUserCannotLogIn() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        User removed = crobs.addUser("Alex");
        crobs.removeUser(removed.getUserId());
        crobs.logout();
        assertThrows(NotFoundException.class, () -> crobs.login(removed.getUserId()));
    }
    @Test void superAdminCannotBeRemoved() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        assertThrows(AuthorizationException.class, () -> crobs.removeUser("SA001"));
        assertEquals(UserRole.SUPERADMIN, crobs.getCurrentUser().getRole());
    }
    @Test void unknownRemovalReportsNotFound() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        assertThrows(NotFoundException.class, () -> crobs.removeUser("UNKNOWN"));
    }
}
