package hk.edu.polyu.comp.comp2021.crobs;

import hk.edu.polyu.comp.comp2021.crobs.controller.CommandParser;
import hk.edu.polyu.comp.comp2021.crobs.controller.CommandProcessor;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.web.WebCommandResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

/** Regression tests for the shared CLI/Web command-history path. */
public class UndoRedoIntegrationTest {
    private CROBS model;
    private CommandProcessor processor;
    private String date;

    @BeforeEach
    void setUp() {
        model = new CROBS();
        processor = new CommandProcessor(new CommandParser(model));
        date = LocalDate.now().plusDays(7).toString();
        ok("login SA001");
    }

    private String ok(String command) {
        String output = processor.process(command);
        assertFalse(output.startsWith("Error:"), command + " -> " + output);
        return output;
    }

    private String error(String command) {
        String output = processor.process(command);
        assertTrue(output.startsWith("Error:"), command + " -> " + output);
        return output;
    }

    private String id(String text, String prefix) {
        Matcher match = Pattern.compile("\\b" + prefix + "\\d+\\b").matcher(text);
        assertTrue(match.find(), text);
        return match.group();
    }

    private String room() { return id(ok("addRoom meetingRoom 10 -"), "R"); }
    private String book(String room) { return id(ok("book " + room + " " + date + " 10:00 11:00 demo"), "B"); }

    @Test void addRoomRedoKeepsIdAndDoesNotRewindGenerator() {
        String first = room();
        ok("undo"); error("listRoom " + first);
        ok("redo"); assertTrue(ok("listRoom " + first).contains(first));
        String second = room(); assertNotEquals(first, second);
        assertEquals(Long.parseLong(first.substring(1)) + 1, Long.parseLong(second.substring(1)));
    }

    @Test void modifiesRoomBothWays() {
        String room = room();
        ok("modifyRoom " + room + " capacity 30");
        assertEquals(30, model.getRoom(room).getCapacity());
        ok("undo"); assertEquals(10, model.getRoom(room).getCapacity());
        ok("redo"); assertEquals(30, model.getRoom(room).getCapacity());
    }

    @Test void bookingCreationModificationDeletionAreReversible() {
        String room = room(), booking = book(room);
        ok("undo"); assertTrue(model.getCurrentUserBookings().isEmpty());
        ok("redo"); assertEquals(booking, model.getCurrentUserBookings().get(0).getBookingId());
        ok("modifyBooking " + booking + " " + room + " " + date + " 12:00 13:00 changed");
        ok("undo"); assertEquals("demo", model.getCurrentUserBookings().get(0).getPurpose());
        ok("redo"); assertEquals("changed", model.getCurrentUserBookings().get(0).getPurpose());
        ok("deleteBooking " + booking); assertTrue(model.getCurrentUserBookings().isEmpty());
        ok("undo"); assertEquals(booking, model.getCurrentUserBookings().get(0).getBookingId());
        ok("redo"); assertTrue(model.getCurrentUserBookings().isEmpty());
    }

    @Test void roomDeletionRestoresBookingAndOriginalRoomId() {
        String room = room(); book(room);
        ok("deleteRoom " + room); assertTrue(model.getCurrentUserBookings().get(0).isCancelled());
        ok("undo"); assertEquals(room, model.getRoom(room).getRoomId());
        assertFalse(model.getCurrentUserBookings().get(0).isCancelled());
        ok("redo"); assertTrue(model.getCurrentUserBookings().get(0).isCancelled());
    }

    @Test void addUserAndRoleChangesRestoreOriginalIdentity() {
        String user = id(ok("addUser Ada Example"), "U");
        ok("undo"); error("changeRole " + user + " ADMIN");
        ok("redo"); ok("changeRole " + user + " ADMIN");
        ok("undo"); ok("removeUser " + user);
        ok("undo"); ok("changeRole " + user + " ADMIN");
        ok("undo"); ok("redo");
        error("removeUser " + user);
    }

    @Test void removeUserRestoresDependentBookings() {
        String room = room(), user = id(ok("addUser Owner"), "U");
        ok("logout"); ok("login " + user);
        String booking = book(room);
        ok("logout"); ok("login SA001");
        ok("removeUser " + user);
        assertFalse(ok("schedule " + room + " " + date).contains(booking));
        ok("undo"); assertTrue(ok("schedule " + room + " " + date).contains(booking));
        ok("redo"); assertFalse(ok("schedule " + room + " " + date).contains(booking));
        ok("undo"); ok("logout"); ok("login " + user);
        assertEquals(booking, model.getCurrentUserBookings().get(0).getBookingId());
    }

    @Test void queriesAndFailuresPreserveRedoButNewMutationClearsIt() {
        String room = room(); ok("undo");
        ok("listAllRooms"); error("modifyRoom missing capacity 0"); error("undo extra");
        assertTrue(model.canRedo()); ok("redo");
        ok("undo"); room(); assertFalse(model.canRedo()); error("redo");
        assertFalse(ok("listAllRooms").contains(room));
    }

    @Test void emptyHistoryAndSessionBoundaries() {
        error("undo"); error("redo"); room();
        error("login SA001"); assertTrue(model.canUndo());
        ok("logout"); error("undo"); assertFalse(model.canUndo());
        ok("login SA001"); error("undo"); assertFalse(model.canRedo());
    }

    @Test void historyLimitIsOneHundred() {
        for (int i = 0; i < 101; i++) ok("addUser User " + i);
        for (int i = 0; i < 100; i++) ok("undo");
        error("undo");
        for (int i = 0; i < 100; i++) ok("redo");
        error("redo");
    }

    @Test void directModelEditsCannotBeSilentlyOverwritten() {
        String room = room();
        model.modifyRoom(room, "capacity", "99");
        error("undo"); assertEquals(99, model.getRoom(room).getCapacity());
        assertTrue(model.canUndo());
    }

    @Test void jsonEscapesControlCharactersQuotesAndSurrogates() {
        String output = "Quote \" slash \\ newline\n tab\t control\u0001 Unicode 中文 \uD83D\uDE00";
        String json = WebCommandResult.toJson("listAllRooms", output, model);
        assertTrue(json.contains("\\\"")); assertTrue(json.contains("\\\\"));
        assertTrue(json.contains("\\n")); assertTrue(json.contains("\\t"));
        assertTrue(json.contains("\\u0001")); assertTrue(json.contains("\\ud83d\\ude00"));
        assertFalse(json.contains("\n"));
        assertTrue(json.contains("\"userId\":\"SA001\""));
        assertTrue(WebCommandResult.toJson("undo", "Error: Nothing to undo", model).contains("\"ok\":false"));
    }

    @Test void nullAndBlankCommandsAreReportedAsErrors() {
        error(""); error("   "); error(null);
    }

    @Test void malformedFeaturesDoNotBreakTheCommandResponse() {
        String room = room(); ok("undo");
        error("addRoom meetingRoom 10 123");
        assertTrue(model.canRedo()); ok("redo");
        assertEquals(room, model.getRoom(room).getRoomId());
    }
}
