package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;

/**
 * Command for {@code listBookings}: lists the current user's bookings.
 */
public final class ListBookingsCommand implements Command {
    private final CROBS crobs;
    private final OutputFormatter formatter=new OutputFormatter();

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs the system model
     */
    public ListBookingsCommand(CROBS crobs) {
        this.crobs = crobs;
    }

    @Override
    public String execute() {
        return formatter.formatBookings(crobs.getCurrentUserBookings());
    }
}
