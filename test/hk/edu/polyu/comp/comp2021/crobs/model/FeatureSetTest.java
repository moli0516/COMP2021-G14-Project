package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FeatureSetTest {

    @Test
    void constructorNormalizesFeatures() {
        FeatureSet features = new FeatureSet(
                new HashSet<>(Arrays.asList("Projector!", "WHITE-board")));

        assertEquals(
                new HashSet<>(Arrays.asList("projector", "whiteboard")),
                features.asSet());
    }

    @Test
    void parseSplitsAndNormalizesFeatures() {
        FeatureSet features = FeatureSet.parse("Projector, Whiteboard/Microphone");

        assertEquals(
                new HashSet<>(Arrays.asList(
                        "projector", "whiteboard", "microphone")),
                features.asSet());
    }

    @Test
    void returnedSetCannotBeModified() {
        FeatureSet features = FeatureSet.parse("projector");

        assertThrows(
                UnsupportedOperationException.class,
                () -> features.asSet().add("whiteboard"));
    }

    @Test
    void containsAllChecksForRequiredSubset() {
        FeatureSet available =
                FeatureSet.parse("projector, whiteboard");
        FeatureSet required =
                FeatureSet.parse("projector");

        assertTrue(available.containsAll(required));
        assertFalse(required.containsAll(available));
    }

    @Test
    void rejectsNullSet() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FeatureSet(null));
    }
}
