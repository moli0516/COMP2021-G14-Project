package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Stores room objects by ID in memory; list queries return independent, unsorted lists.
 */
public final class RoomRepository {
    private final Map<String, Room> entries = new HashMap<>();

    /**
     * Finds an existing room by ID.
     *
     * @param roomId the identifier to find
     * @return the stored room
     * @throws NotFoundException if no entry has the supplied ID
     */
    public Room require(String roomId) {
        Room room = entries.get(roomId);
        if(room == null){
            throw new NotFoundException("The room is not found");
        }
        return room;
    }

    /**
     * Adds an entry without replacing an existing ID.
     *
     * @param value the non-null room to add
     * @throws ValidationException if the ID already exists
     * @throws NullPointerException if value is null
     */
    public void add(Room value) {
        if (!entries.containsKey(value.getRoomId())) entries.put(value.getRoomId(), value);
        else throw new ValidationException("The room exists already");
    }

    /**
     * Replaces an entry that already exists.
     *
     * @param value the non-null replacement with an existing ID
     * @throws NotFoundException if the ID does not exist
     * @throws NullPointerException if value is null
     */
    public void replace(Room value) {
        if(entries.containsKey(value.getRoomId())) entries.replace(value.getRoomId(), value);
        else throw new NotFoundException("The room is not found");
    }

    /**
     * Removes the entry with the supplied ID.
     *
     * @param roomId the identifier to remove
     * @throws NotFoundException if the ID does not exist
     */
    public void remove(String roomId) {
        if(entries.containsKey(roomId)) entries.remove(roomId);
        else throw new NotFoundException("The room is not found");
    }

    /**
     * Returns a new list of all stored entries.
     *
     * @return an unsorted list, possibly empty; modifying it does not change this repository
     */
    public List<Room> findAll() {
        return new ArrayList<>(entries.values());
    }

}
