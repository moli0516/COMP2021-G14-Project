package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

public final class RemoveUserCommand implements Command {
    private final CROBS crobs;
    private final String targetUserId;

    public RemoveUserCommand(CROBS crobs, String targetUserId) {
        this.crobs = crobs;
        this.targetUserId = targetUserId;
    }

    @Override
    public String execute() {
        // TODO: call the corresponding CROBS API and format its result.
        throw new UnsupportedOperationException("TODO: implement");
    }
}
