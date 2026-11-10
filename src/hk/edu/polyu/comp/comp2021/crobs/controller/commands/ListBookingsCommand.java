package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;

public final class ListBookingsCommand implements Command {
    private final CROBS crobs;
    private final OutputFormatter formatter=new OutputFormatter();

    public ListBookingsCommand(CROBS crobs) {
        this.crobs = crobs;
    }

    @Override
    public String execute() {
        return formatter.formatBookings(crobs.getCurrentUserBookings());
    }
}
