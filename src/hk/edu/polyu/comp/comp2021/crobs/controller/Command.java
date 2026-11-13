package hk.edu.polyu.comp.comp2021.crobs.controller;

/**
 * Command interface to be implemented by the commands.
 */
public interface Command {
    /**
     * Executes this command.
     *
     * @return the message to show to the user
     */
    String execute();
    /**
     * Tells whether executing this command ends the session.
     *
     * @return {@code true} if the application should quit
     */
    default boolean isQuit(){return false;}
}