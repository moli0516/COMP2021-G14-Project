package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;

/**
 * Command for {@code quit}: ends the session.
 */
public final class QuitCommand implements Command {

    /**
     * Creates the command with its parsed arguments.
     *
     */
    public QuitCommand() {
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
