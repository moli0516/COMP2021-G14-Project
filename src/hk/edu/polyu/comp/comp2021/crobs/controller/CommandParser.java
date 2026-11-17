package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.controller.commands.*;
import hk.edu.polyu.comp.comp2021.crobs.model.FeatureSet;
import hk.edu.polyu.comp.comp2021.crobs.model.RoomType;
import hk.edu.polyu.comp.comp2021.crobs.model.UserRole;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.util.Set;
import java.util.HashSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Parses command lines into {@link Command} objects.
 */
public final class CommandParser {
    private final CROBS crobs;

    /**
     * Creates a parser bound to the given system.
     *
     * @param crobs the system model
     */
    public CommandParser(CROBS crobs) {
        this.crobs = crobs;
    }

    /**
     * Parses one command line; malformed input is rejected with a
     * {@code ValidationException}.
     *
     * @param commandLine the raw command line
     * @return the matching command wrapped for shared model history recording
     * @throws ValidationException if input is null, blank, unknown, or malformed;
     *         booking-time semantics are checked during command execution
     */
    public Command parse(String commandLine) {
        if (commandLine == null || commandLine.trim().isEmpty()) {
            throw new ValidationException("One nonblank command required");
        }
        final String name = commandLine.trim().split("\\s+", 2)[0];
        final Command delegate = parseCore(commandLine);
        return new MyCommand(name, delegate);
    }

    /**
     * Parses nonblank input without adding the history wrapper.
     *
     * @param commandLine the validated nonblank command line
     * @return the concrete command
     * @throws ValidationException if the verb or arguments are invalid
     */
    private Command parseCore(String commandLine) {
        String[] words=commandLine.trim().split("\\s+");
        String name=words[0];

        switch(name){
            case "undo":
                checkExactly(words, 1);
                return new UndoCommand(crobs);
            case "redo":
                checkExactly(words, 1);
                return new RedoCommand(crobs);
            case "addUser":
                checkAtLeast(words, 2);
                return new AddUserCommand(crobs, joinFrom(words,1));
            case "removeUser":
                checkExactly(words, 2);
                return new RemoveUserCommand(crobs, words[1]);
            case "changeRole":
                checkExactly(words, 3);
                return new ChangeRoleCommand(crobs, words[1], parseRole(words[2]));
            case "login":
                checkExactly(words, 2);
                return new LoginCommand(crobs, words[1]);
            case "logout":
                checkExactly(words, 1);
                return new LogoutCommand(crobs);
            case "addRoom":
                checkExactly(words, 4);
                return new AddRoomCommand(crobs, parseRoomType(words[1]),parseNumber(words[2]), parseFeatures(words[3]));

            case "modifyRoom":
                checkExactly(words, 4);
                return new ModifyRoomCommand(crobs,words[1],words[2],words[3]);
            case "deleteRoom":
                checkExactly(words, 2);
                return new DeleteRoomCommand(crobs, words[1]);
            case "book":
                checkAtLeast(words, 6);
                return new BookCommand(crobs, words[1],parseDate(words[2]), parseTime(words[3]),
                parseTime(words[4]),joinFrom(words, 5));
            case "modifyBooking":
                checkAtLeast(words, 7);
                return new ModifyBookingCommand(crobs, words[1], words[2], parseDate(words[3]),
                parseTime(words[4]),parseTime(words[5]),joinFrom(words, 6));
            case "deleteBooking":
                checkExactly(words, 2);
                return new DeleteBookingCommand(crobs, words[1]);
            case "search":
                checkExactly(words, 6);
                return new SearchCommand(crobs, parseDate(words[1]), parseTime(words[2]), parseTime(words[3]),
                parseNumber(words[4]), parseFeatures(words[5]));
            case "schedule":
                checkExactly(words, 3);
                return new ScheduleCommand(crobs,words[1],parseDate(words[2]));
            case "listRoom":
                checkExactly(words, 2);
                return new ListRoomCommand(crobs, words[1]);
            case "listAllRooms":
                checkExactly(words, 1);
                return new ListAllRoomsCommand(crobs);
            case "listBookings":
                checkExactly(words, 1);
                return new ListBookingsCommand(crobs);
            case "quit":
                checkExactly(words, 1);
                return new QuitCommand();
            default:
                throw new ValidationException("Unknown command: "+name);
        }
    }
    /**
     * Requires an exact token count including the command name.
     *
     * @param words the nonempty token array
     * @param count the required token count
     * @throws ValidationException if the count differs
     */
    private static void checkExactly(String[] words, int count){
        if(words.length!=count){
            throw new ValidationException("Wrong number of arguments for "+words[0]);
        }
    }
    /**
     * Requires a minimum token count including the command name.
     *
     * @param words the nonempty token array
     * @param count the minimum token count
     * @throws ValidationException if too few tokens are supplied
     */
    private static void checkAtLeast(String[] words, int count){
        if(words.length<count){
            throw new ValidationException("Missing arguments for "+ words[0]);
        }
    }
    /**
     * Joins trailing command tokens with single spaces.
     *
     * @param words the token array
     * @param start the valid first token index
     * @return the joined trailing text
     */
    private static String joinFrom(String[] words, int start){
        StringBuilder result= new StringBuilder(words[start]);
        for (int i=start+1;i<words.length;i++){
            result.append(" ").append(words[i]);
        }
        return result.toString();
    }
    /**
     * Parses an ISO date without applying reservation-time policy.
     *
     * @param text the non-null date token
     * @return the parsed date
     * @throws ValidationException if parsing fails
     */
    private static LocalDate parseDate(String text){
        try{
            return LocalDate.parse(text);
        }catch(DateTimeParseException e){
            throw new ValidationException("Invalid date '"+text+"'. use yyyy-MM-dd format.");
        }
    }
    /**
     * Parses an ISO local time without applying reservation-time policy.
     *
     * @param text the non-null time token
     * @return the parsed local time
     * @throws ValidationException if parsing fails
     */
    private static LocalTime parseTime(String text){
        try{
            return LocalTime.parse(text);
        }catch(DateTimeParseException e){
            throw new ValidationException("Invalid time '"+text+"' use HH:mm format.");
        }
    }
    /**
     * Parses a signed integer; domain bounds remain in the model.
     *
     * @param text the integer token
     * @return the parsed integer
     * @throws ValidationException if the token is not a representable integer
     */
    private static int parseNumber(String text){
        try{
            return Integer.parseInt(text);
        }catch(NumberFormatException e){
            throw new ValidationException("Invalid number");
        }
    }
    /**
     * Resolves an exact room-type command spelling.
     *
     * @param text the room-type token
     * @return the matching room type
     * @throws ValidationException if no room type matches
     */
    private static RoomType parseRoomType(String text){
        for(RoomType type:RoomType.values()){
            if (type.toCommand().equals(text)){
                return type;
            }
        }
        throw new ValidationException("Unknown room type. use lectureTheatre, computerLab, seminarRoom or meetingRoom.");
    }
    /**
     * Parses a case-insensitive assignable role.
     *
     * @param text the non-null role token
     * @return REGULAR or ADMIN
     * @throws ValidationException if the role is not assignable
     */
    private static UserRole parseRole(String text){
        if(text.equalsIgnoreCase("REGULAR")){
            return UserRole.REGULAR;
        }
        if (text.equalsIgnoreCase("ADMIN")){
            return UserRole.ADMIN;
        }
        throw new ValidationException("Invalid role. Use REGULAR or ADMIN.");
    }
    /**
     * Parses feature names, treating a dash as an empty set.
     *
     * @param text the non-null feature token
     * @return the parsed feature names
     * @throws IllegalArgumentException if feature syntax is invalid
     */
    private static Set<String> parseFeatures(String text){
        if(text.equals("-")){
            return new HashSet<>();
        }
        return FeatureSet.parse(text).asSet();
    }

    private class MyCommand implements Command {
        private final String name;
        private final Command delegate;

        public MyCommand(String name, Command delegate) {
            this.name = name;
            this.delegate = delegate;
        }

        @Override
        public String execute() {
            return crobs.executeRecorded(name, delegate::execute);
        }

        @Override
        public boolean isQuit() {
            return delegate.isQuit();
        }
    }
}
