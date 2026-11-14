"""Compile and smoke-test real browser forms using Playwright Chromium."""
import datetime
import re
import socket
import subprocess
import time
import traceback
import urllib.request
from common import ROOT, MAIN, options, prepare, check, entity, logs, report

def main():
    args = options(__doc__)
    from playwright.sync_api import sync_playwright, expect
    work, classes = prepare(args, "browser")
    url = "http://127.0.0.1:8080"
    with socket.socket() as probe:
        check(probe.connect_ex(("127.0.0.1", 8080)) != 0,
              "Port 8080 is occupied. Stop your existing CROBS server first.")
    output = (work / "server.stdout.txt").open("w", encoding="utf-8")
    errors = (work / "server.stderr.txt").open("w", encoding="utf-8")
    proc = subprocess.Popen(
        [args.java, "-cp", str(classes), MAIN + "web.WebApplication",
         "-html", str(work / "commands.html"), "-txt", str(work / "commands.txt")],
        cwd=ROOT, stdout=output, stderr=errors)
    failure = None
    try:
        deadline = time.monotonic() + 20
        while True:
            check(proc.poll() is None, "Web server exited; inspect server.stderr.txt")
            try:
                with urllib.request.urlopen(url, timeout=1) as response:
                    check(response.status == 200, "Homepage not available")
                break
            except OSError:
                if time.monotonic() > deadline:
                    raise TimeoutError("Web server did not become ready")
                time.sleep(0.1)
        with sync_playwright() as pw:
            browser = pw.chromium.launch(headless=not args.headed)
            context = browser.new_context()
            context.tracing.start(screenshots=True, snapshots=True, sources=True)
            page = context.new_page()
            page.set_default_timeout(10000)
            js_errors = []
            page.on("pageerror", lambda error: js_errors.append(str(error)))
            page.on("dialog", lambda dialog: dialog.accept())
            try:
                page.goto(url)
                expect(page.locator("#section-title")).to_have_text("Session")

                def section(name):
                    page.locator("#navigation").get_by_role("button", name=name, exact=True).click()

                def form(title):
                    return page.locator("#forms form").filter(
                        has=page.get_by_role("heading", name=title, exact=True))

                def submit(section_name, title, values=None, error=False):
                    section(section_name)
                    target = form(title)
                    for name, value in (values or {}).items():
                        field = target.locator('[name="' + name + '"]')
                        if field.evaluate("el => el.tagName") == "SELECT":
                            field.select_option(value)
                        else:
                            field.fill(value)
                    with page.expect_response(lambda response:
                            response.url.endswith("/api/command") and
                            response.request.method == "POST") as pending:
                        target.get_by_role("button", name=title, exact=True).click()
                    check(pending.value.status == 200, "Command HTTP failure")
                    expect(target.get_by_role("button", name=title, exact=True)).to_be_enabled()
                    text = page.locator("#output").inner_text()
                    check(text.startswith("Error:") == error, title + ": " + text)
                    return text

                submit("Session", "Log in", {"userId": "SA001"})
                page.get_by_role("button", name="Refresh ID options", exact=True).click()
                expect(page.get_by_role("button", name="Refresh ID options", exact=True)).to_be_enabled()
                expect(page.locator("#output")).to_have_text("ID options refreshed")
                room = entity(submit("Administration", "Add room", {
                    "roomType": "meetingRoom", "capacity": "10", "features": "projector"}), "R")
                user = entity(submit("Administration", "Add user", {"name": "Browser User"}), "U")
                submit("Session", "Log out")
                submit("Session", "Log in", {"userId": user})
                day = (datetime.date.today() + datetime.timedelta(days=14)).isoformat()
                values = {"roomId": room, "date": day, "startTime": "09:00",
                          "endTime": "10:00", "purpose": "Browser meeting"}
                booking = entity(submit("Bookings", "Create booking", values), "B")
                expect(form("Modify booking").locator('[name="bookingId"] option[value="' + booking + '"]')).to_have_count(1)
                submit("Bookings", "Create booking", dict(values, startTime="09:30", endTime="10:30"), error=True)
                submit("Bookings", "Modify booking", dict(values, bookingId=booking,
                       startTime="11:00", endTime="12:00", purpose="Updated browser meeting"))
                check("11:00-12:00 Updated browser meeting" in submit("Bookings", "My bookings"),
                      "Modified booking missing")
                check("Updated browser meeting" in submit("Schedule", "Weekly schedule", {
                    "roomId": room, "weekStartDate": day}), "Schedule missing booking")
                check(room in submit("Rooms", "Search available rooms", {
                    "date": day, "startTime": "12:00", "endTime": "13:00",
                    "minCapacity": "10", "features": "projector"}), "Search failed")
                submit("Bookings", "Delete booking", {"bookingId": booking})
                expect(form("Modify booking").locator('[name="bookingId"] option[value="' + booking + '"]')).to_have_count(0)
                check("No bookings found." in submit("Bookings", "My bookings"), "Booking not deleted")
                submit("Session", "Log out")
                submit("Bookings", "My bookings", error=True)
                # Quit disables every button, so wait for the command response/status instead.
                section("Session")
                with page.expect_response(lambda response: response.url.endswith("/api/command")):
                    form("Quit application").get_by_role("button", name="Quit application", exact=True).click()
                expect(page.locator("#status")).to_have_text(
                    "Application stopped. Restart Java and reload this page.")
                check(not js_errors, "Uncaught JavaScript errors: " + repr(js_errors))
                page.screenshot(path=str(work / "final.png"), full_page=True)
                check(proc.wait(timeout=15) == 0, "Server did not exit cleanly")
                txt, _ = logs(work)
                check("quit" in txt.splitlines(), "Quit command missing from log")
            except Exception:
                failure = traceback.format_exc()
                try:
                    page.screenshot(path=str(work / "failure.png"), full_page=True)
                    (work / "failure.html").write_text(page.content(), encoding="utf-8")
                except Exception:
                    pass
            finally:
                (work / "page-errors.txt").write_text("\n".join(js_errors), encoding="utf-8")
                context.tracing.stop(path=str(work / "trace.zip"))
                browser.close()
    except Exception:
        failure = traceback.format_exc()
    finally:
        if proc.poll() is None:
            proc.terminate()
            try:
                proc.wait(timeout=5)
            except subprocess.TimeoutExpired:
                proc.kill()
                proc.wait(timeout=5)
        output.close()
        errors.close()
    report(work, failure is None, failure)
    return int(failure is not None)

if __name__ == "__main__":
    raise SystemExit(main())
