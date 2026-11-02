package hk.edu.polyu.comp.comp2021.crobs.view;

import hk.edu.polyu.comp.comp2021.crobs.model.Room;
import hk.edu.polyu.comp.comp2021.crobs.model.Booking;
import hk.edu.polyu.comp.comp2021.crobs.model.User;
import java.util.List;

public final class OutputFormatter {
    public String formatRoom(Room room) {
        throw new UnsupportedOperationException("TODO: format room details");
    }

    public String formatRooms(List<Room> rooms) {
        throw new UnsupportedOperationException("TODO: format room list");
    }

    public String formatBookings(List<Booking> bookings) {
        // Only pass bookings the current user is authorized to inspect.
        throw new UnsupportedOperationException("TODO: format own bookings");
    }

    public String formatSchedule(List<Booking> bookings, User viewer) {
        // TODO: hide other owners' IDs for regular viewers, including error text.
        // These raw records must remain inside trusted code, not a public GUI API.
        throw new UnsupportedOperationException("TODO: format privacy-safe schedule");
    }
}
