package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

/**
 * Command for {@code removeUser}: removes a regular user and their bookings.
 */
public final class RemoveUserCommand implements Command {
    private final CROBS crobs;
    private final String targetUserId;

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs the system model
     * @param targetUserId the ID of the target user
     */
    public RemoveUserCommand(CROBS crobs, String targetUserId) {
        this.crobs = crobs;
        this.targetUserId = targetUserId;
    }

    @Override
    public String execute() {
        crobs.removeUser(targetUserId);
        return "Removed: "+targetUserId;
    }
}
