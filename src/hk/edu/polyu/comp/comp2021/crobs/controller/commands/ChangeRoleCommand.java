package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.model.UserRole;

public final class ChangeRoleCommand implements Command {
    private final CROBS crobs;
    private final String targetUserId;
    private final UserRole newRole;

    public ChangeRoleCommand(CROBS crobs, String targetUserId, UserRole newRole) {
        this.crobs = crobs;
        this.targetUserId = targetUserId;
        this.newRole = newRole;
    }

    @Override
    public String execute() {
        // TODO: call the corresponding CROBS API and format its result.
        throw new UnsupportedOperationException("TODO: implement");
    }
}
