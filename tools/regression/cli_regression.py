"""Compile and regression-test the real CLI; no third-party dependencies."""
import datetime
import queue
import subprocess
import threading
import traceback
from common import ROOT, MAIN, options, prepare, check, entity, logs, report

class CLI:
    def __init__(self, args, work, classes):
        self.work, self.sent = work, []
        self.events = queue.Queue()
        self.raw = []
        self.stderr = (work / "stderr.txt").open("w", encoding="utf-8")
        self.proc = subprocess.Popen(
            [args.java, "-cp", str(classes), MAIN + "Application",
             "-html", str(work / "commands.html"), "-txt", str(work / "commands.txt")],
            cwd=ROOT, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
            stderr=self.stderr, text=True, encoding="utf-8", errors="replace", bufsize=1)
        threading.Thread(target=self._read, daemon=True).start()
        self._response()

    def _read(self):
        while True:
            char = self.proc.stdout.read(1)
            if not char:
                self.events.put(None)
                return
            self.raw.append(char)
            self.events.put(char)

    def _response(self):
        import time
        deadline = time.monotonic() + 15
        value = ""
        while True:
            char = self.events.get(timeout=max(0.01, deadline-time.monotonic()))
            if char is None:
                raise RuntimeError("CLI exited unexpectedly: " + value)
            value += char
            if value.endswith("CROBS>> "):
                return value[:-len("CROBS>> ")].strip()
            if time.monotonic() >= deadline:
                raise TimeoutError("CLI response timed out")

    def command(self, line, error=False):
        self.sent.append(line)
        self.proc.stdin.write(line + "\n")
        self.proc.stdin.flush()
        output = self._response()
        check(output.startswith("Error:") == error,
              "Unexpected result for " + line + ": " + output)
        return output

    def finish(self, quit_command=True):
        if quit_command:
            self.sent.append("quit")
            self.proc.stdin.write("quit\n")
            self.proc.stdin.flush()
        self.proc.stdin.close()
        check(self.proc.wait(timeout=15) == 0, "CLI exit status was nonzero")
        self.cleanup()
        logs(self.work, self.sent)

    def cleanup(self):
        if self.proc.poll() is None:
            self.proc.kill()
            self.proc.wait(timeout=5)
        self.stderr.close()
        (self.work / "stdout.txt").write_text("".join(self.raw), encoding="utf-8")

def workflow(c):
    day = datetime.date.today() + datetime.timedelta(days=14)
    d = day.isoformat()
    c.command("listAllRooms", error=True)
    c.command("login UNKNOWN", error=True)
    c.command("login SA001")
    c.command("login SA001", error=True)
    r = entity(c.command("addRoom meetingRoom 10 projector"), "R")
    u = entity(c.command("addUser Alice Regression"), "U")
    other = entity(c.command("addUser Bob Regression"), "U")
    c.command(f"modifyRoom {r} capacity 20")
    check("capacity: 20" in c.command(f"listRoom {r}"), "Room modification not persisted")
    c.command(f"modifyRoom {r} capacity abc", error=True)
    check("capacity: 20" in c.command(f"listRoom {r}"), "Rejected update changed room")
    c.command("logout")
    c.command("login " + u)
    c.command("addRoom meetingRoom 10 -", error=True)
    c.command("addUser Forbidden", error=True)
    c.command(f"modifyRoom {r} capacity 30", error=True)
    b = entity(c.command(f"book {r} {d} 09:00 10:00 Original meeting"), "B")
    c.command(f"book {r} {d} 09:30 10:30 Conflict", error=True)
    adjacent = entity(c.command(f"book {r} {d} 10:00 11:00 Adjacent meeting"), "B")
    c.command(f"modifyBooking {b} {r} {d} 10:30 11:30 Rejected change", error=True)
    listing = c.command("listBookings")
    original = next(line for line in listing.splitlines() if line.startswith(b + " "))
    check("09:00-10:00 Original meeting" in original, "Failed modification changed original")
    check(listing.index(b) < listing.index(adjacent), "Bookings not time sorted")
    check(r not in c.command(f"search {d} 09:30 09:45 1 projector"), "Occupied room found")
    check(r in c.command(f"search {d} 11:00 12:00 20 projector"), "Available room not found")
    check(r not in c.command(f"search {d} 11:00 12:00 21 projector"), "Capacity filter failed")
    check(r not in c.command(f"search {d} 11:00 12:00 1 whiteboard"), "Feature filter failed")
    sixth = (day + datetime.timedelta(days=6)).isoformat()
    seventh = (day + datetime.timedelta(days=7)).isoformat()
    included = entity(c.command(f"book {r} {sixth} 09:00 10:00 Last included day"), "B")
    excluded = entity(c.command(f"book {r} {seventh} 09:00 10:00 Outside week"), "B")
    schedule = c.command(f"schedule {r} {d}")
    check(b in schedule and included in schedule and excluded not in schedule, "Seven-day boundaries failed")
    check("booked by " + u in schedule, "Owner cannot see own ID")
    c.command("logout")
    c.command("login " + other)
    c.command("deleteBooking " + b, error=True)
    c.command(f"modifyBooking {b} {r} {d} 12:00 13:00 Forbidden", error=True)
    schedule = c.command(f"schedule {r} {d}")
    check("booked by hidden" in schedule and u not in schedule, "Regular-user privacy failed")
    c.command("logout")
    c.command("login SA001")
    check("booked by " + u in c.command(f"schedule {r} {d}"), "Admin owner visibility failed")
    c.command("deleteBooking " + b, error=True)
    c.command("removeUser " + u)
    check("No bookings this week." in c.command(f"schedule {r} {d}"), "Removing user retained bookings")
    c.command("logout")
    c.command("login " + u, error=True)
    c.command("login SA001")
    c.command(f"book {r} {d} 09:00 10:00 Removed room booking")
    c.command("deleteRoom " + r)
    c.command("listRoom " + r, error=True)
    check("CANCELLED" in c.command("listBookings"), "Room deletion did not cancel booking")
    c.command("book", error=True)
    c.command("search invalid-date 09:00 10:00 1 -", error=True)
    c.command("notACommand <tag>&", error=True)


def undo_redo_workflow(c):
    """Exercise history through parsed CLI commands and observable model state."""
    def move(command, verb):
        expected = ("Undone: " if command == "undo" else "Redone: ") + verb
        check(c.command(command) == expected, "Wrong history receipt: " + expected)

    def empty(command):
        check("Nothing to " + command in c.command(command, error=True),
              "Expected empty " + command + " history")

    def booking_row(booking_id):
        return next((line for line in c.command("listBookings").splitlines()
                     if line.startswith(booking_id + " ")), None)

    def newer(new_id, old_id):
        check(int(new_id[1:]) > int(old_id[1:]), "Undo rewound the ID generator")

    c.command("logout")
    c.command("undo", error=True)
    c.command("redo", error=True)
    c.command("login SA001")
    empty("undo")
    empty("redo")
    d = (datetime.date.today() + datetime.timedelta(days=14)).isoformat()
    r = entity(c.command("addRoom meetingRoom 10 projector"), "R")
    room_before = c.command("listRoom " + r)
    move("undo", "addRoom")
    c.command("listRoom " + r, error=True)
    empty("undo")
    move("redo", "addRoom")
    check(c.command("listRoom " + r) == room_before, "Redo changed the room or its ID")
    empty("redo")

    c.command(f"modifyRoom {r} capacity 20")
    room_after = c.command("listRoom " + r)
    move("undo", "modifyRoom")
    check(c.command("listRoom " + r) == room_before, "Undo did not restore room details")
    c.command("listAllRooms")
    c.command(f"search {d} 09:00 10:00 1 projector")
    c.command(f"modifyRoom {r} capacity abc", error=True)
    c.command("login SA001", error=True)
    move("redo", "modifyRoom")
    check(c.command("listRoom " + r) == room_after, "Query/failure destroyed redo history")

    u = entity(c.command("addUser History User"), "U")
    move("undo", "addUser")
    c.command(f"changeRole {u} ADMIN", error=True)
    move("redo", "addUser")
    role_after = c.command(f"changeRole {u} ADMIN")
    check(u in role_after and "ADMIN" in role_after, "Role change lost the user ID")
    move("undo", "changeRole")
    move("redo", "changeRole")
    check("already has role ADMIN" in c.command(f"changeRole {u} ADMIN", error=True),
          "Redo did not restore admin role")
    move("undo", "changeRole")
    c.command("removeUser " + u)
    empty("redo")
    move("undo", "removeUser")
    c.command(f"changeRole {u} ADMIN")
    move("undo", "changeRole")
    move("undo", "addUser")
    replacement = entity(c.command("addUser History Replacement"), "U")
    newer(replacement, u)
    empty("redo")

    b = entity(c.command(f"book {r} {d} 09:00 10:00 History original"), "B")
    before = booking_row(b)
    check(before is not None and "ACTIVE" in before, "Created booking missing")
    move("undo", "book")
    check(booking_row(b) is None, "Undo book retained booking")
    move("redo", "book")
    check(booking_row(b) == before, "Redo book changed ID/details")
    c.command(f"modifyBooking {b} {r} {d} 11:00 12:00 History modified")
    after = booking_row(b)
    check("11:00-12:00 History modified" in after, "Booking modification missing")
    move("undo", "modifyBooking")
    check(booking_row(b) == before, "Undo modifyBooking did not restore original")
    c.command(f"book {r} {d} 09:30 10:30 Conflict", error=True)
    past = (datetime.date.today() - datetime.timedelta(days=2)).isoformat()
    c.command(f"book {r} {past} 09:00 10:00 Past booking", error=True)
    c.command(f"schedule {r} {d}")
    move("redo", "modifyBooking")
    check(booking_row(b) == after, "Failed booking/query destroyed redo history")
    c.command("deleteBooking " + b)
    check(booking_row(b) is None, "deleteBooking retained booking")
    move("undo", "deleteBooking")
    check(booking_row(b) == after, "Undo deleteBooking changed booking")
    move("redo", "deleteBooking")
    check(booking_row(b) is None, "Redo deleteBooking retained booking")
    move("undo", "deleteBooking")
    move("undo", "modifyBooking")
    move("undo", "book")
    newer_booking = entity(c.command(f"book {r} {d} 09:00 10:00 History branch"), "B")
    newer(newer_booking, b)
    empty("redo")
    active = booking_row(newer_booking)
    c.command("deleteRoom " + r)
    c.command("listRoom " + r, error=True)
    check("CANCELLED" in booking_row(newer_booking), "Room deletion did not cancel booking")
    move("undo", "deleteRoom")
    check(c.command("listRoom " + r) == room_after, "Undo deleteRoom did not restore room")
    check(booking_row(newer_booking) == active, "Undo deleteRoom did not restore active booking")
    move("redo", "deleteRoom")
    c.command("listRoom " + r, error=True)
    check("CANCELLED" in booking_row(newer_booking), "Redo deleteRoom did not cancel booking")
    move("undo", "deleteRoom")

    abandoned = entity(c.command("addRoom meetingRoom 5 -"), "R")
    move("undo", "addRoom")
    replacement_room = entity(c.command("addRoom meetingRoom 6 -"), "R")
    newer(replacement_room, abandoned)
    empty("redo")

    # Cross-session setup: removing a regular user also removes their bookings.
    c.command("logout")
    c.command("login " + replacement)
    empty("undo")
    empty("redo")
    owned = entity(c.command(f"book {r} {d} 13:00 14:00 Removed owner booking"), "B")
    c.command("logout")
    c.command("login SA001")
    schedule_before = c.command(f"schedule {r} {d}")
    c.command("removeUser " + replacement)
    check(owned not in c.command(f"schedule {r} {d}"), "removeUser retained owned booking")
    move("undo", "removeUser")
    check(c.command(f"schedule {r} {d}") == schedule_before,
          "Undo removeUser did not restore booking ID, owner and details")
    move("redo", "removeUser")
    check(owned not in c.command(f"schedule {r} {d}"), "Redo removeUser retained booking")
    move("undo", "removeUser")
    c.command("logout")
    c.command("undo", error=True)
    c.command("redo", error=True)
    c.command("login " + replacement)
    empty("undo")
    empty("redo")
    check(booking_row(owned) is not None, "Undo removeUser did not restore login/ownership")
    c.command("logout")
    c.command("login SA001")

    # Both stacks must be bounded by the documented 100-entry history policy.
    original_room = c.command("listRoom " + r)
    for capacity in range(101, 202):
        c.command(f"modifyRoom {r} capacity {capacity}")
    for _ in range(100):
        move("undo", "modifyRoom")
    empty("undo")
    check("capacity: 101" in c.command("listRoom " + r), "History limit boundary is incorrect")
    for _ in range(100):
        move("redo", "modifyRoom")
    empty("redo")
    check("capacity: 201" in c.command("listRoom " + r), "Redo chain did not restore final state")
    check(c.command("listRoom " + r) != original_room, "History-limit test made no change")
    c.command("logout")
    c.command("login SA001")
    empty("undo")
    empty("redo")

def main():
    args = options(__doc__)
    work, classes = prepare(args, "cli")
    c = None
    try:
        c = CLI(args, work, classes)
        workflow(c)
        undo_redo_workflow(c)
        c.finish()
        _, html = logs(work)
        check("&lt;tag&gt;&amp;" in html and "<tag>" not in html, "HTML escaping failed")
        eof_dir = work / "eof"
        eof_dir.mkdir()
        c = CLI(args, eof_dir, classes)
        c.command("login SA001")
        c.finish(quit_command=False)
        invalid = subprocess.run([args.java, "-cp", str(classes), MAIN + "Application"],
                                 cwd=ROOT, capture_output=True, text=True, timeout=15)
        (work / "invalid-startup.txt").write_text(invalid.stdout + invalid.stderr, encoding="utf-8")
        check(invalid.returncode != 0 and "Usage:" in invalid.stderr, "Invalid startup accepted")
        report(work, True)
        return 0
    except Exception:
        if c is not None:
            c.cleanup()
        report(work, False, traceback.format_exc())
        return 1

if __name__ == "__main__":
    raise SystemExit(main())
