package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public final class FeatureSet {
    private final Set<String> values;

    public FeatureSet(Set<String> values) {
        // TODO: validate/normalize tokens consistently with parse().
        this.values = Collections.unmodifiableSet(new HashSet<>(values));
    }

    public static FeatureSet parse(String text) {
        throw new UnsupportedOperationException("TODO: parse comma-separated features");
    }

    public Set<String> asSet() {
        return values;
    }

    public boolean containsAll(FeatureSet required) {
        throw new UnsupportedOperationException("TODO: match all required features");
    }
}
