package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Locale;
import java.util.Collections;

/**
 * An immutable set of normalized room-feature names.
 */
public final class FeatureSet {
    private final Set<String> values;

    /**
     * Copies feature names, removes non-ASCII letters, lowercases them, and removes duplicates.
     *
     * @param values the non-null set of feature names
     * @throws IllegalArgumentException if the set or an entry is null, or a normalized entry has no letters
     */
    public FeatureSet(Set<String> values) {
        if (values == null) {
            throw new IllegalArgumentException("Features must not be null");
        }

        Set<String> normalizedValues = new HashSet<>();
        for (String value : values) {
            if (value == null) {
                throw new IllegalArgumentException("Feature must not be null");
            }

            String normalized = value.replaceAll("[^a-zA-Z]", "").toLowerCase(Locale.ROOT);

            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("Feature must contain at least one letter");
            }

            normalizedValues.add(normalized);
        }

        this.values = Collections.unmodifiableSet(normalizedValues);
    }

    /**
     * Parses feature names separated by commas, slashes, or hyphens.
     *
     * @param text the non-null feature expression
     * @return a normalized feature set
     * @throws IllegalArgumentException if a resulting feature contains no letters
     * @throws NullPointerException if text is null
     */
    public static FeatureSet parse(String text){
        String[] features = text.split("[,/-]");
        for (int i = 0; i < features.length; i++) {
            if (features[i] != null) {
                features[i] = features[i].replaceAll("[^a-zA-Z]", "").toLowerCase();
            }
        }
        return new FeatureSet(new HashSet<String>(Arrays.asList(features)));
    }

    /**
     * Returns the unmodifiable normalized feature set.
     *
     * @return the unmodifiable set of feature names
     */
    public Set<String> asSet() {
        return values;
    }

    /**
     * Checks whether every required feature is present.
     *
     * @param required the non-null required feature set
     * @return whether all required features are included
     * @throws NullPointerException if required is null
     */
    public boolean containsAll(FeatureSet required) {
        return this.values.containsAll(required.asSet());
    }
}
