package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Locale;
import java.util.Collections;

public final class FeatureSet {
    private final Set<String> values;

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

    public static FeatureSet parse(String text){
        String[] features = text.split("[,/-]");
        for (int i = 0; i < features.length; i++) {
            if (features[i] != null) {
                features[i] = features[i].replaceAll("[^a-zA-Z]", "").toLowerCase();
            }
        }
        return new FeatureSet(new HashSet<String>(Arrays.asList(features)));
    }

    public Set<String> asSet() {
        return values;
    }

    public boolean containsAll(FeatureSet required) {
        return this.values.containsAll(required.asSet());
    }
}
