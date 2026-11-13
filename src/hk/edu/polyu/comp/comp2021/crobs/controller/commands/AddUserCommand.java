package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;

/**
 * Command for {@code addUser}: adds a new regular user.
 */
public final class AddUserCommand implements Command {
    private final CROBS crobs;
    private final String name;
    private final OutputFormatter formatter = new OutputFormatter();

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs the system model
     * @param name  the user's name
     */
    public AddUserCommand(CROBS crobs, String name) {
        this.crobs = crobs;
        this.name = name;
    }

    @Override
    public String execute() {
        return "User added: "+formatter.formatUser(crobs.addUser(name));
    }
}
