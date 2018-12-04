package pfe.terrain.gen.criteria;

import pfe.terrain.gen.algo.geometry.Face;
import pfe.terrain.gen.algo.key.Key;

import java.util.Map;
import java.util.Set;

public class LakeProximity implements Criterion {

    private static final double WEIGHT = 1;

    public static final Key<Boolean> LAKES_KEY =
            new Key<>("LAKES", Boolean.class);

    private Set<Face> lakes;

    public LakeProximity(Set<Face> lakes) {
        this.lakes = lakes;
    }

    @Override
    public void assignScores(Map<Face, Double> scores) {
        lakes.forEach(lake ->
                scores.forEach((key, value) ->
                        scores.put(key, value + (1 / (lake.getCenter().distance(key.getCenter()) + 0.1)) * WEIGHT)));
    }

}