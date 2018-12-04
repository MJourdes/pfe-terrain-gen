package pfe.terrain.gen.criteria;

import pfe.terrain.gen.algo.geometry.Face;
import pfe.terrain.gen.algo.key.Key;
import pfe.terrain.gen.algo.types.DoubleType;

import java.util.Map;

import static pfe.terrain.gen.algo.constraints.Contract.facesPrefix;

public class Pitch implements Criterion {

    public static final Key<DoubleType> PITCH_KEY =
            new Key<>(facesPrefix + "PITCH", DoubleType.class);

    private static final double WEIGHT = 0.1;

    @Override
    public void assignScores(Map<Face, Double> scores) {
        scores.forEach((key, value) ->
                scores.put(key, value + (1 / (key.getProperty(PITCH_KEY).value + 0.1)) * WEIGHT));
    }
}