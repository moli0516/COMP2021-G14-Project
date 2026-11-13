package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("MissingJavadoc")
class FeatureSetTest {

    @SuppressWarnings("MissingJavadoc")
    @Test
    void constructorNormalizesFeatures() {
        FeatureSet features = new FeatureSet(
                new HashSet<>(Arrays.asList("Projector!", "WHITE-board")));

        assertEquals(
                new HashSet<>(Arrays.asList("projector", "whiteboard")),
                features.asSet());
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void parseSplitsAndNormalizesFeatures() {
        FeatureSet features = FeatureSet.parse("Projector, Whiteboard/Microphone");

        assertEquals(
                new HashSet<>(Arrays.asList(
                        "projector", "whiteboard", "microphone")),
                features.asSet());
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void returnedSetCannotBeModified() {
        FeatureSet features = FeatureSet.parse("projector");

        //noinspection DataFlowIssue
        assertThrows(
                UnsupportedOperationException.class,
                () -> features.asSet().add("whiteboard"));
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void containsAllChecksForRequiredSubset() {
        FeatureSet available =
                FeatureSet.parse("projector, whiteboard");
        FeatureSet required =
                FeatureSet.parse("projector");

        assertTrue(available.containsAll(required));
        assertFalse(required.containsAll(available));
    }

    @SuppressWarnings("MissingJavadoc")
    @Test
    void rejectsNullSet() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FeatureSet(null));
    }
}
