package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

/**
 * Base user identity and default privilege policy.
 */
public abstract class User {
    private final String userId;
    private final String name;

    /**
     * Creates a user identity.
     *
     * @param userId the identifier stored without additional validation
     * @param name the nonblank display name
     * @throws ValidationException if the name is null or blank
     */
    protected User(String userId, String name){
        if(name == null || name.trim().isEmpty()){
            throw new ValidationException("Name cannot be null");
        }
        this.userId = userId;
        this.name = name;
    }

    /**
     * Returns the user identifier.
     *
     * @return the stored user ID
     */
    public String getUserId(){ return userId; }
    /**
     * Returns the display name.
     *
     * @return the user name
     */
    public String getName(){ return name; }

    /**
     * Returns this user's role.
     *
     * @return the user role
     */
    public abstract UserRole getRole();
    /**
     * Reports whether this user has administrative privileges.
     *
     * @return {@code false} unless overridden
     */
    public boolean isAdmin(){ return false; }
    /**
     * Reports whether this user may change roles.
     *
     * @return {@code false} unless overridden
     */
    public boolean canChangeRoles(){ return false; }
}
