package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;

/**
 * Command for {@code modifyRoom}: changes one field of a room.
 */
public final class ModifyRoomCommand implements Command {
    private final CROBS crobs;
    private final String roomId;
    private final String field;
    private final String newValue;
    private final OutputFormatter formatter=new OutputFormatter();

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs the system model
     * @param roomId the room ID
     * @param field the name of the field to change
     * @param newValue the new value
     */
    public ModifyRoomCommand(CROBS crobs, String roomId, String field, String newValue) {
        this.crobs = crobs;
        this.roomId = roomId;
        this.field = field;
        this.newValue = newValue;
    }

    @Override
    public String execute() {
        return "Room updated: " + formatter.formatRoom(crobs.modifyRoom(roomId, field, newValue));
    }
}
