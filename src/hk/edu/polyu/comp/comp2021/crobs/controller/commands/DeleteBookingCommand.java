package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

public final class DeleteBookingCommand implements Command {
    private final CROBS crobs;
    private final String bookingId;

    public DeleteBookingCommand(CROBS crobs, String bookingId) {
        this.crobs = crobs;
        this.bookingId = bookingId;
    }

    @Override
    public String execute() {
        // TODO: call the corresponding CROBS API and format its result.
        throw new UnsupportedOperationException("TODO: implement");
    }
}
