package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.model.UserRole;

/**
 * Command for {@code changeRole}: changes another user's role (superadmin only).
 */
public final class ChangeRoleCommand implements Command {
    private final CROBS crobs;
    private final String targetUserId;
    private final UserRole newRole;

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs        the system model
     * @param targetUserId the ID of the target user
     * @param newRole      the new role
     */
    public ChangeRoleCommand(CROBS crobs, String targetUserId, UserRole newRole) {
        this.crobs = crobs;
        this.targetUserId = targetUserId;
        this.newRole = newRole;
    }

    @Override
    public String execute() {
        crobs.changeRole(targetUserId, newRole);
        return "Role of " + targetUserId + " changes to " + newRole + ".";
    }
}
