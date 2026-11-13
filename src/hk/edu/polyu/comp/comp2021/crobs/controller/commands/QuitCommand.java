package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

/**
 * Command for {@code quit}: ends the session.
 */
public final class QuitCommand implements Command {
    private final CROBS crobs;

    /**
     * Creates the command with its parsed arguments.
     *
     * @param crobs the system model
     */
    public QuitCommand(CROBS crobs) {
        this.crobs = crobs;
    }
    @Override 
    public boolean isQuit(){
        return true;
    }
    @Override
    public String execute() {
        return "Bye.";
    }
}
