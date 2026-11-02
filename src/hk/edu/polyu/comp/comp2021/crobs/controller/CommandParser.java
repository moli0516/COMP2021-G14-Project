package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

public final class CommandParser {
    private final CROBS crobs;

    public CommandParser(CROBS crobs) {
        this.crobs = crobs;
    }

    public Command parse(String commandLine) {
        // TODO: validate name/argument count, parse types and construct a command.
        // TODO: support names/purposes containing spaces; reject malformed input.
        throw new UnsupportedOperationException("TODO: implement parsing");
    }
}
