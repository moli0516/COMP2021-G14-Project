package hk.edu.polyu.comp.comp2021.crobs.controller;

public final class CommandProcessor {
    private final CommandParser parser;

    public CommandProcessor(CommandParser parser) {
        this.parser = parser;
    }

    public String process(String commandLine) {
        // TODO: parse, execute and translate expected input/domain errors.
        // Keep raw-line logging in the application loop; avoid logging twice.
        throw new UnsupportedOperationException("TODO: implement dispatch");
    }
}
