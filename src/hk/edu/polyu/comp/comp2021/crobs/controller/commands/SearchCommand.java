package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.time.LocalDate;
import java.time.LocalTime;

public final class SearchCommand implements Command {
    private final OutputFormatter formatter=new OutputFormatter();
    private final CROBS crobs;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int minCapacity;
    private final Set<String> features;

    public SearchCommand(CROBS crobs, LocalDate date, LocalTime startTime, LocalTime endTime, int minCapacity, Set<String> features) {
        this.crobs = crobs;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minCapacity = minCapacity;
        this.features = Collections.unmodifiableSet(new HashSet<>(features));
    }

    @Override
    public String execute() {
        return formatter.formatRooms(crobs.searchRooms(date,startTime,endTime,minCapacity,features));
    }
}
