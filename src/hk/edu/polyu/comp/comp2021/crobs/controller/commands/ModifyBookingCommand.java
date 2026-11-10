package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;
import java.time.LocalDate;
import java.time.LocalTime;

public final class ModifyBookingCommand implements Command {
    private final CROBS crobs;
    private final OutputFormatter formatter=new OutputFormatter();
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
        return "Booking updated: "+formatter.formatBooking(crobs.modifyBooking(bookingId, roomId,date,startTime,endTime,purpose));
    }
}
