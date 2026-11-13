package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

/**
 * An immutable room description with a positive capacity and normalized features.
 */
public final class Room {
    private final String roomId;
    private final RoomType roomType;
    private final int capacity;
    private final FeatureSet features;

    /**
     * Creates and validates a room.
     *
     * @param roomId the nonblank identifier
     * @param roomType the room category
     * @param capacity the positive room capacity
     * @param features the non-null feature set
     * @throws ValidationException if any room detail is invalid
     */
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

    /**
     * Returns the room's roomId.
     *
     * @return the stored roomId
     */
    public String getRoomId() { return roomId; }
    /**
     * Returns the room's roomType.
     *
     * @return the stored roomType
     */
    public RoomType getRoomType() { return roomType; }
    /**
     * Returns the room's capacity.
     *
     * @return the stored capacity
     */
    public int getCapacity() { return capacity; }
    /**
     * Returns the room's features.
     *
     * @return the stored features
     */
    public FeatureSet getFeatures() { return features; }

    /**
     * Checks capacity and feature requirements without checking booking availability.
     *
     * @param minCapacity the minimum required capacity
     * @param requiredFeatures the required features, or {@code null} for no feature restriction
     * @return whether both requirements are met
     */
    public boolean supports(int minCapacity, FeatureSet requiredFeatures) {
         if (capacity < minCapacity) {
            return false;
        }
        return requiredFeatures == null || features.containsAll(requiredFeatures);
    }


    /**
     * Creates a validated copy with the same room ID.
     *
     * @param type the new room category
     * @param newCapacity the new positive capacity
     * @param newFeatures the new feature set
     * @return the updated room
     * @throws ValidationException if the replacement details are invalid
     */
    public Room withDetails(RoomType type, int newCapacity, FeatureSet newFeatures) {
        return new Room(roomId, type, newCapacity, newFeatures);
    }
}
