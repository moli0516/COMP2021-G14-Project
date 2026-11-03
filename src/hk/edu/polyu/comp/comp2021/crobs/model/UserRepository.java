package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public final class UserRepository {
    private final Map<String, User> entries = new HashMap<>();

    public User require(String userId) {
        // TODO: find by ID or throw NotFoundException.
        return entries.get(userId);
    }

    public void add(User value) {
        // TODO: reject duplicate ID before insertion.
        if(!entries.containsKey(value.getUserId())) entries.put(value.getUserId(), value);
    }

    public void replace(User value) {
        // TODO: replace existing entity only.
        if(entries.containsKey(value.getUserId())) entries.replace(value.getUserId(), value);
    }

    public void remove(String userId) {
        // TODO: remove existing entity.
        entries.remove(userId);
    }

    public List<User> findAll() {
        // TODO: return detached list, not live map view.
        return new ArrayList<User>(entries.values());
    }

}
