package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.BookingConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Coordinates in-memory users, rooms, bookings, and one shared login session.
 */
public class CROBS {

    private static final int HISTORY_LIMIT = 100;
    private final java.util.Deque<HistoryEntry> undoHistory = new java.util.ArrayDeque<>();
    private final java.util.Deque<HistoryEntry> redoHistory = new java.util.ArrayDeque<>();
    private boolean recording;

    /**
     * Executes an interface command and records successful data mutations.
     * History covers commands parsed by CommandParser, not arbitrary direct model calls.
     * ID counters are intentionally excluded from snapshots and never rewound.
     *
     * @param name exact command verb
     * @param action authorized command action
     * @return original CLI-compatible output
     * @throws AuthorizationException if the action is not authorized
     * @throws ValidationException if execution is nested or the action is invalid
     * @throws RuntimeException if the action fails; repository data is rolled
     *         back but consumed ID numbers are not rewound
     */
    public synchronized String executeRecorded(String name,
            java.util.function.Supplier<String> action) {
        if (recording) throw new ValidationException("Nested command execution is not supported");
        if (!isDataCommand(name)) {
            String output = action.get();
            if ("login".equals(name) || "logout".equals(name) || "quit".equals(name)) {
                undoHistory.clear();
                redoHistory.clear();
            }
            return output;
        }
        User actor = requiredLoggedIn();
        State before = snapshot();
        recording = true;
        try {
            String output = action.get();
            State after = snapshot();
            undoHistory.push(new HistoryEntry(name, actor.getUserId(), before, after));
            while (undoHistory.size() > HISTORY_LIMIT) undoHistory.removeLast();
            redoHistory.clear();
            return output;
        } catch (RuntimeException exception) {
            restore(before);
            throw exception;
        } finally {
            recording = false;
        }
    }

    /**
     * Restores the before snapshot of the latest applied data command.
     * Original entity IDs are retained and the clock is not rewound.
     *
     * @return a receipt naming the undone command
     * @throws AuthorizationException if login or entry ownership is invalid
     * @throws ValidationException if history is empty, recording is active,
     *         or direct model edits invalidate the expected snapshot
     */
    public synchronized String undo() {
        return moveHistory(undoHistory, redoHistory, true);
    }

    /**
     * Restores the after snapshot of the latest undone data command.
     * Original entity IDs are retained and the clock is not rewound.
     *
     * @return a receipt naming the redone command
     * @throws AuthorizationException if login or entry ownership is invalid
     * @throws ValidationException if history is empty, recording is active,
     *         or direct model edits invalidate the expected snapshot
     */
    public synchronized String redo() {
        return moveHistory(redoHistory, undoHistory, false);
    }

    /**
     * Reports whether a logged-in session has a nonempty undo stack.
     * Direct edits can still prevent execution of the recorded operation.
     *
     * @return whether a logged-in session has a undo entry
     */
    public synchronized boolean canUndo() {
        return getCurrentUser() != null && !undoHistory.isEmpty();
    }

    /**
     * Reports whether a logged-in session has a nonempty redo stack.
     * Direct edits can still prevent execution of the recorded operation.
     *
     * @return whether a logged-in session has a redo entry
     */
    public synchronized boolean canRedo() {
        return getCurrentUser() != null && !redoHistory.isEmpty();
    }

    /**
     * Validates and transfers a history entry after restoring its snapshot.
     *
     * @param source the source history stack
     * @param destination the destination history stack
     * @param backwards true for undo, false for redo
     * @return a receipt naming the restored command
     * @throws AuthorizationException if login or actor ownership is invalid
     * @throws ValidationException if recording, empty history, or state divergence prevents restoration
     */
    private String moveHistory(java.util.Deque<HistoryEntry> source,
            java.util.Deque<HistoryEntry> destination, boolean backwards) {
        User actor = requiredLoggedIn();
        if (recording) throw new ValidationException("Cannot change history during a command");
        if (source.isEmpty()) throw new ValidationException(backwards ? "Nothing to undo" : "Nothing to redo");
        HistoryEntry entry = source.peek();
        if (!actor.getUserId().equals(entry.actorId)) {
            throw new AuthorizationException("History belongs to a different login session");
        }
        State expected = backwards ? entry.after : entry.before;
        if (!snapshot().sameAs(expected)) {
            throw new ValidationException("Data changed outside command history; logout and login to reset history");
        }
        restore(backwards ? entry.before : entry.after);
        source.pop();
        destination.push(entry);
        return (backwards ? "Undone: " : "Redone: ") + entry.name;
    }

    /**
     * Identifies command verbs whose successful mutations are recorded.
     *
     * @param name the non-null, case-sensitive command verb
     * @return true if execution should create a history entry
     */
    private static boolean isDataCommand(String name) {
        switch (name) {
            case "addUser": case "removeUser": case "changeRole":
            case "addRoom": case "modifyRoom": case "deleteRoom":
            case "book": case "modifyBooking": case "deleteBooking":
                return true;
            default: return false;
        }
    }

    /**
     * Captures repository mappings, excluding session, clock, and ID counters.
     *
     * @return an independent mapping snapshot containing immutable entity references
     */
    private State snapshot() {
        return new State(users.findAll(), rooms.findAll(), bookings.findAll());
    }

    /**
     * Restores a trusted snapshot without applying new-reservation time checks.
     *
     * @param state the non-null private model snapshot
     */
    private void restore(State state) {
        // Snapshots are private, generated by this model, and contain immutable entities.
        users.restoreEntries(state.users);
        rooms.restoreEntries(state.rooms);
        bookings.restoreEntries(state.bookings);
    }

    /**
     * Private snapshot of repository mappings containing immutable entities.
     */
    private static final class State {
        private final java.util.Map<String, User> users = new java.util.HashMap<>();
        private final java.util.Map<String, Room> rooms = new java.util.HashMap<>();
        private final java.util.Map<String, Booking> bookings = new java.util.HashMap<>();

        /**
         * Copies entity lists into independent ID mappings.
         *
         * @param userList the users to capture
         * @param roomList the rooms to capture
         * @param bookingList the bookings to capture
         */
        private State(List<User> userList, List<Room> roomList, List<Booking> bookingList) {
            for (User value : userList) users.put(value.getUserId(), value);
            for (Room value : roomList) rooms.put(value.getRoomId(), value);
            for (Booking value : bookingList) bookings.put(value.getBookingId(), value);
        }

        /**
         * Compares all repository mappings with another private snapshot.
         *
         * @param other the non-null snapshot to compare
         * @return true if the repository mappings match
         */
        private boolean sameAs(State other) {
            return users.equals(other.users) && rooms.equals(other.rooms)
                    && bookings.equals(other.bookings);
        }
    }

    /**
     * One actor-owned command and its before/after repository snapshots.
     */
    private static final class HistoryEntry {
        private final String name;
        private final String actorId;
        private final State before;
        private final State after;

        /**
         * Creates an entry for a successful data mutation.
         *
         * @param name the command verb
         * @param actorId the actor user ID
         * @param before the state before execution
         * @param after the state after execution
         */
        private HistoryEntry(String name, String actorId, State before, State after) {
            this.name = name;
            this.actorId = actorId;
            this.before = before;
            this.after = after;
        }
    }

    private final UserRepository users;
    private final BookingRepository bookings;
    private final RoomRepository rooms;
    private final IdGenerator idGenerator;
    private final Clock clock;

    private String currentUserId;

    /**
     * Creates an empty system using the server's default-zone clock.
     * The initial superadmin is {@code SA001}; no user is logged in.
     */
    public CROBS(){
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates an empty system with a supplied clock for reservation validation
     * and date-based room deletion. History does not capture or rewind the clock.
     *
     * @param clock the non-null clock defining the system's local time zone
     * @throws NullPointerException if clock is null
     */
    public CROBS(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.users = new UserRepository();
        this.bookings = new BookingRepository();
        this.rooms = new RoomRepository();
        this.idGenerator = new IdGenerator();

        this.currentUserId = "";
        User superAdmin = new SuperAdmin("SA001", "superadmin");
        users.add(superAdmin);
    }

    /**
     * Resolves the current session user; repository lookup exceptions are treated as no user.
     *
     * @return the logged-in user, or {@code null} if resolution fails
     */
    public User getCurrentUser(){
        try {
            return users.require(currentUserId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Logs in an existing user when no user is currently logged in.
     *
     * @param userId the nonblank ID of an existing user
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws ValidationException if the supplied domain values are invalid
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException if a referenced entity does not exist
     */
    public void login(String userId){
        if (!Objects.equals(currentUserId, "")) {
            throw new AuthorizationException("Please logout before login");
        } else {
            if(userId == null || userId.trim().isEmpty()) throw new ValidationException("The userId is blanked");
        }

        User user = users.require(userId);
        currentUserId = user.getUserId();
    }

    /**
     * Creates a regular user; requires an administrator.
     *
     * @param name the nonblank display name
     * @return the newly stored user
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws ValidationException if the supplied domain values are invalid
     */
    public User addUser(String name){
        requiredAdmin();

        String userId = idGenerator.nextUserId();
        User user = new RegularUser(userId, name);
        users.add(user);
        return user;
    }

    /**
     * Removes a regular user and all their bookings; requires an administrator.
     *
     * @param targetUserId the regular user to remove
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws ValidationException if the supplied domain values are invalid
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException if a referenced entity does not exist
     */
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

    /**
     * Replaces a user with the requested role while retaining ID and name; requires superadmin.
     *
     * @param userId the user whose role is to change
     * @param newRole a different role, either REGULAR or ADMIN
     * @throws AuthorizationException if the session is not superadmin or the target is superadmin
     * @throws ValidationException if the supplied domain values are invalid
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException if a referenced entity does not exist
     */
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

    /**
     * Clears the current login session.
     *
     * @throws AuthorizationException if no user is logged in
     */
    public void logout(){
        requiredLoggedIn();

        currentUserId = "";
    }

    /**
     * Requires a logged-in user.
     *
     * @return the authorized session user
     * @throws AuthorizationException if the current session lacks the required privileges
     */
    private User requiredLoggedIn(){
        User user = getCurrentUser();
        if(user == null){
            throw new AuthorizationException("Please login first");
        }
        return user;
    }

    /**
     * Requires a logged-in administrator.
     *
     * @throws AuthorizationException if the current session lacks the required privileges
     */
    private void requiredAdmin(){
        User user = requiredLoggedIn();
        if(!user.isAdmin()){
            throw new AuthorizationException("Admin privilege is required");
        }
    }

    /**
     * Requires a logged-in user with role-changing privileges.
     *
     * @throws AuthorizationException if the current session lacks the required privileges
     */
    private void requireSuperAdmin(){
        User user = requiredLoggedIn();
        if(!user.canChangeRoles()){
            throw new AuthorizationException("SuperAdmin privilege is required");
        }
    }

    /**
     * Creates and stores a room; requires an administrator.
     *
     * @param type the non-null room category
     * @param capacity the positive capacity
     * @param features the non-null set of feature names
     * @return the new room
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws ValidationException if the supplied domain values are invalid
     * @throws IllegalArgumentException if the feature set is invalid
     */
    public Room addRoom(RoomType type, int capacity, Set<String> features){
        requiredAdmin();

        Room room = new Room(idGenerator.nextRoomId(), type, capacity, new FeatureSet(features));
        rooms.add(room);
        return room;
    }

    /**
     * Removes a room and cancels its active bookings dated today or later; requires admin.
     *
     * @param roomId the room to remove
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException if a referenced entity does not exist
     */
    public void deleteRoom(String roomId) {
        requiredAdmin();
        rooms.require(roomId);

        LocalDate today = LocalDate.now(clock);
        for (Booking booking : bookings.findByRoom(roomId)) {
            if (!booking.isCancelled()
                    && !booking.getDate().isBefore(today)) {
                bookings.replace(booking.cancel());
            }
        }
        rooms.remove(roomId);
    }

    /**
     * Creates an active booking owned by the current user; requires login.
     * The proposed start must be strictly after now in the system clock's zone.
     * Time validation occurs before booking ID allocation or repository mutation.
     *
     * @param roomId the existing room to book
     * @param date the non-null booking date
     * @param startTime the inclusive start time
     * @param endTime the exclusive end time, after the start
     * @param purpose the nonblank booking purpose
     * @return the stored booking
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException if a referenced entity does not exist
     * @throws ValidationException if the supplied domain values are invalid
     * @throws BookingConflictException if an active booking overlaps in the same room on the same date
     */
    public Booking createBooking(String roomId, LocalDate date,
                                 LocalTime startTime, LocalTime endTime, String purpose){
        requiredLoggedIn();
        rooms.require(roomId);
        validateBookingTime(date, startTime, endTime);
        checkBookingConflict(roomId, date, startTime, endTime, null);
        
        Booking booking = new Booking(idGenerator.nextBookingId(),currentUserId,roomId,date,startTime,endTime,purpose,false);
        bookings.add(booking);
        return booking;
    }

    /**
     * Replaces a booking with validated active details; only its logged-in owner may modify it.
     * The proposed start must be strictly after now in the system clock's zone.
     * Time validation occurs before booking ID allocation or repository mutation.
     *
     * @param bookingId the existing booking to replace
     * @param roomId the existing room to book
     * @param date the non-null booking date
     * @param startTime the inclusive start time
     * @param endTime the exclusive end time, after the start
     * @param purpose the nonblank booking purpose
     * @return the replacement booking
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException if a referenced entity does not exist
     * @throws ValidationException if the supplied domain values are invalid
     * @throws BookingConflictException if another active booking conflicts with the new interval
     */
    public Booking modifyBooking(String bookingId, String roomId,
                          LocalDate date, LocalTime startTime, LocalTime endTime,
                          String purpose){
        requiredLoggedIn();
        if(!Objects.equals(currentUserId, bookings.require(bookingId).getOwnerUserId())){
            throw new AuthorizationException("Regular user cannot modify");
        }

        rooms.require(roomId);

        validateBookingTime(date, startTime, endTime);
        checkBookingConflict(roomId, date, startTime, endTime, bookingId);

        Booking booking = new Booking(bookingId,currentUserId,roomId,date,startTime,endTime,purpose,false);
        bookings.replace(booking);
        return booking;
    }

    /**
     * Permanently removes a booking; only its logged-in owner may delete it.
     *
     * @param bookingId the booking to remove
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException if a referenced entity does not exist
     */
    public void deleteBooking(String bookingId){
        requiredLoggedIn();
        if(!Objects.equals(currentUserId, bookings.require(bookingId).getOwnerUserId())){
            throw new AuthorizationException("Regular user cannot delete booking");
        }

        bookings.remove(bookingId);
    }

    /**
     * Validates proposed reservation times using one reading of the system clock.
     * This creation/modification policy is not applied to historical Booking
     * objects, queries, or trusted undo/redo snapshot restoration.
     *
     * @param date the proposed date in the system clock's local time zone
     * @param startTime the inclusive start, which must be strictly after now
     * @param endTime the exclusive end, which must be strictly after the start
     * @throws ValidationException if any value is null, the interval does not
     *         increase, the date is in the past, or the start is not in the future
     */
    private void validateBookingTime(LocalDate date, LocalTime startTime,
                                     LocalTime endTime) {
        if (date == null) {
            throw new ValidationException("Booking date must not be null");
        }
        if (startTime == null || endTime == null) {
            throw new ValidationException("Start time and end time must not be null");
        }
        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("Start time must be before end time");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (date.isBefore(now.toLocalDate())) {
            throw new ValidationException("Booking date must not be in the past");
        }
        if (!LocalDateTime.of(date, startTime).isAfter(now)) {
            throw new ValidationException("Booking start time must be in the future");
        }
    }

    /**
     * Checks active bookings for a same-room, same-date time conflict.
     *
     * @param roomId the room to inspect
     * @param date the date to match
     * @param startTime the proposed start time
     * @param endTime the proposed end time
     * @param idToExclude the booking to ignore, or null
     * @throws BookingConflictException if a non-excluded active booking overlaps
     * @throws ValidationException if a matching booking detects an invalid proposed interval
     */
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
    /**
     * Returns only the current user's bookings, including past and cancelled bookings.
     *
     * @return a new list sorted by date, start time, and booking ID; possibly empty
     * @throws AuthorizationException if the current session lacks the required privileges
     */
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
    
    /**
     * Replaces room details without changing existing bookings; requires an administrator.
     *
     * @param roomId the existing room ID
     * @param type the new room category
     * @param capacity the new positive capacity
     * @param features the new non-null feature names
     * @return the updated room
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException if a referenced entity does not exist
     * @throws ValidationException if the supplied domain values are invalid
     * @throws IllegalArgumentException if the feature set is invalid
     */
    public Room modifyRoom(String roomId, RoomType type, int capacity,
                        Set<String> features) {
        requiredAdmin();

        Room existing = rooms.require(roomId);
        Room updated = existing.withDetails(
                type, capacity, new FeatureSet(features));
        rooms.replace(updated);
        return updated;
    }

    /**
     * Changes one room field while preserving the other details; requires an administrator.
     *
     * @param roomId the existing room ID
     * @param field roomType, capacity, or features
     * @param newValue the nonblank replacement expressed in command syntax
     * @return the updated room
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException if a referenced entity does not exist
     * @throws ValidationException if the supplied domain values are invalid
     */
    public Room modifyRoom(String roomId, String field, String newValue) {
        requiredAdmin();

        Room existing = rooms.require(roomId);

        if (field == null || field.trim().isEmpty()) {
            throw new ValidationException("Room field must not be blank");
        }
        if (newValue == null || newValue.trim().isEmpty()) {
            throw new ValidationException("New value must not be blank");
        }

        RoomType type = existing.getRoomType();
        int capacity = existing.getCapacity();
        Set<String> features = existing.getFeatures().asSet();

        switch (field.trim()) {
            case "roomType":
                type = RoomType.fromCommand(newValue.trim());
                break;

            case "capacity":
                try {
                    capacity = Integer.parseInt(newValue.trim());
                } catch (NumberFormatException e) {
                    throw new ValidationException(
                            "Capacity must be a valid integer");
                }
                if (capacity <= 0) {
                    throw new ValidationException("Capacity must be positive");
                }
                break;

            case "features":
                try {
                    features = FeatureSet.parse(newValue.trim()).asSet();
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Invalid room features");
                }
                break;

            default:
                throw new ValidationException(
                        "Unknown room field. Use roomType, capacity or features");
        }

        return modifyRoom(roomId, type, capacity, features);
    }

    /**
     * Returns a room by ID; requires login.
     *
     * @param roomId the room identifier
     * @return the stored room
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException if a referenced entity does not exist
     */
    public Room getRoom(String roomId) {
        requiredLoggedIn();
        return rooms.require(roomId);
    }

    /**
     * Lists all rooms; requires login.
     *
     * @return a new list sorted by room ID, possibly empty
     * @throws AuthorizationException if the current session lacks the required privileges
     */
    public List<Room> getAllRooms() {
        requiredLoggedIn();

        List<Room> result = rooms.findAll();
        result.sort(Comparator.comparing(Room::getRoomId));
        return result;
    }

    /**
     * Finds rooms meeting all filters without overlapping active bookings; requires login.
     *
     * @param type the required category, or null for any category
     * @param minCapacity the nonnegative minimum capacity
     * @param requiredFeatures required feature names, or null for no feature restriction
     * @param date the availability date
     * @param startTime the inclusive start time
     * @param endTime the exclusive end time, after the start
     * @return a new list of available rooms sorted by ID, possibly empty
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws ValidationException if the supplied domain values are invalid
     * @throws IllegalArgumentException if the feature names are invalid
     */
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
                requiredFeatures == null ? Collections.emptySet() : requiredFeatures);

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

    /**
     * Searches available rooms of any category; requires login.
     *
     * @param date the availability date
     * @param startTime the inclusive start time
     * @param endTime the exclusive end time, after the start
     * @param minCapacity the nonnegative minimum capacity
     * @param features required features, or null for none
     * @return available rooms sorted by room ID, possibly empty
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws ValidationException if the supplied domain values are invalid
     * @throws IllegalArgumentException if the feature names are invalid
     */
    public List<Room> searchRooms(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            int minCapacity,
            Set<String> features) {

        return searchRooms(
                null,
                minCapacity,
                features,
                date,
                startTime,
                endTime);
    }

    /**
     * Lists active bookings in the seven-day window starting at the supplied date; requires login.
     *
     * @param roomId the existing room ID
     * @param weekStartDate the inclusive first date; need not be a Monday
     * @return a new list sorted by date, start time, and booking ID; owner IDs are not redacted here
     * @throws AuthorizationException if the current session lacks the required privileges
     * @throws hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException if a referenced entity does not exist
     * @throws ValidationException if the supplied domain values are invalid
     * @throws java.time.DateTimeException if computing the end of the window exceeds the supported date range
     */
    public List<Booking> getWeeklySchedule(
            String roomId,
            LocalDate weekStartDate) {

        requiredLoggedIn();
        rooms.require(roomId);

        if (weekStartDate == null) {
            throw new ValidationException("Week start date must not be null");
        }

        LocalDate weekEndExclusive = weekStartDate.plusDays(7);
        List<Booking> result = new ArrayList<>();

        for (Booking booking : bookings.findByRoom(roomId)) {
            boolean inWeek = !booking.getDate().isBefore(weekStartDate) && booking.getDate().isBefore(weekEndExclusive);

            if (inWeek && !booking.isCancelled()) {
                result.add(booking);
            }
        }

        result.sort(Comparator.comparing(Booking::getDate).thenComparing(Booking::getStartTime).thenComparing(Booking::getBookingId));

        return result;
    }
}

