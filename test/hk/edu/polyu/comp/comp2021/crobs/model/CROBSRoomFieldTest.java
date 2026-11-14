package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CROBSRoomFieldTest {

    private CROBS crobs;
    private Room original;

    @BeforeEach
    void setUp() {
        crobs = new CROBS();
        crobs.login("SA001");

        original = crobs.addRoom(
                RoomType.MEETING_ROOM,
                10,
                Collections.singleton("projector"));
    }

    @Test
    void changingRoomTypePreservesOtherFieldsAndPersistsTheChange() {
        Room updated = crobs.modifyRoom(
                original.getRoomId(),
                "roomType",
                "computerLab");

        assertAll(
                () -> assertEquals(
                        original.getRoomId(), updated.getRoomId()),
                () -> assertSame(
                        RoomType.COMPUTER_LAB, updated.getRoomType()),
                () -> assertEquals(10, updated.getCapacity()),
                () -> assertEquals(
                        Collections.singleton("projector"),
                        updated.getFeatures().asSet()),
                () -> assertSame(
                        updated, crobs.getRoom(original.getRoomId()))
        );
    }

    @Test
    void changingCapacityPreservesOtherFieldsAndPersistsTheChange() {
        Room updated = crobs.modifyRoom(
                original.getRoomId(),
                "capacity",
                "20");

        assertAll(
                () -> assertEquals(
                        original.getRoomId(), updated.getRoomId()),
                () -> assertSame(
                        RoomType.MEETING_ROOM, updated.getRoomType()),
                () -> assertEquals(20, updated.getCapacity()),
                () -> assertEquals(
                        Collections.singleton("projector"),
                        updated.getFeatures().asSet()),
                () -> assertSame(
                        updated, crobs.getRoom(original.getRoomId()))
        );
    }

    @Test
    void changingFeaturesReplacesTheFeatureSetAndPreservesOtherFields() {
        Room updated = crobs.modifyRoom(
                original.getRoomId(),
                "features",
                "whiteboard,computer");

        Set<String> expectedFeatures = new HashSet<>(
                Arrays.asList("whiteboard", "computer"));

        assertAll(
                () -> assertEquals(
                        original.getRoomId(), updated.getRoomId()),
                () -> assertSame(
                        RoomType.MEETING_ROOM, updated.getRoomType()),
                () -> assertEquals(10, updated.getCapacity()),
                () -> assertEquals(
                        expectedFeatures, updated.getFeatures().asSet()),
                () -> assertSame(
                        updated, crobs.getRoom(original.getRoomId()))
        );
    }

    @Test
    void roomFieldAndValueAreTrimmed() {
        Room updated = crobs.modifyRoom(
                original.getRoomId(),
                " capacity ",
                " 12 ");

        assertEquals(12, updated.getCapacity());
        assertEquals(
                12,
                crobs.getRoom(original.getRoomId()).getCapacity());
    }

    @Test
    void nullOrBlankFieldIsRejectedWithoutChangingTheRoom() {
        String[] invalidFields = {null, "", " "};

        for (String field : invalidFields) {
            assertRejectedWithoutChangingRoom(field, "20");
        }
    }

    @Test
    void nullOrBlankValueIsRejectedWithoutChangingTheRoom() {
        String[] invalidValues = {null, "", " "};

        for (String value : invalidValues) {
            assertRejectedWithoutChangingRoom("capacity", value);
        }
    }

    @Test
    void unknownFieldIsRejectedWithoutChangingTheRoom() {
        assertRejectedWithoutChangingRoom("location", "MainBuilding");
    }

    @Test
    void invalidCapacityIsRejectedWithoutChangingTheRoom() {
        String[] invalidValues = {
                "abc",
                "1.5",
                "2147483648",
                "0",
                "-1"
        };

        for (String value : invalidValues) {
            assertRejectedWithoutChangingRoom("capacity", value);
        }
    }

    @Test
    void invalidRoomTypeIsRejectedWithoutChangingTheRoom() {
        assertRejectedWithoutChangingRoom("roomType", "office");
    }

    @Test
    void invalidFeaturesAreRejectedWithoutChangingTheRoom() {
        assertRejectedWithoutChangingRoom("features", "123");
    }

    @Test
    void unknownRoomIsRejected() {
        assertThrows(
                NotFoundException.class,
                () -> crobs.modifyRoom(
                        "R_DOES_NOT_EXIST",
                        "capacity",
                        "20"));

        assertSame(original, crobs.getRoom(original.getRoomId()));
    }

    @Test
    void anonymousUserCannotChangeRoomFields() {
        crobs.logout();

        assertThrows(
                AuthorizationException.class,
                () -> crobs.modifyRoom(
                        original.getRoomId(),
                        "capacity",
                        "20"));

        crobs.login("SA001");
        assertSame(original, crobs.getRoom(original.getRoomId()));
    }

    @Test
    void regularUserCannotChangeRoomFields() {
        User regular = crobs.addUser("Regular User");

        crobs.logout();
        crobs.login(regular.getUserId());

        assertThrows(
                AuthorizationException.class,
                () -> crobs.modifyRoom(
                        original.getRoomId(),
                        "capacity",
                        "20"));

        crobs.logout();
        crobs.login("SA001");

        assertSame(original, crobs.getRoom(original.getRoomId()));
    }

    private void assertRejectedWithoutChangingRoom(
            String field, String value) {
        assertThrows(
                ValidationException.class,
                () -> crobs.modifyRoom(
                        original.getRoomId(),
                        field,
                        value),
                "Should reject field=" + field + ", value=" + value);

        Room stored = crobs.getRoom(original.getRoomId());

        assertAll(
                () -> assertSame(original, stored),
                () -> assertSame(
                        RoomType.MEETING_ROOM, stored.getRoomType()),
                () -> assertEquals(10, stored.getCapacity()),
                () -> assertEquals(
                        Collections.singleton("projector"),
                        stored.getFeatures().asSet())
        );
    }
}