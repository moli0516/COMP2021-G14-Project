package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;

/**
 * Command for {@code listRoom}: shows one room.
 */
public final class ListRoomCommand implements Command {
    private final CROBS crobs;
    private final String roomId;
    private final OutputFormatter formatter=new OutputFormatter();

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs  the system model
     * @param roomId the room ID
     */
    public ListRoomCommand(CROBS crobs, String roomId) {
        this.crobs = crobs;
        this.roomId = roomId;
    }

    @Override
    public String execute() {
        return formatter.formatRoom(crobs.getRoom(roomId));
    }
}
