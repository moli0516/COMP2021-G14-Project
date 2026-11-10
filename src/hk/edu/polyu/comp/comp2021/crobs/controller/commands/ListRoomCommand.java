package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;

public final class ListRoomCommand implements Command {
    private final CROBS crobs;
    private final String roomId;
    private final OutputFormatter formatter=new OutputFormatter();

    public ListRoomCommand(CROBS crobs, String roomId) {
        this.crobs = crobs;
        this.roomId = roomId;
    }

    @Override
    public String execute() {
        //TODO: Re-enable when the room model APIs are available.
        //return formatter.formatRoom(crobs.getRoom(roomId));
        throw new UnsupportedOperationException("Waiting for API.");
    }
}
