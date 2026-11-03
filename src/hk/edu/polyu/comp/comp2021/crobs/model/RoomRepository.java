package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public final class RoomRepository {
    private final Map<String, Room> entries = new HashMap<>();

    public Room require(String roomId) {
        Room room = entries.get(roomId);
        if(room == null){
            throw new NotFoundException("The room is not found");
        }
        return room;
    }

    public void add(Room value) {
        if (!entries.containsKey(value.getRoomId())) entries.put(value.getRoomId(), value);
        else throw new ValidationException("The room exists already");
    }

    public void replace(Room value) {
        if(entries.containsKey(value.getRoomId())) entries.replace(value.getRoomId(), value);
        else throw new NotFoundException("The room is not found");
    }

    public void remove(String roomId) {
        if(entries.containsKey(roomId)) entries.remove(roomId);
        else throw new NotFoundException("The room is not found");
    }

    public List<Room> findAll() {
        return new ArrayList<>(entries.values());
    }

}
