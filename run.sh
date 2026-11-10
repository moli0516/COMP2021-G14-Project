#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "$0")"

if ! command -v javac >/dev/null 2>&1 ||
   ! command -v java >/dev/null 2>&1; then
    echo "Error: Install JDK 25 and ensure java and javac are on PATH." >&2
    exit 1
fi

BUILD_DIR=$(mktemp -d "${TMPDIR:-/tmp}/crobs-run.XXXXXX")
trap 'rm -rf -- "$BUILD_DIR"' EXIT

find src -name '*.java' -print > "$BUILD_DIR/sources.txt"

if [[ ! -s "$BUILD_DIR/sources.txt" ]]; then
    echo "Error: No Java source files found in src." >&2
    exit 1
fi

javac -encoding UTF-8 \
    -d "$BUILD_DIR/classes" \
    @"$BUILD_DIR/sources.txt"

java -cp "$BUILD_DIR/classes" \
    hk.edu.polyu.comp.comp2021.crobs.Application \
    "$@"