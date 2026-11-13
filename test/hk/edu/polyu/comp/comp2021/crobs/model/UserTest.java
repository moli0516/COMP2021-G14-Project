package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("MissingJavadoc")
class UserTest {
    @SuppressWarnings("MissingJavadoc")
    @Test void regularUserHasNoAdministrativePrivileges() {
        User user = new RegularUser("U1", "Alex");
        assertAll(() -> assertEquals("U1", user.getUserId()),
                () -> assertEquals("Alex", user.getName()),
                () -> assertEquals(UserRole.REGULAR, user.getRole()),
                () -> assertFalse(user.isAdmin()),
                () -> assertFalse(user.canChangeRoles()));
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void adminCanManageButCannotChangeRoles() {
        User user = new AdminUser("A1", "Admin");
        assertEquals(UserRole.ADMIN, user.getRole());
        assertTrue(user.isAdmin());
        assertFalse(user.canChangeRoles());
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void superAdminHasBothCapabilities() {
        User user = new SuperAdmin("SA001", "superadmin");
        assertEquals(UserRole.SUPERADMIN, user.getRole());
        assertTrue(user.isAdmin());
        assertTrue(user.canChangeRoles());
        assertInstanceOf(AdminUser.class, user);
    }
    @SuppressWarnings("MissingJavadoc")
    @Test void rolesAreTheThreeAgreedValues() {
        assertEquals(3, UserRole.values().length);
        for (UserRole role : UserRole.values()) {
            assertEquals(role, UserRole.valueOf(role.name()));
        }
    }
}
