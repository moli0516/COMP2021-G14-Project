package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public final class UserRepository {
    private final Map<String, User> entries = new HashMap<>();

    public User require(String userId) {
        // TODO: find by ID or throw NotFoundException.
        User user = entries.get(userId);
        if(user == null){
            throw new NotFoundException("The user is not found");
        }
        return user;
    }

    public void add(User value) {
        // TODO: reject duplicate ID before insertion.
        if(!entries.containsKey(value.getUserId())) entries.put(value.getUserId(), value);
        else throw new ValidationException("The user exists already");
    }

    public void replace(User value) {
        // TODO: replace existing entity only.
        if(entries.containsKey(value.getUserId())) entries.replace(value.getUserId(), value);
        else throw new NotFoundException("The user is not found");
    }

    public void remove(String userId) {

        if(entries.containsKey(userId)) {
            if (entries.get(userId).canChangeRoles()){
                throw new AuthorizationException("SuperAdmin cannot be removed");
            }
            entries.remove(userId);

        }
        else throw new NotFoundException("The user is not found");
    }

    public List<User> findAll() {
        return new ArrayList<User>(entries.values());
    }

}
