package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public final class BookingRepository {
    private final Map<String, Booking> entries = new HashMap<>();

    public Booking require(String bookingId) {
        // TODO: find by ID or throw NotFoundException.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public void add(Booking value) {
        // TODO: reject duplicate ID before insertion.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public void replace(Booking value) {
        // TODO: replace existing entity only.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public void remove(String bookingId) {
        // TODO: remove existing entity.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public List<Booking> findAll() {
        // TODO: return detached list, not live map view.
        throw new UnsupportedOperationException("TODO: implement");
    }

    public List<Booking> findByOwner(String ownerUserId) {
        throw new UnsupportedOperationException("TODO: implement");
    }

    public List<Booking> findByRoom(String roomId) {
        throw new UnsupportedOperationException("TODO: implement");
    }

}
