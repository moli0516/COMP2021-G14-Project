package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.BookingConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
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

    public void deleteRoom(String roomId) {
        requiredAdmin();
        rooms.require(roomId);

        LocalDate today = LocalDate.now();
        for (Booking booking : bookings.findByRoom(roomId)) {
            if (!booking.isCancelled()
                    && !booking.getDate().isBefore(today)) {
                bookings.replace(booking.cancel());
            }
        }
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
    
    public Room modifyRoom(String roomId, RoomType type, int capacity,
                        Set<String> features) {
        requiredAdmin();

        Room existing = rooms.require(roomId);
        Room updated = existing.withDetails(
                type, capacity, new FeatureSet(features));
        rooms.replace(updated);
        return updated;
    }

    public Room getRoom(String roomId) {
        requiredLoggedIn();
        return rooms.require(roomId);
    }

    public List<Room> getAllRooms() {
        requiredLoggedIn();

        List<Room> result = rooms.findAll();
        result.sort(Comparator.comparing(Room::getRoomId));
        return result;
    }

    public List<Room> searchRooms(RoomType type, int minCapacity,
                                Set<String> requiredFeatures,
                                LocalDate date, LocalTime startTime,
                                LocalTime endTime) {
        requiredLoggedIn();

        if (minCapacity < 0) {
            throw new ValidationException("Minimum capacity must not be negative");
        }
        if (date == null) {
            throw new ValidationException("Date must not be null");
        }
        if (startTime == null || endTime == null|| !startTime.isBefore(endTime)) {
            throw new ValidationException("Start time must be before end time");
        }

        FeatureSet required = new FeatureSet(
                requiredFeatures == null ? Collections.<String>emptySet() : requiredFeatures);

        List<Room> result = new ArrayList<>();
        for (Room room : rooms.findAll()) {
            if (type != null && room.getRoomType() != type) {
                continue;
            }
            if (room.getCapacity() < minCapacity) {
                continue;
            }

            if (!room.getFeatures().asSet().containsAll(required.asSet())) {
                continue;
            }

            boolean available = true;
            for (Booking booking : bookings.findByRoom(room.getRoomId())) {
                if (!booking.isCancelled() && booking.getDate().equals(date) && booking.overlaps(startTime, endTime)) {
                    available = false;
                    break;
                }
            }

            if (available) {
                result.add(room);
            }
        }

        result.sort(Comparator.comparing(Room::getRoomId));
        return result;
    }

    public List<Booking> getWeeklySchedule(LocalDate weekStart) {
        User user = requiredLoggedIn();

        if (weekStart == null) {
            throw new ValidationException("Week start date must not be null");
        }

        LocalDate weekEnd = weekStart.plusDays(7);
        List<Booking> result = new ArrayList<>();

        for (Booking booking : bookings.findAll()) {
            boolean inWeek = !booking.getDate().isBefore(weekStart)
                    && booking.getDate().isBefore(weekEnd);

            // Regular users see their own bookings; admins see all bookings.
            boolean visibleToUser = user.isAdmin()
                    || Objects.equals(user.getUserId(), booking.getOwnerUserId());

            if (inWeek && visibleToUser && !booking.isCancelled()) {
                result.add(booking);
            }
        }

        result.sort(Comparator.comparing(Booking::getDate)
                .thenComparing(Booking::getStartTime)
                .thenComparing(Booking::getBookingId));

        return result;
    }
}

