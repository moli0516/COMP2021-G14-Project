package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.*;

public final class BookingRepository {
    private final Map<String, Booking> entries = new HashMap<>();

    public Booking require(String bookingId) {
        // TODO: find by ID or throw NotFoundException.
        return entries.get(bookingId);
    }

    public void add(Booking value) {
        // TODO: reject duplicate ID before insertion.
        if(!entries.containsKey(value.getBookingId())) entries.put(value.getBookingId(),value);
    }

    public void replace(Booking value) {
        // TODO: replace existing entity only.
        if(entries.containsKey(value.getBookingId())) entries.replace(value.getBookingId(),value);
    }

    public void remove(String bookingId) {
        // TODO: remove existing entity.
        entries.remove(bookingId);
    }

    public List<Booking> findAll() {
        return new ArrayList<>(entries.values());
    }

    public List<Booking> findByOwner(String ownerUserId) {
        List<Booking> bookings = new ArrayList<Booking>();
        for(Booking booking : entries.values()){
            if(Objects.equals(booking.getOwnerUserId(), ownerUserId)){
                bookings.add(booking);
            }
        }
        return bookings;
    }

    public List<Booking> findByRoom(String roomId) {
        List<Booking> bookings = new ArrayList<Booking>();
        for(Booking booking : entries.values()){
            if(Objects.equals(booking.getRoomId(), roomId)){
                bookings.add(booking);
            }
        }
        return bookings;
    }

}
