package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

public final class ListAllRoomsCommand implements Command {
    private final CROBS crobs;

    public ListAllRoomsCommand(CROBS crobs) {
        this.crobs = crobs;
    }

    @Override
    public String execute() {
        // TODO: call the corresponding CROBS API and format its result.
        throw new UnsupportedOperationException("TODO: implement");
    }
}
