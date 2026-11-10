package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CROBSRoomTest {

    @Test
    void adminCanAddGetModifyAndDeleteRoom() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");

        RoomType type = RoomType.values()[0];
        Room added = crobs.addRoom(
                type, 10, Collections.singleton("projector"));

        assertSame(added, crobs.getRoom(added.getRoomId()));

        Room modified = crobs.modifyRoom(
                added.getRoomId(),
                type,
                20,
                Collections.singleton("whiteboard"));

        assertEquals(added.getRoomId(), modified.getRoomId());
        assertEquals(20, modified.getCapacity());
        assertEquals(
                Collections.singleton("whiteboard"),
                modified.getFeatures().asSet());

        crobs.deleteRoom(added.getRoomId());

        assertThrows(
                NotFoundException.class,
                () -> crobs.getRoom(added.getRoomId()));
    }

    @Test
    void getAllRoomsReturnsAddedRooms() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");

        RoomType type = RoomType.values()[0];
        Room first = crobs.addRoom(type, 5, Collections.<String>emptySet());
        Room second = crobs.addRoom(type, 10, Collections.<String>emptySet());

        assertTrue(crobs.getAllRooms().contains(first));
        assertTrue(crobs.getAllRooms().contains(second));
    }

    @Test
    void regularUserCannotAddRoom() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");
        User regularUser = crobs.addUser("Test User");

        crobs.logout();
        crobs.login(regularUser.getUserId());

        assertThrows(
                AuthorizationException.class,
                () -> crobs.addRoom(
                        RoomType.values()[0],
                        10,
                        Collections.<String>emptySet()));
    }
}
