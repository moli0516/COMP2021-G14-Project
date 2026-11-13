package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * An immutable booking with a room, owner, date, time interval, and cancellation state.
 */
public final class Booking {
    private final String bookingId;
    private final String ownerUserId;
    private final String roomId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String purpose;
    private final boolean cancelled;

    /**
     * Creates and validates a booking; no repository or authorization checks are performed.
     *
     * @param bookingId the nonblank booking identifier
     * @param ownerUserId the nonblank owner identifier
     * @param roomId the nonblank room identifier
     * @param date the booking date
     * @param startTime the inclusive start time
     * @param endTime the exclusive end time, after the start
     * @param purpose the nonblank booking purpose
     * @param cancelled whether the booking is cancelled
     * @throws ValidationException if a required value is null or blank, or the interval is not increasing
     */
    public Booking(String bookingId, String ownerUserId, String roomId,
                   LocalDate date, LocalTime startTime, LocalTime endTime,
                   String purpose, boolean cancelled) {
        if(bookingId == null || bookingId.trim().isEmpty()){
            throw new ValidationException("Booking id cannot be null");
        }
        if(ownerUserId == null || ownerUserId.trim().isEmpty()){
            throw new ValidationException("Owner user id cannot be null");
        }
        if(roomId == null || roomId.trim().isEmpty()){
            throw new ValidationException("Room cannot be null");
        }
        if(purpose == null || purpose.trim().isEmpty()){
            throw new ValidationException("Room cannot be null");
        }
        if(date == null){
            throw new ValidationException("Date cannot be null");
        }
        if(startTime == null ){
            throw new ValidationException("Start time cannot be null");
        }
        if(endTime == null){
            throw new ValidationException("End time cannot be null");
        }
        if(startTime.isAfter(endTime) || startTime.equals(endTime)){
            throw new ValidationException("End time is greater or equal to the start time");
        }
        this.bookingId = bookingId;
        this.ownerUserId = ownerUserId;
        this.roomId = roomId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.cancelled = cancelled;
    }

    /**
     * Returns the booking's booking id.
     *
     * @return the stored bookingId
     */
    public String getBookingId() { return bookingId; }
    /**
     * Returns the booking's owner user id.
     *
     * @return the stored ownerUserId
     */
    public String getOwnerUserId() { return ownerUserId; }
    /**
     * Returns the booking's room id.
     *
     * @return the stored roomId
     */
    public String getRoomId() { return roomId; }
    /**
     * Returns the booking's date.
     *
     * @return the stored date
     */
    public LocalDate getDate() { return date; }
    /**
     * Returns the booking's start time.
     *
     * @return the stored startTime
     */
    public LocalTime getStartTime() { return startTime; }
    /**
     * Returns the booking's end time.
     *
     * @return the stored endTime
     */
    public LocalTime getEndTime() { return endTime; }
    /**
     * Returns the booking's purpose.
     *
     * @return the stored purpose
     */
    public String getPurpose() { return purpose; }
    /**
     * Reports the cancellation state.
     *
     * @return {@code true} if cancelled
     */
    public boolean isCancelled() { return cancelled; }

    /**
     * Tests overlap of time intervals only; date and cancellation state are not checked.
     * Adjacent intervals that share only an endpoint do not overlap.
     *
     * @param otherStart the other interval's inclusive start
     * @param otherEnd the other interval's exclusive end
     * @return whether the intervals overlap
     * @throws ValidationException if either time is null or the interval is not increasing
     */
    public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
        if (otherStart == null || otherEnd == null) {
            throw new ValidationException(
                    "Start time and end time cannot be null."
            );
        }

        if (!otherStart.isBefore(otherEnd)) {
            throw new ValidationException(
                    "Start time must be before end time."
            );
        }

        return startTime.isBefore(otherEnd) && otherStart.isBefore(endTime);
    }

    /**
     * Creates a validated copy with new details, preserving ID, owner, and cancellation state.
     *
     * @param newRoomId the new room ID
     * @param newDate the new date
     * @param newStart the new start time
     * @param newEnd the new end time
     * @param newPurpose the new purpose
     * @return a new booking
     * @throws ValidationException if the replacement details are invalid
     */
    public Booking withDetails(String newRoomId, LocalDate newDate,
                               LocalTime newStart, LocalTime newEnd,
                               String newPurpose) {
        return new Booking(this.bookingId, this.ownerUserId, newRoomId, newDate, newStart, newEnd, newPurpose, this.cancelled);
    }

    /**
     * Creates a cancelled copy without modifying this booking.
     *
     * @return a new cancelled booking
     * @throws ValidationException if this booking is already cancelled
     */
    public Booking cancel() {
        if (isCancelled()) throw new ValidationException("The booking already canceled");
        return new Booking(this.bookingId,this.ownerUserId,this.roomId,this.date,this.startTime,this.endTime,this.purpose,true);
    }
}
