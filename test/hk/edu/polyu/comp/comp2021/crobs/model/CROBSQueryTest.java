package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CROBSQueryTest {

    @Test
    void searchRoomsFiltersByCapacityFeaturesAndAvailability() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");

        RoomType type = RoomType.values()[0];
        Room bookedRoom = crobs.addRoom(
                type, 10, Arrays.asList("projector", "whiteboard")
                        .stream().collect(java.util.stream.Collectors.toSet()));
        Room availableRoom = crobs.addRoom(
                type, 20, Collections.singleton("projector"));

        LocalDate date = LocalDate.now().plusDays(1);
        crobs.createBooking(
                bookedRoom.getRoomId(),
                date,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                "Meeting");

        List<Room> results = crobs.searchRooms(
                type,
                10,
                Collections.singleton("projector"),
                date,
                LocalTime.of(10, 30),
                LocalTime.of(11, 30));

        assertEquals(Collections.singletonList(availableRoom), results);
    }

    @Test
    void weeklyScheduleContainsBookingsWithinRequestedWeek() {
        CROBS crobs = new CROBS();
        crobs.login("SA001");

        Room room = crobs.addRoom(
                RoomType.values()[0],
                10,
                Collections.<String>emptySet());

        LocalDate weekStart = LocalDate.now().plusDays(1);
        Booking inWeek = crobs.createBooking(
                room.getRoomId(),
                weekStart.plusDays(2),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "In week");

        crobs.createBooking(
                room.getRoomId(),
                weekStart.plusDays(7),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "Outside week");

        List<Booking> schedule = crobs.getWeeklySchedule(weekStart);

        assertEquals(1, schedule.size());
        assertEquals(inWeek.getBookingId(), schedule.get(0).getBookingId());
    }
}
