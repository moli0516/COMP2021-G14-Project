package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import java.time.LocalDate;

public final class ScheduleCommand implements Command {
    private final CROBS crobs;
    private final String roomId;
    private final LocalDate weekStartDate;

    public ScheduleCommand(CROBS crobs, String roomId, LocalDate weekStartDate) {
        this.crobs = crobs;
        this.roomId = roomId;
        this.weekStartDate = weekStartDate;
    }

    @Override
    public String execute() {
        // TODO: call the corresponding CROBS API and format its result.
        throw new UnsupportedOperationException("TODO: implement");
    }
}
