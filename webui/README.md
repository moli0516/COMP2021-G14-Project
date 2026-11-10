# CROBS local Web UI

This adds a separate Java entry point; the existing CLI is unchanged. Requires the JDK-bundled jdk.httpserver module. Confirm course acceptance of this JDK-specific API before submission. No third-party libraries or build tool are required.

## Run (macOS/Linux, from repository root)

```sh
CLASSES=$(mktemp -d /tmp/crobs-web.XXXXXX)
find src -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -d "$CLASSES" && java --add-modules jdk.httpserver -cp "$CLASSES" hk.edu.polyu.comp.comp2021.crobs.web.WebApplication -html /tmp/crobs-web.html -txt /tmp/crobs-web.txt
```

Open http://127.0.0.1:8080. Log in as SA001. This is ONE SHARED APPLICATION SESSION, including across tabs. Not a multi-user web service. The server binds only to IPv4 loopback. Do not expose it through a proxy or tunnel. Use the Quit application button for graceful shutdown and logger closure. Existing log overwrite behavior follows the existing logger implementation.

## API

POST /api/command with UTF-8 text body containing one CLI command. The page includes a random per-run token sent as X-CROBS-Token. Other origins receive no CORS permission. Read-only GET requests serve only three explicit assets. Commands are serialized and use the existing parser/processor and HTML/TXT loggers. HTTP 200 means processing completed; business failures retain the existing `Error:` response convention. HTTP errors represent adapter failures. Responses are text, deliberately not JSON. No automatic retry of mutations.

## Coverage and limitations

Graphical forms cover login/logout, all room operations, all booking operations, search, schedule, user creation/removal, role changes and quit. Administration controls are visible to all; authorization remains enforced in Java. Results use the existing formatter in a text result panel, not structured room tables or a calendar. Session identity is not separately queried. This is a functional integration baseline, not a polished full GUI or a claim of full bonus marks.

## Manual acceptance checklist

- Login, logout and failed login; permissions rejected for anonymous/regular users.
- Add/list/modify/delete rooms; check remaining fields and IDs are unchanged.
- Book/list/modify/delete bookings; test overlaps and adjacency.
- Search and weekly schedule, including ownership visibility.
- Add/remove users and change roles, including dependent booking cancellation.
- HTML/TXT contain each submitted command in order; HTML escapes special characters.
- Restart clears model state; two tabs share one login as documented.
- Confirm quit returns a response, stops Java and closes HTML logs.
- Original CLI and all existing JUnit tests still work.
- Request without token is rejected; an unknown URL is 404.

This patch was generated against inspected source interfaces, but was NOT compiled or executed by the assistant. Run compilation and tests before merging. No remote repository changes are performed by the patch itself.
