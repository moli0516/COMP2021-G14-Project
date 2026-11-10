package hk.edu.polyu.comp.comp2021.crobs.controller;
import  hk.edu.polyu.comp.comp2021.crobs.model.exceptions.CrobsException;

public final class CommandProcessor {
    private final CommandParser parser;
    private boolean quitRequest;

    public CommandProcessor(CommandParser parser) {
        this.parser = parser;
    }

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
    public boolean isQuitRequested(){
        return quitRequest;
    }
}
