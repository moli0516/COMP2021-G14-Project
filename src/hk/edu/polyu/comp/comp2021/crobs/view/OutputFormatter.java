package hk.edu.polyu.comp.comp2021.crobs.view;

import hk.edu.polyu.comp.comp2021.crobs.model.Room;
import hk.edu.polyu.comp.comp2021.crobs.model.Booking;
import hk.edu.polyu.comp.comp2021.crobs.model.User;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Objects;
import java.util.Comparator;

public final class OutputFormatter {
    private static final Comparator<Booking> BY_DATE_TIME_ID=Comparator.comparing(Booking::getDate)
            .thenComparing(Booking::getStartTime).thenComparing(Booking::getBookingId);
    public String formatUser(User user){
        return user.getUserId()+"("+user.getName()+", "+user.getRole()+")";
    }
    public String formatRoom(Room room) {
        String features = new TreeSet<String>(room.getFeatures().asSet()).toString();
        return room.getRoomId() + " | " + room.getRoomType().toCommand()
            + " capacity" + room.getCapacity() + "| features" + features;
    }

    public String formatRooms(List<Room> rooms) {
        if(rooms.isEmpty()){
            return "No rooms were found.";
        }
        List<Room> sorted=new ArrayList<>(rooms);
        sorted.sort(Comparator.comparing(Room::getRoomId));
        StringBuilder s= new StringBuilder();
        for(Room room: sorted){
            appendLine(s, formatRoom(room));
        }
        return s.toString();
    }
    public String formatBooking(Booking b){
        String res=b.getBookingId()+" room "+b.getRoomId()+" "+b.getDate()+" "+b.getStartTime()+"-"
                +b.getEndTime()+" "+b.getPurpose()+" "+(b.isCancelled()?"CANCELLED":"ACTIVE");
        return res;
    }

    public String formatBookings(List<Booking> bookings) {
        if (bookings.isEmpty()){
            return "No bookings found.";
        }
        List<Booking> sorted=new ArrayList<>(bookings);
        sorted.sort(BY_DATE_TIME_ID);
        StringBuilder s=new StringBuilder();
        for (Booking b: sorted){
            appendLine(s, formatBooking(b));
        }
        return s.toString();
    }

    public String formatSchedule(List<Booking> bookings, User viewer) {
        List<Booking> current = new ArrayList<>();
        for (Booking b : bookings) {
            if (!b.isCancelled()) {
                current.add(b);
            }
        }
        if (current.isEmpty()) {
            return "No bookings this week.";
        }
        current.sort(BY_DATE_TIME_ID);
        StringBuilder s = new StringBuilder();
        for (Booking b : current) {
            boolean canSeeOwner = viewer.isAdmin() || Objects.equals(viewer.getUserId(), b.getOwnerUserId());
            appendLine(s, b.getBookingId() + " | " + b.getDate() + " "
                    + b.getStartTime() + "-" + b.getEndTime() + " | " + b.getPurpose()
                    + " | booked by" + (canSeeOwner ? b.getOwnerUserId() : "hidden"));
            return s.toString();
        }
    }
        private static void appendLine(StringBuilder s, String line){
            if (s.length()>0){
                s.append(System.lineSeparator());
            }
            s.append(line);
        }
}