package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.util.*;

public final class BookingRepository {
    private final Map<String, Booking> entries = new HashMap<>();

    public Booking require(String bookingId) {
        Booking booking = entries.get(bookingId);
        if(booking == null){
            throw new NotFoundException("The booking is not found");
        }
        return booking;
    }

    public void add(Booking value) {
        if(!entries.containsKey(value.getBookingId())) entries.put(value.getBookingId(),value);
        else throw new ValidationException("The booking exists already");
    }

    public void replace(Booking value) {
        if(entries.containsKey(value.getBookingId())) entries.replace(value.getBookingId(),value);
        else throw new NotFoundException("The booking is not found");
    }

    public void remove(String bookingId) {
        if(entries.containsKey(bookingId)) entries.remove(bookingId);
        else throw new NotFoundException("The booking is not found");
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
