# CROBS — Campus Room Booking System

Java course project with a command-line interface and an optional local browser interface. Both interfaces use the same room-booking model and authorization rules. Separate launches have separate in-memory data.

## Requirements

- JDK 25; `java` and `javac` must be available on PATH.
- IntelliJ IDEA project language level: 8, as required by the project brief.
- Bash on macOS/Linux, or Command Prompt/PowerShell on Windows.
- A browser for Web mode. No npm install or third-party web framework is required.

The Web adapter uses the JDK-specific `jdk.httpserver` module. Confirm its acceptance under course library restrictions before submission. Scripts compile with the installed JDK; they do not enforce JDK version or Java 8 syntax automatically.

## Quick start

Run these commands from the repository root. Scripts also locate the repository when invoked from another directory.

| Mode           | macOS / Linux        | Windows PowerShell |
|----------------|----------------------|--------------------|
| CLI (default)  | `bash run.sh`        | `.\run.bat`        |
| CLI (explicit) | `bash run.sh cli`    | `.\run.bat cli`    |
| Web UI         | `bash run.sh web`    | `.\run.bat web`    |
| Help           | `bash run.sh --help` | `.\run.bat --help` |

In Command Prompt use `run.bat` instead of `.\run.bat`. On macOS/Linux, optionally run `chmod +x run.sh`, then use `./run.sh`.

Scripts compile production sources into a fresh temporary directory before launch. JUnit tests are not compiled or run by these launchers. Compilation failure stops startup. The temporary build is cleaned up after normal exit.

### Web UI

```sh
bash run.sh web
```

Open **http://127.0.0.1:8080**, then log in as `SA001`.

- Use forms for rooms, bookings, searches, schedules, users and roles.
- ID dropdowns parse existing command output, not a separate JSON API.
- Room options come from `listAllRooms`; personal booking options from `listBookings`.
- The user dropdown contains only page-local known users; choose **Enter ID manually…** for other accounts.
- Dropdown refresh queries also appear in command logs.
- All browser tabs share ONE login and application state. Use one tab; this is not a multi-user web service.
- The server binds to local IPv4 loopback only. Do not expose it through a proxy or tunnel.
- Use **Quit application** to stop the server and close the HTML/TXT logs.
- Restart the server and reload the page after editing web assets: the current server loads them at startup.

### CLI

```sh
bash run.sh cli
```

At `CROBS>>`, try:

```text
login SA001
addRoom meetingRoom 10 projector,whiteboard
listAllRooms
quit
```

IDs are generated automatically; use the actual returned IDs in later operations. No user is logged in at startup. All data is discarded on application exit.

## Simple log arguments

Default files:

| Mode | HTML            | TXT            |
|------|-----------------|----------------|
| CLI  | `logs/cli.html` | `logs/cli.txt` |
| Web  | `logs/web.html` | `logs/web.txt` |

To choose both filenames with one argument, supply a prefix without an extension:

```sh
bash run.sh web logs/demo
bash run.sh cli "logs/manual test"
```

```powershell
.\run.bat web logs\demo
.\run.bat cli "logs\manual test"
```

`logs/demo` becomes `logs/demo.html` and `logs/demo.txt`. Parent directories are created automatically. Relative paths are resolved from the repository root, not the caller's directory.

The original flags remain supported, in either order:

```sh
bash run.sh -html logs/session.html -txt logs/session.txt
bash run.sh web -html logs/demo.html -txt logs/demo.txt
```

```powershell
.\run.bat web -html logs\demo.html -txt logs\demo.txt
```

Reusing a filename follows the existing logger behavior; these launchers do not implement append, rotation or backups. Use a different prefix to preserve previous sessions. HTML and TXT must identify different files. Logs contain commands rather than application responses. Web automatic option queries are recorded too.

## CLI commands

| Command         | Arguments                                         |
|-----------------|---------------------------------------------------|
| `login`         | `userID`                                          |
| `logout`        | —                                                 |
| `addUser`       | `name` (spaces allowed)                           |
| `removeUser`    | `targetUserID`                                    |
| `changeRole`    | `targetUserID REGULAR\|ADMIN`                     |
| `addRoom`       | `roomType capacity features`                      |
| `modifyRoom`    | `roomID field newValue`                           |
| `deleteRoom`    | `roomID`                                          |
| `listRoom`      | `roomID`                                          |
| `listAllRooms`  | —                                                 |
| `book`          | `roomID date startTime endTime purpose`           |
| `modifyBooking` | `bookingID roomID date startTime endTime purpose` |
| `deleteBooking` | `bookingID`                                       |
| `listBookings`  | —                                                 |
| `search`        | `date startTime endTime minCapacity features`     |
| `schedule`      | `roomID weekStartDate`                            |
| `quit`          | —                                                 |

Room types: `lectureTheatre`, `computerLab`, `seminarRoom`, `meetingRoom`.
Room fields in the integrated UI: `roomType`, `capacity`, `features`.
Dates: `yyyy-MM-dd`. Times: `HH:mm`. Features: comma-separated with no spaces; use `-` for none. Purpose and user names can contain spaces.

Regular users manage their own bookings and query rooms/schedules. Admin users additionally manage rooms and regular users. Only the superadmin changes roles. Admin status does not permit modifying another user's bookings. The Java model remains the authority for validation and permissions; visible administration forms do not grant privileges.

## Testing and project inspection

In IntelliJ, configure SDK 25, mark `src` as Sources and `test` as Test Sources, and attach the team's JUnit Jupiter libraries with Test scope. Run the complete model test suite and measure coverage. The scripts above launch the application only; no claim is made that they run tests.

Before merging changes:

- Compile production sources; run all model tests.
- Verify CLI and browser workflows, authorization and invalid input.
- Verify booking overlaps, adjacent times, ownership and sorting.
- Verify weekly room schedules and owner-ID visibility.
- Verify room/user deletion and dependent booking cancellation.
- Verify HTML escaping, command order and graceful log closure.
- Run the course IntelliJ inspection profile.

## Troubleshooting

- **`java` / `javac` not found:** install JDK 25 and fix PATH; check `java -version` and `javac -version`.
- **Main class not found:** use the launcher, which compiles and supplies the classpath. Do not invoke the packaged class without its build directory.
- **Web files missing:** apply/commit the Web UI files, including `WebApplication.java` and `webui` assets.
- **Port 8080 in use:** quit the earlier CROBS server, or stop the process holding that port. Port selection is not configurable in the current adapter.
- **Log I/O timeout:** retry with a local prefix such as `/tmp/crobs-demo` on macOS/Linux; inspect the original path/filesystem separately.
- **Dropdown empty or parser warning:** log in, refresh options and inspect command results. Manual ID entry remains available. Formatter changes may require parser updates.
- **Browser changes do not appear:** restart Java and reload the browser; it may also require a hard refresh.

## Repository layout

```text
src/       Java model, controllers, logging, CLI and Web entry points
webui/     HTML, CSS and browser JavaScript
test/     JUnit tests (directory name: test)
lib/       Project test dependencies
docs/     Project documentation (directory name: docs)
run.sh     macOS/Linux launcher
run.bat    Windows launcher
```

Undo/redo, persistent storage and independent browser sessions are not implemented by these launchers or the Web UI patch. Complete the project report, manual, presentation and required GenAI acknowledgement separately; this README is a quick-start guide, not a replacement for required deliverables.
