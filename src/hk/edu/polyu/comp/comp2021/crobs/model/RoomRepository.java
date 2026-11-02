package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public final class RoomRepository {
    private final Map<String, Room> entries = new HashMap<>();

    public Room require(String roomId) {
        // TODO: find by ID or throw NotFoundException.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public void add(Room value) {
        // TODO: reject duplicate ID before insertion.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public void replace(Room value) {
        // TODO: replace existing entity only.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public void remove(String roomId) {
        // TODO: remove existing entity.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public List<Room> findAll() {
        // TODO: return detached list, not live map view.
        throw new UnsupportedOperationException("TODO: implement");
    }

}
