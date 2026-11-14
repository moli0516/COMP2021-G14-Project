package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

/** Applies the undo operation to this model's current session history. */
public final class UndoCommand implements Command {
    private final CROBS crobs;

    /** @param crobs model owning the history */
    public UndoCommand(CROBS crobs) { this.crobs = crobs; }

    @Override
    public String execute() { return crobs.undo(); }
}
