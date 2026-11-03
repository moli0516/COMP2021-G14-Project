package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public class CROBS {

    private UserRepository users;
    private BookingRepository bookings;
    private RoomRepository rooms;
    private IdGenerator idGenerator;

    private String currentUserId;

    public CROBS(){
        this.users = new UserRepository();
        this.bookings = new BookingRepository();
        this.rooms = new RoomRepository();
        this.idGenerator = new IdGenerator();

        this.currentUserId = null;
    }

    public User getCurrentUser(){
        return users.require(currentUserId);
    }

    public void login(String userId){
        if (currentUserId != null) {
            throw new AuthorizationException("Please logout before login");
        }
        if (currentUserId == null && userId.trim().isEmpty()){
            throw new ValidationException("The userId is null");
        }
        User user = users.require(userId);
        currentUserId = user.getUserId();
    }

    public User addUser(String name){
        requiredAdmin();

        String userId = idGenerator.nextUserId();
        User user = new RegularUser(userId, name);
        users.add(user);
        return user;
    }

    public void removeUser(String targetUserId){
        requiredAdmin();

        users.remove(targetUserId);
    }

    public void logout(){
        requiredLoggedIn();

        currentUserId = null;
    }

    private User requiredLoggedIn(){
        User user = getCurrentUser();
        if(user == null){
            throw new AuthorizationException("Please login first");
        }
        return user;
    }

    private User requiredAdmin(){
        User user = getCurrentUser();
        if(!user.isAdmin()){
            throw new AuthorizationException("Admin privilege is required");
        }
        return user;
    }

    private User requireSuperAdmin(){
        User user = getCurrentUser();
        if(!user.canChangeRoles()){
            throw new AuthorizationException("Admin privilege is required");
        }
        return user;
    }

    public Room addRoom(RoomType type, int capacity, Set<String> features){
        requiredAdmin();

        Room room = new Room(idGenerator.nextRoomId(), type, capacity, new FeatureSet(features));
        rooms.add(room);
        return room;
    }

    public void deleteRoom(String roomId){
        requiredAdmin();
        
        rooms.remove(roomId);
    }

    public Booking createBooking(String roomId, LocalDate date,
                                 LocalTime startTime, LocalTime endTime, String purpose){
        requiredLoggedIn();
        
        Booking booking = new Booking(idGenerator.nextBookingId(),currentUserId,roomId,date,startTime,endTime,purpose,true);
        bookings.add(booking);
        return booking;
    }

    public Booking modifyBooking(String bookingId, String roomId,
                          LocalDate date, LocalTime startTime, LocalTime endTime,
                          String purpose){
        requiredLoggedIn();

        Booking booking = new Booking(bookingId,currentUserId,roomId,date,startTime,endTime,purpose,true);
        bookings.replace(booking);;
        return booking;
    }

    void deleteBooking(String bookingId){
        requiredLoggedIn();

        bookings.remove(bookingId);
    }

}
