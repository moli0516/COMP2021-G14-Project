package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

/**
 * Command for {@code deleteBooking}: deletes one of the user's own bookings.
 */
public final class DeleteBookingCommand implements Command {
    private final CROBS crobs;
    private final String bookingId;

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs the system model
     * @param bookingId the booking ID
     */
    public DeleteBookingCommand(CROBS crobs, String bookingId) {
        this.crobs = crobs;
        this.bookingId = bookingId;
    }

    @Override
    public String execute() {
        crobs.deleteBooking(bookingId);
        return "Booking deleted: "+bookingId;
    }
}
