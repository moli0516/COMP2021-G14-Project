package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;

class IdGeneratorTest {
    @Test void sequencesStartAtOneAndIncrease() {
        IdGenerator ids = new IdGenerator();
        assertEquals("U000001", ids.nextUserId());
        assertEquals("U000002", ids.nextUserId());
        assertEquals("R000001", ids.nextRoomId());
        assertEquals("R000002", ids.nextRoomId());
        assertEquals("B000001", ids.nextBookingId());
        assertEquals("B000002", ids.nextBookingId());
    }
    @Test void sequencesAreIndependent() {
        IdGenerator ids = new IdGenerator();
        ids.nextUserId(); ids.nextUserId();
        assertEquals("B000001", ids.nextBookingId());
        assertEquals("R000001", ids.nextRoomId());
        assertEquals("U000003", ids.nextUserId());
    }
    @Test void independentSessionsStartFresh() {
        IdGenerator first = new IdGenerator();
        first.nextUserId();
        assertEquals("U000001", new IdGenerator().nextUserId());
        assertEquals("U000002", first.nextUserId());
    }
    @Test void generatedIdsAreUniqueAcrossTypes() {
        IdGenerator ids = new IdGenerator();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            assertTrue(seen.add(ids.nextUserId()));
            assertTrue(seen.add(ids.nextRoomId()));
            assertTrue(seen.add(ids.nextBookingId()));
        }
        assertEquals(3000, seen.size());
    }
}
