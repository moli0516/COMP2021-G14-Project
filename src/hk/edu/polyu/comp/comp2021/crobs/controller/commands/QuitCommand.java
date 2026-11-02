package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

public final class QuitCommand implements Command {
    private final CROBS crobs;

    public QuitCommand(CROBS crobs) {
        this.crobs = crobs;
    }

    @Override
    public String execute() {
        // TODO: signal loop termination without System.exit in the model.
        throw new UnsupportedOperationException("TODO: implement");
    }
}
