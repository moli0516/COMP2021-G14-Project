package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

/**
 * Command for {@code deleteRoom}: deletes a room and cancels its future bookings.
 */
public final class DeleteRoomCommand implements Command {
    private final CROBS crobs;
    private final String roomId;

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs the system model
     * @param roomId the room ID
     */
    public DeleteRoomCommand(CROBS crobs, String roomId) {
        this.crobs = crobs;
        this.roomId = roomId;
    }

    @Override
    public String execute() {
        crobs.deleteRoom(roomId);
        return "Room deleted: " + roomId;
    }
}

