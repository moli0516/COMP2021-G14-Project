package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.Locale;

/**
 * Generates independent, incrementing user, room, and booking IDs in memory.
 */
public final class IdGenerator {
    private long nextUserNumber = 1;
    private long nextRoomNumber = 1;
    private long nextBookingNumber = 1;

    /**
     * Returns and consumes the next user identifier.
     *
     * @return {@code U} followed by a number padded to at least six digits, initially 000001
     */
    public String nextUserId() {
        return String.format(
                Locale.ROOT,
                "U%06d",
                nextUserNumber++
        );
    }

    /**
     * Returns and consumes the next room identifier.
     *
     * @return {@code R} followed by a number padded to at least six digits, initially 000001
     */
    public String nextRoomId() {
        return String.format(
                Locale.ROOT,
                "R%06d",
                nextRoomNumber++
        );
    }

    /**
     * Returns and consumes the next booking identifier.
     *
     * @return {@code B} followed by a number padded to at least six digits, initially 000001
     */
    public String nextBookingId() {
        return String.format(
                Locale.ROOT,
                "B%06d",
                nextBookingNumber++
        );
    }
}
