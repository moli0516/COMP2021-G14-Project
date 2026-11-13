package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command for {@code book}: books a room for the logged-in user.
 */
public final class BookCommand implements Command {
    private final CROBS crobs;
    private final String roomId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String purpose;
    private final OutputFormatter format= new  OutputFormatter();

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs the system model
     * @param roomId the room ID
     * @param date the booking date
     * @param startTime the start time
     * @param endTime the end time
     * @param purpose the purpose of the booking
     */
    public BookCommand(CROBS crobs, String roomId, LocalDate date, LocalTime startTime, LocalTime endTime, String purpose) {
        this.crobs = crobs;
        this.roomId = roomId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
    }

    @Override
    public String execute() {
        return "Booking created: "+format.formatBooking(crobs.createBooking(roomId,date, startTime,endTime, purpose));
    }
}
