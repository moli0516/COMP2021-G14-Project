# CROBS external regression tests

Run from the repository root. Requires Python 3.9+, Git, and the course JDK (25).
No production Java files are changed. These scripts compile src/ with --release 8.
They do not execute or replace your JUnit suite, coverage run, or inspections.

## CLI

    python tools/regression/cli_regression.py

## Browser setup (prefer a virtual environment)

    python -m pip install -r tools/regression/requirements-browser.txt
    python -m playwright install chromium
    python tools/regression/browser_smoke.py --headed

Use python3 instead of python if that is your Python 3 executable.
Stop any existing server on port 8080 first. Close other CROBS browser tabs.
The browser runner starts and stops its own fresh WebApplication on 127.0.0.1:8080.
Use no --headed flag for headless execution.

Both runners accept --java and --javac executable paths, for example:

    python tools/regression/cli_regression.py --java "/path/to/jdk-25/bin/java" --javac "/path/to/jdk-25/bin/javac"

## Results

Each invocation creates a new regression-results/<runner>-<timestamp>/ directory.
PASS exits 0; FAIL exits 1. Setup/compile failures also exit nonzero.
Inspect result.json, command logs, stdout/stderr and compile logs on failure.
Browser runs additionally save trace.zip, page-errors.txt and screenshots;
failed browser workflows attempt to save failure.html and failure.png.
Open a trace with:

    python -m playwright show-trace regression-results/<run>/trace.zip

The CLI checks permissions, dynamic IDs, room modification/failure safety,
booking conflicts and adjacency, booking failure safety and ordering, search
filters, schedule boundaries/privacy, user removal, room deletion/cancellation,
malformed input, HTML escaping, exact TXT logging, quit, EOF, invalid startup.
The browser checks real forms, login, empty-room refresh, room/user creation,
booking picker refresh, conflicts, modification, search, schedule, deletion,
logout rejection, JavaScript errors and clean quit.

This is targeted regression coverage, not a complete REQ1-REQ18 acceptance test.
Regular-user privacy is checked by CLI; not every scenario is duplicated in browser.
There is no structured-results-table requirement in these tests.
The CLI prompt/output strings and browser selectors match the inspected repository
source; local UI/output changes may require updating the assertions.

Do not submit regression-results or your Python virtual environment accidentally.
No .gitignore is modified by this add-only patch. Delete generated results after
review or exclude them locally with .git/info/exclude.

The patch was syntax-checked and checked with git apply in a clean temporary
repository. Java/Chromium execution must be performed on your own machine.
