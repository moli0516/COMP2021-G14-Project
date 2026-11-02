package hk.edu.polyu.comp.comp2021.crobs.model;

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
        throw new UnsupportedOperationException("TODO: validate and parse room type");
    }
}
