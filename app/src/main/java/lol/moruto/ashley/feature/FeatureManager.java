package lol.moruto.ashley.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lol.moruto.ashley.MainActivity;
import lol.moruto.ashley.feature.impl.*;

public class FeatureManager {
    private final List<Feature> features = new ArrayList<>();

    public FeatureManager(MainActivity activity) {
        features.add(new CaptureMarkedImageFeature(activity));
        features.add(new FileShredderFeature(activity));
        features.add(new SendHttpRequestFeature(activity));
        features.add(new CryptoFeature(activity));
    }

    public List<Feature> getFeatures() {
        return Collections.unmodifiableList(features);
    }
}