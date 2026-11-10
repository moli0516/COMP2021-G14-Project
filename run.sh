#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
usage() {
    echo "Usage: $0 [cli|web] [log-prefix]"
    echo "       $0 [cli|web] -html file -txt file"
    echo "Defaults: cli mode; logs/cli.html + logs/cli.txt (web: logs/web.*)"
}
MODE=cli
case "${1:-}" in
    -h|--help|help) usage; exit 0 ;;
    cli|web) MODE=$1; shift ;;
esac
HTML=""; TXT=""
if [[ $# -eq 0 ]]; then
    HTML="logs/$MODE.html"; TXT="logs/$MODE.txt"
elif [[ $# -eq 1 && $1 != -* ]]; then
    HTML="$1.html"; TXT="$1.txt"
else
    while [[ $# -gt 0 ]]; do
        [[ $# -ge 2 && -n $2 ]] || { usage >&2; exit 2; }
        case "$1" in
            -html) [[ -z $HTML ]] || { usage >&2; exit 2; }; HTML=$2 ;;
            -txt) [[ -z $TXT ]] || { usage >&2; exit 2; }; TXT=$2 ;;
            *) usage >&2; exit 2 ;;
        esac
        shift 2
    done
    [[ -n $HTML && -n $TXT ]] || { usage >&2; exit 2; }
fi
if ! command -v javac >/dev/null 2>&1 || ! command -v java >/dev/null 2>&1; then
    echo "Error: Install JDK 25 and put java and javac on PATH." >&2; exit 1
fi
MAIN=hk.edu.polyu.comp.comp2021.crobs.Application
if [[ $MODE == web ]]; then
    MAIN=hk.edu.polyu.comp.comp2021.crobs.web.WebApplication
    [[ -f src/hk/edu/polyu/comp/comp2021/crobs/web/WebApplication.java && -f webui/index.html && -f webui/app.js && -f webui/style.css ]] || {
        echo "Error: Web UI files are missing. Apply the Web UI patch first." >&2; exit 1;
    }
fi
mkdir -p -- "$(dirname -- "$HTML")" "$(dirname -- "$TXT")"
BUILD_DIR=$(mktemp -d "${TMPDIR:-/tmp}/crobs-run.XXXXXX")
trap 'rm -rf -- "$BUILD_DIR"' EXIT
mkdir -p "$BUILD_DIR/classes"
find src -name '*.java' -print0 | while IFS= read -r -d '' source; do
    source=${source//\\/\\\\}
    source=${source//\"/\\\"}
    printf '"%s"\n' "$source"
done > "$BUILD_DIR/sources.txt"
[[ -s "$BUILD_DIR/sources.txt" ]] || { echo "Error: No Java sources found." >&2; exit 1; }
javac -encoding UTF-8 -d "$BUILD_DIR/classes" @"$BUILD_DIR/sources.txt"
printf 'Mode: %s\nHTML log: %s\nTXT log: %s\n' "$MODE" "$HTML" "$TXT"
if [[ $MODE == web ]]; then
    java --add-modules jdk.httpserver -cp "$BUILD_DIR/classes" "$MAIN" -html "$HTML" -txt "$TXT"
else
    java -cp "$BUILD_DIR/classes" "$MAIN" -html "$HTML" -txt "$TXT"
fi
