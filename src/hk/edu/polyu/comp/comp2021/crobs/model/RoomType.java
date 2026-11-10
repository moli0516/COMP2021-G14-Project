package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

public enum RoomType {
    LECTURE_THEATRE("lectureTheatre"),
    COMPUTER_LAB("computerLab"),
    SEMINAR_ROOM("seminarRoom"),
    MEETING_ROOM("meetingRoom");

    private final String commandName;

    RoomType(String commandName) {
        this.commandName = commandName;
    }

    public String toCommand() {
        return commandName;
    }

    public static RoomType fromCommand(String text) {
        if (text != null) {
            for (RoomType type : values()) {
                if (type.commandName.equals(text)) {
                    return type;
                }
            }
        }

        throw new ValidationException("Unsupported room type: " + text);
    }
}
