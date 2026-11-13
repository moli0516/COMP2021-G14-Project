package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

/**
 * Command for {@code logout}: logs out the current user.
 */
public final class LogoutCommand implements Command {
    private final CROBS crobs;

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs the system model
     */
    public LogoutCommand(CROBS crobs) {
        this.crobs = crobs;
    }

    @Override
    public String execute() {
        crobs.logout();
        return "Logged out.";
    }
}
