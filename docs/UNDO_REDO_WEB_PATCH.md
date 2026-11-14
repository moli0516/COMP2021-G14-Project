# CROBS undo/redo + styled Web results — candidate patch

## Status and safety

This is a reviewable candidate patch. It does not modify GitHub, create a branch,
or commit any change. Apply only in a clean working tree or a disposable branch.
Production Java compilation, the JUnit suite, real browser smoke tests, course
inspection and coverage have NOT been run in the authoring environment, which
has no Java/Javac installation. Previously reported project test results do not
apply automatically after this patch.

The patch was prepared from the GitHub file contents retrieved for this request.
Full-repository `git apply --check` against a checkout has NOT been performed.
Run that command locally before applying. Patch format/numstat parsing and the
34 dependency-free JavaScript mock-DOM checks did pass during authoring.

## Apply and verify

From the repository root, with the downloaded patch available locally:

```sh
git status --short
git switch -c experiment/undo-redo-web
git apply --check /your/local/crobs-undo-redo-web.patch
git apply /your/local/crobs-undo-redo-web.patch
node --check webui/app.js
node test/webui/command-ui.test.cjs
```

If `git apply --check` fails, stop and inspect the differences; do not use
`--reject` or force application on a submission branch. CROBS.java originally
uses CRLF; its patch hunk preserves that convention. Local newline conversion
or edits may require regeneration of the patch.

Then run the full JUnit suite in IntelliJ, including the 13 new tests in
UndoRedoIntegrationTest, and run the course inspection and model coverage again.
Run the existing CLI regression suite. Finally launch `bash run.sh web` (or the
Windows launcher), restart Java and reload the browser assets before testing.
The Node mock-DOM tests are not a substitute for browser or HTTP integration tests.

For an uncommitted clean application, reversal can be checked with:

```sh
git apply --reverse --check /your/local/crobs-undo-redo-web.patch
```

## Design: memento-backed reversible history, not textual reverse commands

This candidate deliberately uses private before/after repository snapshots.
It does NOT implement an independent undo method in every existing mutation
command and does NOT rerun a textual inverse CLI command. The public undo/redo
commands restore the private before/after state of a history entry. This is the
Command + Memento variant discussed as the safer baseline for cascading deletes.

All commands returned by CommandParser are wrapped in a recording boundary.
The nine recorded data verbs are addUser, removeUser, changeRole, addRoom,
modifyRoom, deleteRoom, book, modifyBooking and deleteBooking. CLI and Web both
use that parser/processor path. Calling CROBS mutation methods directly from
other Java code is outside the recording boundary. Undo/redo compares the
current repositories with its expected state and refuses to overwrite
out-of-band edits. Logout/login resets stale history.

* First-time creation still uses IdGenerator.
* Undo/redo restores the original objects and IDs, without generating new IDs.
* Counters never rewind, including after failed operations; ID gaps are allowed.
* Room deletion snapshots include all cancellation effects. User deletion
  snapshots include the deleted user's bookings. Original cancellation flags
  are restored, not blindly activated.
* Query commands neither create history entries nor clear redo.
* Failed data commands roll back repository state and preserve both stacks.
  Identifier counters are intentionally not rolled back.
* New successful data changes clear redo. Even a successful same-value mutation
  is treated as a history step in this version.
* A successful login, logout or quit clears both stacks. Failed login/logout
  does not clear them. Login/logout themselves are not undoable.
* History is global to the one shared application session, not per browser tab.
* At most 100 entries are retained. Full snapshots trade simplicity for memory
  and runtime cost: O(number of users + rooms + bookings) per snapshot.
* Snapshot collections copy maps but share the existing immutable entity
  objects. If entities become mutable, introduce deep copies or immutable DTOs.
* The Web adapter already serializes commands under its existing lock. This
  patch does not add a multi-user service or make arbitrary direct model access
  thread-safe. History operations should be used through the shared command path.
* Restoring an entry replays the original state, not validation against a new
  wall-clock date. This avoids recomputing deleteRoom cancellation effects on redo.
* Package-private repository restore helpers accept only private model snapshots
  in this design; no public arbitrary-ID restore CLI/API endpoint is introduced.

## Web transport and styling — all 19 commands

The existing `/api/command` endpoint continues accepting a plain-text command.
Clients requesting exactly `Accept: application/json` receive a JSON envelope:

```json
{"command":"undo","ok":true,"output":"Undone: addRoom","canUndo":false,"canRedo":true,"userId":"SA001","userRole":"SUPERADMIN"}
```

The userRole value above is illustrative; actual values come from UserRole.name().
Clients not requesting JSON retain the previous plain-text response. The server
keeps its existing loopback binding, request-token validation, host check, CSP,
command logging, serialization and quit handling. The CLI OutputFormatter is
unchanged.

All 17 previous commands plus undo and redo have styled success/error cards.
Room outputs and booking outputs become tables when their existing text format
matches; weekly schedules become tables without exposing hidden owner IDs.
Matching login/addUser/changeRole user summaries become user tables. Deletions,
logout, quit, undo/redo, empty lists and other summaries are styled message cards.
Every result also has an expandable, lossless raw-output view. This is NOT a
fully typed domain-result API: table columns still parse the existing text
formatter. If output formatting changes, unrecognized responses fall back to
styled text instead of silently losing rows or inventing data. A name or purpose
containing HTML is rendered as text, never as innerHTML.

The header exposes undo/redo buttons whose enabled state comes from the JSON
response. A successful operation refreshes room and personal booking dropdowns;
refresh queries remain logged and do not replace the main displayed result.
The user picker still contains only page-known users, not a full user directory.
After undo/redo it is reset to avoid retaining deleted or obsolete user options;
use manual ID entry for restored users not listed on the page. Existing one-tab
operation guidance still applies. Browser reload recovery and independent
browser sessions are not added by this patch.

## Manual browser acceptance checklist

1. Login as SA001; create a room; undo removes it and disables redo/undo as
   appropriate; redo restores the same room ID and refreshes dropdowns.
2. Modify room capacity; undo and redo alternate between old/new values.
3. Create, modify and delete bookings; undo/redo preserve booking IDs and details.
4. Delete a room with a future booking; undo restores the room and booking status;
   redo re-applies exactly the original cancellation effects.
5. Remove a regular user with bookings; undo restores both; redo removes both.
6. Change a regular user's role; undo and redo restore the correct role.
7. Execute queries after undo: redo remains available. Submit a failed mutation:
   redo remains available. Submit a valid new mutation: redo is cleared.
8. Logout/login: history is cleared; another login cannot undo the previous user.
9. Check styled results for all 19 verbs, no-results cases, invalid arguments,
   authorization failures and unknown IDs. Confirm raw output remains available.
10. Verify schedule owner visibility as regular user and admin. Test a purpose
    containing pipes, angle brackets, quotes, Chinese text and emoji; no injected
    markup should execute and JSON decoding must succeed.
11. Stop with quit; buttons are disabled. Restart and reload before continuing.

## Retrieved base file blob IDs

These are Git blob SHAs, not a claim that HEAD was pinned or a full checkout tested.
Compare with `git hash-object <file>` when checking whether a local base differs.

- model/CROBS.java: 11bad173a960f13487e8dd259e8bccbc863c1238
- model/UserRepository.java: bfdfb09d297c166896086707b52ec7510f320000
- model/RoomRepository.java: 292d2091f7114f49e4138437730267b97da2331e
- model/BookingRepository.java: 927d43506931a81e188925305eff01d1fc62e2cd
- controller/CommandParser.java: 6354d67360fc529c9b2c3c5153745bbbdfc5a94e
- controller/CommandProcessor.java: 016fe8b9086b29d8f2d9061060b9a7a85f3a4053
- web/WebApplication.java: c3d7bdfcfe8e9c67cd6d8a68f0e96b286337432c
- webui/app.js: ae1b94e74db5557287c2f9c36b40851e855978e8
- webui/index.html: 77cfae12463b879273f1190470899ed7d9654cc2
- webui/style.css: 2ec724282fe22b2e0420dbf3225a514564e0b88a

Java paths above are relative to src/hk/edu/polyu/comp/comp2021/crobs/.
