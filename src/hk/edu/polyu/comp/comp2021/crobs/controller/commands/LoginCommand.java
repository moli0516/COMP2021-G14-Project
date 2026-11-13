package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;

/**
 * Command for {@code login}: logs in as an existing user.
 */
public final class LoginCommand implements Command {
    private final CROBS crobs;
    private final String userId;
    private final OutputFormatter formatter=new OutputFormatter();

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs  the system model
     * @param userId the user ID
     */
    public LoginCommand(CROBS crobs, String userId) {
        this.crobs = crobs;
        this.userId = userId;
    }

    @Override
    public String execute() {
        crobs.login(userId);
        return "Logged in as "+formatter.formatUser(crobs.getCurrentUser());
    }
}
