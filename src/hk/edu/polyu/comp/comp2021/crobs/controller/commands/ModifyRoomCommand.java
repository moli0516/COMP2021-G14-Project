package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;

public final class ModifyRoomCommand implements Command {
    private final CROBS crobs;
    private final String roomId;
    private final String field;
    private final String newValue;
    private final OutputFormatter formatter=new OutputFormatter();

    public ModifyRoomCommand(CROBS crobs, String roomId, String field, String newValue) {
        this.crobs = crobs;
        this.roomId = roomId;
        this.field = field;
        this.newValue = newValue;
    }

    @Override
    public String execute() {
        // TODO: Re-enable when the room model APIs are available.
        //return "Room updated: " + formatter.formatRoom(crobs.modifyRoom(roomId, field, newValue));
        throw new UnsupportedOperationException("waiting for API");
    }
}
