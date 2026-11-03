package hk.edu.polyu.comp.comp2021.crobs.model;

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
        // TODO: enforce nonblank IDs/purpose, nonnull values and start < end.
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
        // Caller must also check room, date and cancellation state.
        throw new UnsupportedOperationException("TODO: half-open interval overlap");
    }

    public Booking withDetails(String newRoomId, LocalDate newDate,
                               LocalTime newStart, LocalTime newEnd,
                               String newPurpose) {
        throw new UnsupportedOperationException("TODO: half-open interval overlap");
    }

    public Booking cancel() {
        throw new UnsupportedOperationException("TODO: return cancelled copy");
    }
}
