package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

public final class Room {
    private final String roomId;
    private final RoomType roomType;
    private final int capacity;
    private final FeatureSet features;

    public Room(String roomId, RoomType roomType, int capacity, FeatureSet features) {
        if (roomId == null || roomId.trim().isEmpty()) {
            throw new ValidationException("Room ID must not be blank");
        }
        if (roomType == null) {
            throw new ValidationException("Room type must not be null");
        }
        if (capacity <= 0) {
            throw new ValidationException("Capacity must be positive");
        }
        if (features == null) {
            throw new ValidationException("Features must not be null");
        }
        this.roomId = roomId;
        this.roomType = roomType;
        this.capacity = capacity;
        this.features = features;
    }

    public String getRoomId() { return roomId; }
    public RoomType getRoomType() { return roomType; }
    public int getCapacity() { return capacity; }
    public FeatureSet getFeatures() { return features; }

    public boolean supports(int minCapacity, FeatureSet requiredFeatures) {
         if (capacity < minCapacity) {
            return false;
        }
        return requiredFeatures == null || features.containsAll(requiredFeatures);
    }


    public Room withDetails(RoomType type, int newCapacity, FeatureSet newFeatures) {
        return new Room(roomId, type, newCapacity, newFeatures);
    }
}
