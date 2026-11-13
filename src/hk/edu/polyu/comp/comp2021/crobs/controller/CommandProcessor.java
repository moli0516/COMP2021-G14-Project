package hk.edu.polyu.comp.comp2021.crobs.controller;
import  hk.edu.polyu.comp.comp2021.crobs.model.exceptions.CrobsException;

/**
 * Runs command lines and turns model errors into readable messages.
 */
public final class CommandProcessor {
    private final CommandParser parser;
    private boolean quitRequest;

    /**
     * Creates a processor that uses the given parser.
     *
     * @param parser the parser that builds commands
     */
    public CommandProcessor(CommandParser parser) {
        this.parser = parser;
    }

    /**
     * Parses and executes one command line.
     *
     * @param commandLine the raw command line
     * @return the command output, or an {@code Error: ...} message if it fails
     */
    public String process(String commandLine) {
        try{
            Command command= parser.parse(commandLine);
            String result=command.execute();
            if(command.isQuit()){
                quitRequest=true;
            }
            return result;
        }catch(CrobsException e){
            return "Error: "+e.getMessage();
        }
    }

    /**
     * Tells whether a {@code quit} command has been executed.
     *
     * @return {@code true} once {@code quit} has run
     */
    public boolean isQuitRequested(){
        return quitRequest;
    }
}
