package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.BookingConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;
import java.util.Comparator;
import java.util.List;

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
        User superAdmin = new SuperAdmin("SA001", "superadmin");
        users.add(superAdmin);
    }

    public User getCurrentUser(){
        try {
            return users.require(currentUserId);
        } catch (Exception e) {
            return null;
        }
    }

    public void login(String userId){
        if (currentUserId != null) {
            throw new AuthorizationException("Please logout before login");
        }
        if (currentUserId == null){
            if(userId == null || userId.trim().isEmpty()) throw new ValidationException("The userId is blanked");
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

    public void removeUser(String targetUserId) {
        requiredAdmin();

        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            throw new ValidationException("Target user ID must not be blank");
        }
        User target = users.require(targetUserId);
        if (target.getRole() != UserRole.REGULAR) {
            throw new AuthorizationException(
                    "Only regular users can be removed");
        }
        for (Booking booking : bookings.findByOwner(target.getUserId())) {
            bookings.remove(booking.getBookingId());
        }
        users.remove(target.getUserId());
    }

    public void changeRole(String userId, UserRole newRole) {
        requireSuperAdmin();
        if (userId == null || userId.trim().isEmpty()) {
            throw new ValidationException("User ID must not be blank");
        }
        if (newRole == null) {
            throw new ValidationException("New role must not be null");
        }

        User target = users.require(userId);

        if (target.canChangeRoles()) {
            throw new AuthorizationException(
                    "The superadmin's role cannot be changed");
        }
        if (newRole != UserRole.REGULAR && newRole != UserRole.ADMIN) {
            throw new ValidationException(
                    "New role must be REGULAR or ADMIN");
        }
        if (target.getRole() == newRole) {
            throw new ValidationException(
                    "User already has role " + newRole);
        }

        User replacement;

        if (newRole == UserRole.ADMIN) {
            replacement = new AdminUser(
                    target.getUserId(), target.getName());
        } else {
            replacement = new RegularUser(
                    target.getUserId(), target.getName());
        }
        users.replace(replacement);
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
        User user = requiredLoggedIn();
        if(!user.isAdmin()){
            throw new AuthorizationException("Admin privilege is required");
        }
        return user;
    }

    private User requireSuperAdmin(){
        User user = requiredLoggedIn();
        if(!user.canChangeRoles()){
            throw new AuthorizationException("SuperAdmin privilege is required");
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
        rooms.require(roomId);
        checkBookingConflict(roomId, date, startTime, endTime, null);
        
        Booking booking = new Booking(idGenerator.nextBookingId(),currentUserId,roomId,date,startTime,endTime,purpose,false);
        bookings.add(booking);
        return booking;
    }

    public Booking modifyBooking(String bookingId, String roomId,
                          LocalDate date, LocalTime startTime, LocalTime endTime,
                          String purpose){
        requiredLoggedIn();
        if(!Objects.equals(currentUserId, bookings.require(bookingId).getOwnerUserId())){
            throw new AuthorizationException("Regular user cannot modify");
        }

        rooms.require(roomId);

        checkBookingConflict(roomId, date, startTime, endTime, bookingId);

        Booking booking = new Booking(bookingId,currentUserId,roomId,date,startTime,endTime,purpose,false);
        bookings.replace(booking);;
        return booking;
    }

    public void deleteBooking(String bookingId){
        requiredLoggedIn();
        if(!Objects.equals(currentUserId, bookings.require(bookingId).getOwnerUserId())){
            throw new AuthorizationException("Regular user cannot delete booking");
        }

        bookings.remove(bookingId);
    }

    private void checkBookingConflict(String roomId, LocalDate date, LocalTime startTime, LocalTime endTime, String idToExclude){
        for(Booking existing : bookings.findByRoom(roomId)){
            if(!existing.isCancelled()){
                if(!existing.getBookingId().equals(idToExclude)){
                    if (existing.getDate().equals(date) && existing.overlaps(startTime, endTime)) {

                        throw new BookingConflictException("The room is already booked during this time.");
                    }
                }
            }
        }
    }
    public List<Booking> getCurrentUserBookings() {
        User currentUser = requiredLoggedIn();

        List<Booking> result =
                bookings.findByOwner(currentUser.getUserId());

        result.sort(
                Comparator.comparing(Booking::getDate)
                        .thenComparing(Booking::getStartTime)
                        .thenComparing(Booking::getBookingId)
        );

        return result;
    }

}
