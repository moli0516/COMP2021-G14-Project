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

public final class CommandParser {
    private final CROBS crobs;

    public CommandParser(CROBS crobs) {
        this.crobs = crobs;
    }

    public Command parse(String commandLine) {
        String[] words=commandLine.trim().split("\\s+");
        String name=words[0];

        switch(name){
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
                return new QuitCommand(crobs);
            default:
                throw new ValidationException("Unknown command: "+name);
        }
    }
    private static void checkExactly(String[] words, int count){
        if(words.length!=count){
            throw new ValidationException("Wrong number of arguments for "+words[0]);
        }
    }
    private static void checkAtLeast(String[] words, int count){
        if(words.length<count){
            throw new ValidationException("Missing arguments for "+ words[0]);
        }
    }
    private static String joinFrom(String[] words, int start){
        String result=words[start];
        for (int i=start+1;i<words.length;i++){
            result=result+" "+words[i];
        }
        return result;
    }
    private static LocalDate parseDate(String text){
        try{
            return LocalDate.parse(text);
        }catch(DateTimeParseException e){
            throw new ValidationException("Invalid date '"+text+"'. use yyyy-MM-dd format.");
        }
    }
    private static LocalTime parseTime(String text){
        try{
            return LocalTime.parse(text);
        }catch(DateTimeParseException e){
            throw new ValidationException("Invalid time '"+text+"' use HH:mm format.");
        }
    }
    private static int parseNumber(String text){
        try{
            return Integer.parseInt(text);
        }catch(NumberFormatException e){
            throw new ValidationException("Invalid number");
        }
    }
    private static RoomType parseRoomType(String text){
        for(RoomType type:RoomType.values()){
            if (type.toCommand().equals(text)){
                return type;
            }
        }
        throw new ValidationException("Unknown room type. use lectureTheatre, computerLab, seminarRoom or meetingRoom.");
    }
    private static UserRole parseRole(String text){
        if(text.equalsIgnoreCase("REGULAR")){
            return UserRole.REGULAR;
        }
        if (text.equalsIgnoreCase("ADMIN")){
            return UserRole.ADMIN;
        }
        throw new ValidationException("Invalid role. Use REGULAR or ADMIN.");
    }
    private static Set<String> parseFeatures(String text){
        if(text.equals("-")){
            return new HashSet<String>();
        }
        return FeatureSet.parse(text).asSet();
    }
}
