package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.Locale;

public final class IdGenerator {
    private long nextUserNumber = 1;
    private long nextRoomNumber = 1;
    private long nextBookingNumber = 1;

    public String nextUserId() {
        return String.format(
                Locale.ROOT,
                "U%06d",
                nextUserNumber++
        );
    }

    public String nextRoomId() {
        return String.format(
                Locale.ROOT,
                "R%06d",
                nextRoomNumber++
        );
    }

    public String nextBookingId() {
        return String.format(
                Locale.ROOT,
                "B%06d",
                nextBookingNumber++
        );
    }
}
