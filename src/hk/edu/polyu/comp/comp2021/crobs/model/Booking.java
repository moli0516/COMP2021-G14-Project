package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;

public final class Booking {
    private final String bookingId;
    private final String ownerUserId;
    private final String roomId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String purpose;
    private final boolean cancelled;

    public Booking(String bookingId, String ownerUserId, String roomId,
                   LocalDate date, LocalTime startTime, LocalTime endTime,
                   String purpose, boolean cancelled) {
        if(bookingId == null || bookingId.isBlank()){
            throw new ValidationException("Booking id cannot be null");
        }
        if(ownerUserId == null || ownerUserId.isBlank()){
            throw new ValidationException("Owner user id cannot be null");
        }
        if(roomId == null || roomId.isBlank()){
            throw new ValidationException("Room cannot be null");
        }
        if(purpose == null || purpose.isBlank()){
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

    public String getBookingId() { return bookingId; }
    public String getOwnerUserId() { return ownerUserId; }
    public String getRoomId() { return roomId; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getPurpose() { return purpose; }
    public boolean isCancelled() { return cancelled; }

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

    public Booking withDetails(String newRoomId, LocalDate newDate,
                               LocalTime newStart, LocalTime newEnd,
                               String newPurpose) {
        return new Booking(this.bookingId, this.ownerUserId, newRoomId, newDate, newStart, newEnd, newPurpose, this.cancelled);
    }

    public Booking cancel() {
        if (isCancelled()) throw new ValidationException("The booking already canceled");
        return new Booking(this.bookingId,this.ownerUserId,this.roomId,this.date,this.startTime,this.endTime,this.purpose,true);
    }
}
