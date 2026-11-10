package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;

public final class LoginCommand implements Command {
    private final CROBS crobs;
    private final String userId;
    private final OutputFormatter formatter=new OutputFormatter();

    public LoginCommand(CROBS crobs, String userId) {
        this.crobs = crobs;
        this.userId = userId;
    }

    @Override
    public String execute() {
        crobs.login(userId);
        return "Logged in as "+formatter.formatUser(crobs.getCurrentUser());
    }
}
