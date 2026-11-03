package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public final class RoomRepository {
    private final Map<String, Room> entries = new HashMap<>();

    public Room require(String roomId) {
        return entries.get(roomId);
    }

    public void add(Room value) {
        if (!entries.containsKey(value.getRoomId())) entries.put(value.getRoomId(), value);
    }

    public void replace(Room value) {
        if(entries.containsKey(value.getRoomId())) entries.replace(value.getRoomId(), value);
    }

    public void remove(String roomId) {
        entries.remove(roomId);
    }

    public List<Room> findAll() {
        return new ArrayList<>(entries.values());
    }

}
