package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.model.RoomType;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;

public final class AddRoomCommand implements Command {
    private final CROBS crobs;
    private final RoomType roomType;
    private final int capacity;
    private final Set<String> features;
    private final OutputFormatter formatter = new OutputFormatter();

    public AddRoomCommand(CROBS crobs, RoomType roomType, int capacity, Set<String> features) {
        this.crobs = crobs;
        this.roomType = roomType;
        this.capacity = capacity;
        this.features = Collections.unmodifiableSet(new HashSet<>(features));
    }

    @Override
    public String execute() {
        return "Room added: "+formatter.formatRoom(crobs.addRoom(roomType,capacity,features));
    }
}