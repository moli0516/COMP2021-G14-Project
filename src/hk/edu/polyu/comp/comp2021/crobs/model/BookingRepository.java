package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.util.*;

/**
 * Stores booking objects by ID in memory; list queries return independent, unsorted lists.
 */
public final class BookingRepository {
    private final Map<String, Booking> entries = new HashMap<>();

    /**
     * Finds an existing booking by ID.
     *
     * @param bookingId the identifier to find
     * @return the stored booking
     * @throws NotFoundException if no entry has the supplied ID
     */
    public Booking require(String bookingId) {
        Booking booking = entries.get(bookingId);
        if(booking == null){
            throw new NotFoundException("The booking is not found");
        }
        return booking;
    }

    /**
     * Adds an entry without replacing an existing ID.
     *
     * @param value the non-null booking to add
     * @throws ValidationException if the ID already exists
     * @throws NullPointerException if value is null
     */
    public void add(Booking value) {
        if(!entries.containsKey(value.getBookingId())) entries.put(value.getBookingId(),value);
        else throw new ValidationException("The booking exists already");
    }

    /**
     * Replaces an entry that already exists.
     *
     * @param value the non-null replacement with an existing ID
     * @throws NotFoundException if the ID does not exist
     * @throws NullPointerException if value is null
     */
    public void replace(Booking value) {
        if(entries.containsKey(value.getBookingId())) entries.replace(value.getBookingId(),value);
        else throw new NotFoundException("The booking is not found");
    }

    /**
     * Removes the entry with the supplied ID.
     *
     * @param bookingId the identifier to remove
     * @throws NotFoundException if the ID does not exist
     */
    public void remove(String bookingId) {
        if(entries.containsKey(bookingId)) entries.remove(bookingId);
        else throw new NotFoundException("The booking is not found");
    }

    /**
     * Returns a new list of all stored entries.
     *
     * @return an unsorted list, possibly empty; modifying it does not change this repository
     */
    public List<Booking> findAll() {
        return new ArrayList<>(entries.values());
    }

    /**
     * Finds matching bookings, including cancelled and past bookings.
     *
     * @param ownerUserId the identifier to match
     * @return a new unsorted list of matching bookings, possibly empty
     */
    public List<Booking> findByOwner(String ownerUserId) {
        List<Booking> bookings = new ArrayList<>();
        for(Booking booking : entries.values()){
            if(Objects.equals(booking.getOwnerUserId(), ownerUserId)){
                bookings.add(booking);
            }
        }
        return bookings;
    }

    /**
     * Finds matching bookings, including cancelled and past bookings.
     *
     * @param roomId the identifier to match
     * @return a new unsorted list of matching bookings, possibly empty
     */
    public List<Booking> findByRoom(String roomId) {
        List<Booking> bookings = new ArrayList<>();
        for(Booking booking : entries.values()){
            if(Objects.equals(booking.getRoomId(), roomId)){
                bookings.add(booking);
            }
        }
        return bookings;
    }

}
