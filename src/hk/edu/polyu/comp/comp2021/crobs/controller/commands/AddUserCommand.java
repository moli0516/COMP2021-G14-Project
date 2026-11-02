package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

public final class AddUserCommand implements Command {
    private final CROBS crobs;
    private final String name;

    public AddUserCommand(CROBS crobs, String name) {
        this.crobs = crobs;
        this.name = name;
    }

    @Override
    public String execute() {
        // TODO: call the corresponding CROBS API and format its result.
        throw new UnsupportedOperationException("TODO: implement");
    }
}
