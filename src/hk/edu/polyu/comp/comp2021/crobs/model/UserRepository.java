package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Stores user objects by ID in memory; list queries return independent, unsorted lists.
 */
public final class UserRepository {
    private final Map<String, User> entries = new HashMap<>();

    /**
     * Finds an existing user by ID.
     *
     * @param userId the identifier to find
     * @return the stored user
     * @throws NotFoundException if no entry has the supplied ID
     */
    public User require(String userId) {
        User user = entries.get(userId);
        if(user == null){
            throw new NotFoundException("The user is not found");
        }
        return user;
    }

    /**
     * Adds an entry without replacing an existing ID.
     *
     * @param value the non-null user to add
     * @throws ValidationException if the ID already exists
     * @throws NullPointerException if value is null
     */
    public void add(User value) {
        if(!entries.containsKey(value.getUserId())) entries.put(value.getUserId(), value);
        else throw new ValidationException("The user exists already");
    }

    /**
     * Replaces an entry that already exists.
     *
     * @param value the non-null replacement with an existing ID
     * @throws NotFoundException if the ID does not exist
     * @throws NullPointerException if value is null
     */
    public void replace(User value) {
        if(entries.containsKey(value.getUserId())) entries.replace(value.getUserId(), value);
        else throw new NotFoundException("The user is not found");
    }

    /**
     * Removes the entry with the supplied ID.
     *
     * @param userId the identifier to remove
     * @throws NotFoundException if the ID does not exist
     * @throws AuthorizationException if the user can change roles
     */
    public void remove(String userId) {

        if(entries.containsKey(userId)) {
            if (entries.get(userId).canChangeRoles()){
                throw new AuthorizationException("SuperAdmin cannot be removed");
            }
            entries.remove(userId);

        }
        else throw new NotFoundException("The user is not found");
    }

    /**
     * Returns a new list of all stored entries.
     *
     * @return an unsorted list, possibly empty; modifying it does not change this repository
     */
    public List<User> findAll() {
        return new ArrayList<>(entries.values());
    }

}
