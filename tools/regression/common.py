"""Shared build and assertion helpers for external regression tests."""
import argparse
import datetime
import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MAIN = "hk.edu.polyu.comp.comp2021.crobs."

def options(description):
    parser = argparse.ArgumentParser(description=description)
    parser.add_argument("--java", default="java")
    parser.add_argument("--javac", default="javac")
    parser.add_argument("--headed", action="store_true", help="Browser runner only")
    return parser.parse_args()

def prepare(args, name):
    stamp = datetime.datetime.now().strftime("%Y%m%d-%H%M%S-%f")
    work = ROOT / "regression-results" / (name + "-" + stamp)
    classes = work / "classes"
    classes.mkdir(parents=True)
    sources = sorted((ROOT / "src").rglob("*.java"))
    if not sources:
        raise RuntimeError("No Java sources found under src/")
    argfile = work / "sources.txt"
    argfile.write_text("\n".join('"' + p.as_posix() + '"' for p in sources), encoding="utf-8")
    result = subprocess.run(
        [args.javac, "-encoding", "UTF-8", "--release", "8", "-d", str(classes),
         "@" + str(argfile)], cwd=ROOT, capture_output=True, text=True,
        encoding="utf-8", errors="replace", timeout=120)
    (work / "compile.stdout.txt").write_text(result.stdout, encoding="utf-8")
    (work / "compile.stderr.txt").write_text(result.stderr, encoding="utf-8")
    if result.returncode:
        raise RuntimeError("Compilation failed; inspect " + str(work))
    return work, classes

def check(condition, message):
    if not condition:
        raise AssertionError(message)

def entity(output, prefix):
    match = re.search(r"\b" + prefix + r"\d+\b", output)
    check(match is not None, "Missing " + prefix + " ID in: " + output)
    return match.group()

def logs(work, expected=None):
    txt = (work / "commands.txt").read_text(encoding="utf-8")
    html = (work / "commands.html").read_text(encoding="utf-8")
    if expected is not None:
        check(txt.splitlines() == expected, "TXT command log mismatch")
    check("</html>" in html.lower(), "HTML log was not closed")
    return txt, html

def report(work, passed, error=None):
    data = {"passed": passed, "error": error}
    (work / "result.json").write_text(json.dumps(data, indent=2), encoding="utf-8")
    print(("PASS" if passed else "FAIL") + ": " + str(work))
    if error:
        print(error)
