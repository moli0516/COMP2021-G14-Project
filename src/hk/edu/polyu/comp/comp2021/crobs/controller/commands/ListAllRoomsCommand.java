package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;

public final class ListAllRoomsCommand implements Command {
    private final CROBS crobs;
    private final OutputFormatter formatter=new OutputFormatter();

    public ListAllRoomsCommand(CROBS crobs) {
        this.crobs = crobs;
    }

    @Override
    public String execute() {
        //TODO: Re-enable when the room model APIs are available.
        //return formatter.formatRooms(crobs.getAllRooms());
        throw new UnsupportedOperationException("Waiting for API.");
    }
}
