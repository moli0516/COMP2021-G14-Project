# Part A test patch - application and test guide

## Contents
Adds 9 JUnit Jupiter test classes (80 @Test methods) and this guide.
Production code is not changed. Existing CROBSTest.java is not changed.
Test path: test/hk/edu/polyu/comp/comp2021/crobs/model/.

Classes:
- UserTest: roles, inherited capabilities and identity getters.
- IdGeneratorTest: agreed ID format, independent counters and uniqueness.
- BookingTest: getters, interval boundaries, immutable replacement/cancellation, validation.
- UserRepositoryTest, RoomRepositoryTest, BookingRepositoryTest: storage contracts,
  duplicate/missing IDs, replacement/removal and snapshot containers.
- CROBSUserTest: startup, login/logout, permission checks and user removal.
- CROBSBookingTest: active bookings, conflicts, owner-only mutations, modification
  consistency, slot release and user-removal cascade.
- PartAExceptionTest: message/cause constructors for the five domain exception types.

## Baseline and scope
Generated after inspecting the current master APIs on 3 November 2026.
CROBS.java blob: 4d481330a87d4c2fad049ae00a246af42d037915
Booking.java blob: 10eab0e6441293ae0ecf663d15f3b17a1388b51b
Tests use the existing command-based design, not previously proposed services.
No reflection, mocks, network calls, console I/O, external dependencies or private
repository access are introduced. Constructors/getters must still be validated locally.
Tests share the model package, so package-private deleteBooking can be exercised;
this does NOT establish that command classes can call it. Make the operation public
when wiring CLI commands.

## Apply safely
Start with a clean working tree in the team clone. Create a feature branch from
updated master, not a newly initialized repository. Run from the project root:

    git switch master
    git pull --ff-only origin master
    git switch -c test/part-a-model
    git apply --check part_a_tests.patch
    git apply part_a_tests.patch
    git diff --stat
    git status

If a target test file already exists locally, stop and merge its contents manually;
do not delete existing tests to make the patch apply. New files appear as untracked
until staged, so inspect git status as well as git diff.

## Running
In IntelliJ, mark test as Test Sources Root, keep the supplied JUnit Jupiter library
references, then run the desired test class or the model test package.
Suggested order:
1. UserTest, IdGeneratorTest, PartAExceptionTest.
2. UserRepositoryTest, RoomRepositoryTest, BookingRepositoryTest.
3. BookingTest.
4. CROBSUserTest.
5. CROBSBookingTest.
Run the model package with Coverage after tests are behaving correctly.

## Expected red tests - do not weaken assertions merely to pass
These are behavioral tests, not a characterization of every current bug. The current
implementation is expected to fail many cases:
- CROBS does not create SA001; most integration fixtures first log in as SA001.
  Fix startup first or these failures will mask later behavior.
- repositories return null or silently ignore duplicate/missing operations.
- login(null) dereferences null and anonymous admin checks dereference null.
- Booking validation is absent; overlap/replacement/cancel still throw TODO errors.
- new bookings use cancelled=true.
- booking room/conflict/owner checks and user-removal cancellation are missing.
Fix one group at a time; do not disable tests or assert TODO exceptions as success.
An assertDoesNotThrow checks that an operation accepts valid input; companion tests
check that the same reservation is actually blocking conflicting bookings.

## Assignment expectations versus team contracts
The assignment requires an initial unique superadmin, initially anonymous session,
unique generated IDs, same-room/date conflict rejection, booking identity preservation,
owner-only booking changes and cancellation when removing users (REQ2-6, REQ10-12).
Source: supplied project description, Sections 1-2.
The following are proposed team/API contracts, not verbatim assignment requirements:
- initial superadmin ID SA001, padded U/R/B six-digit IDs;
- logout-before-login and errors on anonymous logout;
- half-open intervals permitting adjacent reservations;
- immutable Booking replacement and cancellation copies;
- ValidationException for invalid inputs and duplicate repository adds;
- NotFoundException for missing repository require/replace/remove;
- blank-purpose/name rejection and retaining canceled repository records.
Agree these contracts before treating a differing policy as a bug. When changing a
contract, update implementation, tests and API documentation consistently.
Repository lists may be mutable detached copies or unmodifiable snapshots; tests
accept both but require that changing a returned container cannot change storage.

## Known coverage limits / next additions
No tests call absent facade methods: changeRole and personal booking listing must
get tests after their public signatures are implemented. Therefore, REQ4/REQ17 and
regular-admin promotion/demotion/inherited session refresh are NOT yet covered.
Room/search/schedule functions, FeatureSet behavior, CLI/logging, room-deletion
future-time boundaries and persistence are outside this Part A patch's main scope.
RoomRepository tests remain included because its storage contract belongs to A.
The removal cascade test checks that removed users' slots become available; it does
not inspect historical records. There is no public booking-query API for doing so yet.
The failed-modification test checks continued occupancy of the original/conflicting
slots; detailed persisted-field assertions should be added once an authorized query
API exists. No guarantee of >=90% coverage is made.

## Verification performed
Unified-diff generation and git apply --check / apply / reverse-check were verified
in a temporary Git repository. No Java compiler/JUnit execution was available in
the generation environment; no claim is made that the suite has compiled or passed.
Run it locally against the actual supplied IntelliJ/JUnit configuration.
After fixing failures, document real test results and coverage in the PR.
