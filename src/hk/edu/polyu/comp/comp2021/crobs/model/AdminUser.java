package hk.edu.polyu.comp.comp2021.crobs.model;

/**
 * A user with administrative privileges but no role-changing privilege.
 */
public class AdminUser extends User{
    /**
     * Creates a user with the privileges defined by this class.
     *
     * @param userId the user identifier
     * @param name the nonblank display name
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException if the name is null or blank
     */
    public AdminUser(String userId, String name){
        super(userId, name);
    }

    /**
     * Returns the role represented by this user.
     *
     * @return the corresponding user role
     */
    @Override
    public UserRole getRole(){ return UserRole.ADMIN; }

    /**
     * Reports whether this user has administrative privileges.
     *
     * @return {@code true}
     */
    @Override
    public boolean isAdmin(){ return true; }
}