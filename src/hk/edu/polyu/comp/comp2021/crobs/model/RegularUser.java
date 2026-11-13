package hk.edu.polyu.comp.comp2021.crobs.model;

/**
 * A regular user with the default non-administrative privileges.
 */
public class RegularUser extends User{
    /**
     * Creates a user with the privileges defined by this class.
     *
     * @param userId the user identifier
     * @param name the nonblank display name
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException if the name is null or blank
     */
    public RegularUser(String userId, String name){
        super(userId, name);
    }

    /**
     * Returns the role represented by this user.
     *
     * @return the corresponding user role
     */
    @Override
    public UserRole getRole(){ return UserRole.REGULAR; }
}
