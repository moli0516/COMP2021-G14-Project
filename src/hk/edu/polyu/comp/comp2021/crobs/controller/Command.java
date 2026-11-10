package hk.edu.polyu.comp.comp2021.crobs.controller;

public interface Command {
    String execute();
    default boolean isQuit(){return false;}
}