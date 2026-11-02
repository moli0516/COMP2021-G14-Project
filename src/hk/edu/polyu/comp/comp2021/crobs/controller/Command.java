package hk.edu.polyu.comp.comp2021.crobs.controller;

public interface Command {
    /** Execute this command; expected domain errors may propagate to the processor. */
    String execute();
}
