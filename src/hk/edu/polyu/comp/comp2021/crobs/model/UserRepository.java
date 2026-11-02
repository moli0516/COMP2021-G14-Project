package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public final class UserRepository {
    private final Map<String, User> entries = new HashMap<>();

    public User require(String userId) {
        // TODO: find by ID or throw NotFoundException.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public void add(User value) {
        // TODO: reject duplicate ID before insertion.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public void replace(User value) {
        // TODO: replace existing entity only.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public void remove(String userId) {
        // TODO: remove existing entity.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public List<User> findAll() {
        // TODO: return detached list, not live map view.
        throw new UnsupportedOperationException("TODO: implement");
    }

}
