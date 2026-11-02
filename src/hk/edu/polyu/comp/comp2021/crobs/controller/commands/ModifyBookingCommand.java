package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import java.time.LocalDate;
import java.time.LocalTime;

public final class ModifyBookingCommand implements Command {
    private final CROBS crobs;
    private final String bookingId;
    private final String roomId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String purpose;

    public ModifyBookingCommand(CROBS crobs, String bookingId, String roomId, LocalDate date, LocalTime startTime, LocalTime endTime, String purpose) {
        this.crobs = crobs;
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
    }

    @Override
    public String execute() {
        // TODO: call the corresponding CROBS API and format its result.
        throw new UnsupportedOperationException("TODO: implement");
    }
}
