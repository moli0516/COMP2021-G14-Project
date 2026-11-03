package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public final class FeatureSet {
    private final Set<String> values;

    public FeatureSet(Set<String> values) {
        // TODO: validate/normalize tokens consistently with parse().
        this.values = Collections.unmodifiableSet(new HashSet<>(values));
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
        return required.asSet().equals(this.values);
    }
}
