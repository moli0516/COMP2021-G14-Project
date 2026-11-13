package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

/**
 * Supported room categories and their case-sensitive command names.
 */
public enum RoomType {
    /**
     * Lecture theatre.
     */
    LECTURE_THEATRE("lectureTheatre"),
    /**
     * Computer laboratory.
     */
    COMPUTER_LAB("computerLab"),
    /**
     * Seminar room.
     */
    SEMINAR_ROOM("seminarRoom"),
    /**
     * Meeting room.
     */
    MEETING_ROOM("meetingRoom");

    private final String commandName;


    /**
     * Associates a room category with its command spelling.
     *
     * @param commandName the case-sensitive command name
     */
    RoomType(String commandName) {
        this.commandName = commandName;
    }

    /**
     * Returns the command spelling for this room category.
     *
     * @return the case-sensitive command name
     */
    public String toCommand() {
        return commandName;
    }

    /**
     * Resolves an exact, case-sensitive command name.
     *
     * @param text the room-type command name
     * @return the matching category
     * @throws ValidationException if text is null or unsupported
     */
    public static RoomType fromCommand(String text) {
        if (text != null) {
            for (RoomType type : values()) {
                if (type.toCommand().equals(text)) {
                    return type;
                }
            }
        }

        throw new ValidationException("Unsupported room type: " + text);
    }
}
