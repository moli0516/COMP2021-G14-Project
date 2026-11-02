package hk.edu.polyu.comp.comp2021.crobs.model;

public final class Room {
    private final String roomId;
    private final RoomType roomType;
    private final int capacity;
    private final FeatureSet features;

    public Room(String roomId, RoomType roomType, int capacity, FeatureSet features) {
        // TODO: enforce nonblank ID, nonnull values, positive capacity.
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
        throw new UnsupportedOperationException("TODO: capacity and feature match");
    }

    public Room withDetails(RoomType type, int newCapacity, FeatureSet newFeatures) {
        throw new UnsupportedOperationException("TODO: validated replacement with same ID");
    }
}
