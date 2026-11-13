package hk.edu.polyu.comp.comp2021.crobs.model;

/**
 * Roles assigned to CROBS users.
 */
public enum UserRole {
    /**
     * Administrator with role-changing privileges.
     */
    SUPERADMIN,
    /**
     * Administrator without role-changing privileges.
     */
    ADMIN,
    /**
     * Regular user.
     */
    REGULAR
}
