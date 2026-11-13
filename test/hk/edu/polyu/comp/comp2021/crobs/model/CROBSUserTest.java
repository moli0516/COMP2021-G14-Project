package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

@SuppressWarnings("MissingJavadoc")
class CROBSUserTest {
    @SuppressWarnings("MissingJavadoc")
    @Test void startupHasNoLoggedInUser() {
        assertNull(new CROBS().getCurrentUser());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void bootstrapSuperAdminCanLogIn() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        assertEquals(UserRole.SUPERADMIN, crobs.getCurrentUser().getRole());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void unknownLoginReportsNotFoundAndKeepsAnonymousSession() {
        CROBS crobs = new CROBS();
        assertThrows(NotFoundException.class, () -> crobs.login("UNKNOWN"));
        assertNull(crobs.getCurrentUser());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void nullAndBlankLoginAreValidationErrors() {
        CROBS crobs = new CROBS();
        assertThrows(ValidationException.class, () -> crobs.login(null));
        assertThrows(ValidationException.class, () -> crobs.login(" "));
        assertNull(crobs.getCurrentUser());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void cannotSwitchAccountsWithoutLogout() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        User other = crobs.addUser("Alex");
        assertThrows(AuthorizationException.class, () -> crobs.login(other.getUserId()));
        assertEquals("SA001", crobs.getCurrentUser().getUserId());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void logoutClearsSession() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        crobs.logout();
        assertNull(crobs.getCurrentUser());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void anonymousLogoutIsRejectedUnderTeamPolicy() {
        assertThrows(AuthorizationException.class, () -> new CROBS().logout());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void anonymousUserCannotManageUsers() {
        CROBS crobs = new CROBS();
        assertThrows(AuthorizationException.class, () -> crobs.addUser("Alex"));
        assertThrows(AuthorizationException.class, () -> crobs.removeUser("U1"));
    }
    @SuppressWarnings("MissingJavadoc")
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
    @SuppressWarnings("MissingJavadoc")
    @Test void regularUserCannotManageUsers() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        User first = crobs.addUser("Alex");
        User second = crobs.addUser("Sam");
        crobs.logout(); crobs.login(first.getUserId());
        assertThrows(AuthorizationException.class, () -> crobs.addUser("Other"));
        assertThrows(AuthorizationException.class, () -> crobs.removeUser(second.getUserId()));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void blankNamesAreRejected() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        assertThrows(ValidationException.class, () -> crobs.addUser(" "));
        assertThrows(ValidationException.class, () -> crobs.addUser(null));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void removedRegularUserCannotLogIn() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        User removed = crobs.addUser("Alex");
        crobs.removeUser(removed.getUserId());
        crobs.logout();
        assertThrows(NotFoundException.class, () -> crobs.login(removed.getUserId()));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void superAdminCannotBeRemoved() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        assertThrows(AuthorizationException.class, () -> crobs.removeUser("SA001"));
        assertEquals(UserRole.SUPERADMIN, crobs.getCurrentUser().getRole());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void unknownRemovalReportsNotFound() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        assertThrows(NotFoundException.class, () -> crobs.removeUser("UNKNOWN"));
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void changeUserRole() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        User user = crobs.addUser("Alex");

        // Promote regular user to admin.
        crobs.changeRole(user.getUserId(), UserRole.ADMIN);

        crobs.logout();
        crobs.login(user.getUserId());

        assertEquals(UserRole.ADMIN, crobs.getCurrentUser().getRole());
        assertEquals(user.getUserId(), crobs.getCurrentUser().getUserId());
        assertEquals("Alex", crobs.getCurrentUser().getName());

        // Demote admin to regular user.
        crobs.logout();
        crobs.login("SA001");

        crobs.changeRole(user.getUserId(), UserRole.REGULAR);

        crobs.logout();
        crobs.login(user.getUserId());

        assertEquals(UserRole.REGULAR, crobs.getCurrentUser().getRole());
        assertEquals(user.getUserId(), crobs.getCurrentUser().getUserId());
        assertEquals("Alex", crobs.getCurrentUser().getName());
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void nonSuperAdminCannotChangeUserRole() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");

        User admin = crobs.addUser("Alex");
        User regular = crobs.addUser("Alan");

        crobs.changeRole(admin.getUserId(), UserRole.ADMIN);

        // Ordinary admin cannot promote a regular user.
        crobs.logout();
        crobs.login(admin.getUserId());

        assertThrows(AuthorizationException.class,
                () -> crobs.changeRole(
                        regular.getUserId(), UserRole.ADMIN));

        // Verify the rejected promotion did not change the role.
        crobs.logout();
        crobs.login(regular.getUserId());

        assertEquals(UserRole.REGULAR, crobs.getCurrentUser().getRole());

        // Regular user cannot demote an admin.
        assertThrows(AuthorizationException.class,
                () -> crobs.changeRole(
                        admin.getUserId(), UserRole.REGULAR));

        // Verify the rejected demotion did not change the role.
        crobs.logout();
        crobs.login(admin.getUserId());

        assertEquals(UserRole.ADMIN, crobs.getCurrentUser().getRole());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test
    void anonymousUserCannotChangeUserRole() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        User user = crobs.addUser("Alex");
        crobs.logout();

        assertThrows(AuthorizationException.class,
                () -> crobs.changeRole(
                        user.getUserId(), UserRole.ADMIN));

        crobs.login(user.getUserId());
        assertEquals(UserRole.REGULAR, crobs.getCurrentUser().getRole());
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void changingToSameRoleIsRejectedWithoutChangingUser() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        User user = crobs.addUser("Alex");

        // Already regular.
        assertThrows(ValidationException.class,
                () -> crobs.changeRole(
                        user.getUserId(), UserRole.REGULAR));

        crobs.logout();
        crobs.login(user.getUserId());

        assertEquals(UserRole.REGULAR, crobs.getCurrentUser().getRole());
        assertEquals("Alex", crobs.getCurrentUser().getName());

        // Promote, then request admin again.
        crobs.logout();
        crobs.login("SA001");

        crobs.changeRole(user.getUserId(), UserRole.ADMIN);

        assertThrows(ValidationException.class,
                () -> crobs.changeRole(
                        user.getUserId(), UserRole.ADMIN));

        crobs.logout();
        crobs.login(user.getUserId());

        assertEquals(UserRole.ADMIN, crobs.getCurrentUser().getRole());
        assertEquals("Alex", crobs.getCurrentUser().getName());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test
    void adminUserCannotBeRemoved() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");

        User target = crobs.addUser("Alex");
        crobs.changeRole(target.getUserId(), UserRole.ADMIN);

        assertThrows(AuthorizationException.class,
                () -> crobs.removeUser(target.getUserId()));

        crobs.logout();
        crobs.login(target.getUserId());

        assertEquals(UserRole.ADMIN, crobs.getCurrentUser().getRole());
        assertEquals("Alex", crobs.getCurrentUser().getName());
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void nullAndBlankRemovalIdsAreRejected() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");

        assertThrows(ValidationException.class,
                () -> crobs.removeUser(null));

        assertThrows(ValidationException.class,
                () -> crobs.removeUser(" "));

        assertEquals("SA001", crobs.getCurrentUser().getUserId());
    }
}
