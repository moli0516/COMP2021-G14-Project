package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

public final class ListRoomCommand implements Command {
    private final CROBS crobs;
    private final String roomId;

    public ListRoomCommand(CROBS crobs, String roomId) {
        this.crobs = crobs;
        this.roomId = roomId;
    }

    @Override
    public String execute() {
        // TODO: call the corresponding CROBS API and format its result.
        throw new UnsupportedOperationException("TODO: implement");
    }
}
