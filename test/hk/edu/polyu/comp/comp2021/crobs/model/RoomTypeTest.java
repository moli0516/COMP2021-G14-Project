package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomTypeTest {

    @Test
    void hasAtLeastOneRoomType() {
        assertTrue(RoomType.values().length > 0);
    }

    @Test
    void eachRoomTypeCanBeLookedUpByName() {
        for (RoomType type : RoomType.values()) {
            assertSame(type, RoomType.valueOf(type.name()));
        }
    }
}
