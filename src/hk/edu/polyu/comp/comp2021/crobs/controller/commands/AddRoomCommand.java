package hk.edu.polyu.comp.comp2021.crobs.controller.commands;

import hk.edu.polyu.comp.comp2021.crobs.controller.Command;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.model.RoomType;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;

public final class AddRoomCommand implements Command {
    private final CROBS crobs;
    private final RoomType roomType;
    private final int capacity;
    private final Set<String> features;

    public AddRoomCommand(CROBS crobs, RoomType roomType, int capacity, Set<String> features) {
        this.crobs = crobs;
        this.roomType = roomType;
        this.capacity = capacity;
        this.features = Collections.unmodifiableSet(new HashSet<>(features));
    }

    @Override
    public String execute() {
        // TODO: call the corresponding CROBS API and format its result.
        throw new UnsupportedOperationException("TODO: implement");
    }
}
