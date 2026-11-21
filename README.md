# CROBS — Campus Room Booking System

COMP2021 Object-Oriented Programming group project. CROBS provides a command-line interface and a local browser interface over the same room-booking model, authorization rules, and command-processing pipeline.

Application data and undo/redo history are held in memory. Separate launches have separate data, and all data is discarded when the application exits.

## Requirements

- JDK 25, with `java` and `javac` available on `PATH`.
- IntelliJ IDEA project language level set to 8, as required by the project brief.
- Bash on macOS/Linux, or Command Prompt/PowerShell on Windows.
- A browser for Web mode. No npm installation or third-party Web framework is required.

The Web adapter uses the JDK-specific `jdk.httpserver` module. Confirm its acceptance under the course library restrictions before submission. The launch scripts compile with the installed JDK; they do not automatically enforce the JDK version or Java 8 source compatibility.

## Quick start

Run these commands from the repository root. The launch scripts also locate the repository when invoked from another directory.

| Mode | macOS / Linux | Windows PowerShell |
| --- | --- | --- |
| CLI (default) | `bash run.sh` | `.\run.bat` |
| CLI (explicit) | `bash run.sh cli` | `.\run.bat cli` |
| Web UI | `bash run.sh web` | `.\run.bat web` |
| Help | `bash run.sh --help` | `.\run.bat --help` |

In Command Prompt, use `run.bat` instead of `.\run.bat`. On macOS/Linux, optionally run `chmod +x run.sh`, then use `./run.sh`.

The scripts compile production sources into a fresh temporary directory before launch. Compilation failure stops startup. The temporary build is cleaned up after normal exit. These launchers do not compile or run JUnit tests.

### CLI example

```bash
bash run.sh cli
```

At the `CROBS>>` prompt:

```text
login SA001
addRoom meetingRoom 10 projector,whiteboard
listAllRooms
quit
```

No user is logged in at startup. The initial superadministrator ID is `SA001`. Other IDs are generated automatically; use the IDs returned by the application in later operations.

### Web UI

```bash
bash run.sh web
```

Open `http://127.0.0.1:8080`, then log in as `SA001`.

- Use forms for rooms, bookings, searches, schedules, users, and roles.
- Command results are rendered as structured HTML tables or message blocks.
- The browser receives a JSON envelope containing textual command output, operation status, current user information, and undo/redo availability.
- Result tables and room/booking dropdowns still parse the textual output field. The JSON response does not provide structured room or booking collections.
- Room options come from `listAllRooms`; personal booking options come from `listBookings`.
- The user dropdown contains only users known to the current page. Choose **Enter ID manually…** for other accounts.
- Automatic dropdown-refresh queries also appear in command logs.
- Undo and Redo buttons synchronize with backend history availability. State-divergence checks can still reject restoration.
- All browser tabs share one login session and application state. Use one tab; this is not a multi-user Web service.
- The server binds to local IPv4 loopback only. Do not expose it through a proxy or tunnel.
- Use **Quit application** to stop the server and close the HTML/TXT logs.
- Restart the server and reload the page after editing Web assets; the server loads them at startup.

## Command logging and launch arguments

Default log files:

| Mode | HTML | TXT |
| --- | --- | --- |
| CLI | `logs/cli.html` | `logs/cli.txt` |
| Web | `logs/web.html` | `logs/web.txt` |

To choose both filenames with one argument, supply a prefix without an extension:

```bash
bash run.sh web logs/demo
bash run.sh cli "logs/manual test"
```

```powershell
.\run.bat web logs\demo
.\run.bat cli "logs\manual test"
```

For example, `logs/demo` produces `logs/demo.html` and `logs/demo.txt`. Parent directories are created automatically. Relative paths are resolved from the repository root, not the caller's directory.

The launch scripts also accept the original flags, in either order:

```bash
bash run.sh -html logs/session.html -txt logs/session.txt
bash run.sh web -html logs/demo.html -txt logs/demo.txt
```

```powershell
.\run.bat web -html logs\demo.html -txt logs\demo.txt
```

HTML and TXT logs must identify different files. Logs contain submitted commands rather than application responses, including commands that subsequently fail. Web option-refresh queries are logged as well.

The launchers do not implement log append, rotation, or backups. Use a different prefix to preserve previous session logs.

## CLI commands

| Command | Arguments |
| --- | --- |
| `login` | `userID` |
| `logout` | — |
| `addUser` | `name` |
| `removeUser` | `targetUserID` |
| `changeRole` | `targetUserID REGULAR\|ADMIN` |
| `addRoom` | `roomType capacity features` |
| `modifyRoom` | `roomID field newValue` |
| `deleteRoom` | `roomID` |
| `listRoom` | `roomID` |
| `listAllRooms` | — |
| `book` | `roomID date startTime endTime purpose` |
| `modifyBooking` | `bookingID roomID date startTime endTime purpose` |
| `deleteBooking` | `bookingID` |
| `listBookings` | — |
| `search` | `date startTime endTime minCapacity features` |
| `schedule` | `roomID weekStartDate` |
| `undo` | — |
| `redo` | — |
| `quit` | — |

### Input formats

- Room types: `lectureTheatre`, `computerLab`, `seminarRoom`, `meetingRoom`.
- Fields accepted by `modifyRoom`: `roomType`, `capacity`, `features`.
- Dates: `yyyy-MM-dd`.
- Times: `HH:mm`.
- Features: comma-separated, with no spaces; use `-` for an empty feature set.
- User names and booking purposes may contain spaces.
- Room capacity must be positive; search minimum capacity may be zero.

### Booking rules

- Booking creation and modification require the proposed start date-time to be strictly in the future according to the server's local clock. Later times on the current date are permitted.
- The end time must be strictly later than the start time.
- Active overlapping bookings for the same room on the same date are rejected.
- Start times are inclusive and end times are exclusive, so adjacent bookings are permitted.
- Only the booking owner may modify or delete a booking. Administrator status does not bypass ownership.
- Removing a regular user deletes that user's booking records.
- Deleting a room cancels its active bookings dated today or later and removes the room. The affected booking records are retained.

### Permissions

- Regular users manage their own bookings and query rooms, availability, and schedules.
- Administrators additionally manage rooms and regular-user accounts.
- Only the superadministrator changes user roles.
- Role changes preserve the target user's ID and name.
- The Java model is the authority for validation and permissions; visible administration forms do not grant privileges.

## Undo and redo

Undo/redo is available through CLI commands and Web toolbar buttons.

- Supported successful data-changing commands executed through the shared command pipeline record before-and-after snapshots of users, rooms, and bookings.
- Undo restores the before snapshot; redo restores the after snapshot, including related records affected by the command.
- Restored entities retain their original IDs. ID counters are not rewound.
- Queries and failed commands do not create history entries or discard available redo operations.
- A successful new recorded data command clears redo history.
- History is limited to 100 undo entries and is cleared by successful login, logout, and quit commands.
- Restoration checks that the current data matches the expected history state. Direct model changes outside the recorded command flow can cause restoration to be rejected.
- Snapshot restoration does not reapply the future-start validation used for booking creation and modification.
- History is in memory only and does not persist after application exit.

## Architecture

- `CROBS` coordinates domain operations, authorization, booking ownership, conflict checks, and the shared session.
- Repositories store users, rooms, and bookings by ID.
- `CommandParser` creates commands and wraps execution for shared history recording.
- `CommandProcessor` executes commands through the common `Command` interface.
- CLI and Web entry points reuse this pipeline and handle command logging outside the model.
- The Web adapter adds JSON status/session metadata while retaining existing textual command output.

## Testing and inspection

### JUnit, coverage, and course inspection

In IntelliJ IDEA:

1. Configure SDK 25 and project language level 8.
2. Mark `src` as Sources and `test` as Test Sources.
3. Attach the team's JUnit Jupiter libraries with Test scope.
4. Run the complete model test suite and measure model line coverage.
5. Run the prescribed course inspection profile.

The launch scripts do not run these checks. Coverage and inspection results must be measured separately.

### External regression runners

The regression tooling is under `tools/regression/`. It requires Python 3.9+, Git, and the course JDK.

Run the CLI checks from the repository root:

```bash
python tools/regression/cli_regression.py
```

For browser checks, preferably use a Python virtual environment:

```bash
python -m pip install -r tools/regression/requirements-browser.txt
python -m playwright install chromium
python tools/regression/browser_smoke.py --headed
```

Use `python3` if that is your Python 3 executable. Omit `--headed` for headless browser execution. Stop any existing server on port 8080 and close other CROBS tabs before running the browser tests.

Results are written to timestamped directories under `regression-results/`. Review result summaries, compile output, command logs, and browser traces/screenshots as applicable. These runners do not replace JUnit, coverage measurement, or course inspection.

### Final verification checklist

- Compile production sources and run the complete model test suite.
- Check CLI and browser workflows, authorization, and invalid inputs.
- Check future-start validation for booking creation and modification.
- Check overlaps, adjacent intervals, ownership, and failed-update safety.
- Check search filters, weekly schedule boundaries, sorting, and owner-ID visibility.
- Check that user removal deletes the user's booking records.
- Check that room deletion cancels its active bookings dated today or later.
- Check undo/redo restoration, history rules, and Web button/selector refresh behavior.
- Check HTML escaping, command order, and graceful log closure.
- Run the course inspection profile.

Passing results apply only to the tested code version. Rerun affected checks after relevant changes, retain the corresponding evidence, and record the tested commit when preparing the final report. This README does not assert a new test run or a current coverage percentage.

## Known limitations

- No persistent application storage or persistent undo/redo history.
- One shared local browser session rather than independent concurrent users.
- Web table rendering and room/booking selectors depend on textual output formatting.
- User selectors contain only users known to the current browser page.
- Regression checks are targeted checks, not exhaustive verification of every requirement or execution path.

## Troubleshooting

| Problem | Action |
| --- | --- |
| `java` or `javac` not found | Install/configure JDK 25, check `PATH`, and run `java -version` and `javac -version`. |
| Main class not found | Use the launcher so production sources are compiled and the classpath is supplied. |
| Web assets missing | Check that `webui/` assets are present and launch using the repository scripts. |
| Port 8080 already in use | Quit the earlier CROBS server or stop the process holding the port. Port selection is not configurable in the current adapter. |
| Booking time rejected | Check the server's local date/time. The proposed start must be strictly in the future and the end must follow the start. |
| Dropdown empty or parser warning | Log in, refresh options, and inspect the command result. Manual ID entry remains available; formatter changes may require parser updates. |
| Browser changes do not appear | Restart the Java server and reload the browser; a hard refresh may also be needed. |
| Undo/redo rejected after direct model edits | Reset history by logging out and logging in again. Changes outside recorded command execution may invalidate snapshots. |
| Request fails after submission | Check the server and resulting state before retrying; the operation may already have completed. |
| Log I/O failure | Check the log path, permissions, and filesystem. Stop the run and investigate the server/log output. |

## Repository layout

```text
src/                Java model, controllers, logging, CLI and Web entry points
webui/              HTML, CSS, and browser JavaScript
test/               JUnit tests
lib/                Project test dependencies
docs/               Project documentation
tools/regression/   External CLI and browser regression tooling
run.sh              macOS/Linux launcher
run.bat             Windows launcher
```

This README is a setup and usage guide. It does not replace the project report, user manual, presentation, or required GenAI acknowledgement.
