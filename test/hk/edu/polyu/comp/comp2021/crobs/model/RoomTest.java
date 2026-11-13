package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ALL")
class RoomTest {

    @Test
    void createsRoomAndReturnsItsDetails() {
        RoomType type = RoomType.values()[0];
        FeatureSet features = FeatureSet.parse("projector, whiteboard");

        Room room = new Room("R001", type, 10, features);

        assertEquals("R001", room.getRoomId());
        assertEquals(type, room.getRoomType());
        assertEquals(10, room.getCapacity());
        assertEquals(features, room.getFeatures());
    }

    @Test
    void rejectsInvalidRoomDetails() {
        RoomType type = RoomType.values()[0];
        FeatureSet features = FeatureSet.parse("projector");

        assertThrows(ValidationException.class,
                () -> new Room(" ", type, 10, features));
        assertThrows(ValidationException.class,
                () -> new Room("R001", null, 10, features));
        assertThrows(ValidationException.class,
                () -> new Room("R001", type, 0, features));
        assertThrows(ValidationException.class,
                () -> new Room("R001", type, 10, null));
    }

    @Test
    void supportsCapacityAndRequiredFeatures() {
        Room room = new Room(
                "R001",
                RoomType.values()[0],
                10,
                FeatureSet.parse("projector, whiteboard"));

        assertTrue(room.supports(8, FeatureSet.parse("projector")));
        assertFalse(room.supports(11, FeatureSet.parse("projector")));
        assertFalse(room.supports(8, FeatureSet.parse("microphone")));
    }

    @Test
    void withDetailsPreservesRoomId() {
        Room room = new Room(
                "R001",
                RoomType.values()[0],
                10,
                FeatureSet.parse("projector"));

        Room updated = room.withDetails(
                RoomType.values()[0],
                20,
                FeatureSet.parse("whiteboard"));

        assertEquals("R001", updated.getRoomId());
        assertEquals(20, updated.getCapacity());
        assertEquals(FeatureSet.parse("whiteboard").asSet(), updated.getFeatures().asSet());
    }
}