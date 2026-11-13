package hk.edu.polyu.comp.comp2021.crobs.model;

/**
 * An administrator permitted to change other users' roles.
 */
public class SuperAdmin extends AdminUser{
    /**
     * Creates a user with the privileges defined by this class.
     *
     * @param userId the user identifier
     * @param name the nonblank display name
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException if the name is null or blank
     */
    public SuperAdmin(String userId, String name){
        super(userId, name);
    }

    /**
     * Returns the role represented by this user.
     *
     * @return the corresponding user role
     */
    @Override
    public UserRole getRole(){ return UserRole.SUPERADMIN; }

    /**
     * Reports whether this user may change roles.
     *
     * @return {@code true}
     */
    @Override
    public boolean canChangeRoles(){ return true; }
}