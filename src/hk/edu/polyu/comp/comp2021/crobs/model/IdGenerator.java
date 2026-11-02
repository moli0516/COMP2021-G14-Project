package hk.edu.polyu.comp.comp2021.crobs.model;

public final class IdGenerator {
    private long nextUserNumber = 1;
    private long nextRoomNumber = 1;
    private long nextBookingNumber = 1;

    public String nextUserId() {
        throw new UnsupportedOperationException("TODO: unique session user ID");
    }

    public String nextRoomId() {
        throw new UnsupportedOperationException("TODO: unique session room ID");
    }

    public String nextBookingId() {
        throw new UnsupportedOperationException("TODO: unique session booking ID");
    }
}
