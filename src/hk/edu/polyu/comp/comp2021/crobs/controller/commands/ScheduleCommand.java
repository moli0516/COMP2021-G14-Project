package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import java.time.LocalDate;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;

/**
 * Command for {@code schedule}: shows a room's bookings for a 7-day period.
 */
public final class ScheduleCommand implements Command {
    private final CROBS crobs;
    private final LocalDate weekStartDate;
    private final String roomId;
    private final OutputFormatter formatter= new OutputFormatter();

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs the system model
     * @param roomId the room ID
     * @param weekStartDate the first day of the 7-day period
     */
    public ScheduleCommand(CROBS crobs, String roomId, LocalDate weekStartDate) {
        this.crobs = crobs;
        this.roomId = roomId;
        this.weekStartDate = weekStartDate;
    }

    @Override
    public String execute() {
        return formatter.formatSchedule(crobs.getWeeklySchedule(roomId, weekStartDate), crobs.getCurrentUser());

    }
}
