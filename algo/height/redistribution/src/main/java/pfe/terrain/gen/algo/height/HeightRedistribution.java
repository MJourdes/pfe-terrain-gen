package pfe.terrain.gen.algo.height;

import pfe.terrain.gen.algo.*;
import pfe.terrain.gen.algo.constraints.Constraints;
import pfe.terrain.gen.algo.constraints.Contract;
import pfe.terrain.gen.algo.exception.DuplicateKeyException;
import pfe.terrain.gen.algo.exception.KeyTypeMismatch;
import pfe.terrain.gen.algo.exception.NoSuchKeyException;
import pfe.terrain.gen.algo.geometry.Coord;
import pfe.terrain.gen.algo.geometry.CoordSet;
import pfe.terrain.gen.algo.geometry.Face;
import pfe.terrain.gen.algo.types.BooleanType;
import pfe.terrain.gen.algo.types.DoubleType;

import java.util.*;

public class HeightRedistribution extends Contract {

    static final Key<DoubleType> vertexHeightKey =
            new SerializableKey<>(verticesPrefix + "HEIGHT", "height", DoubleType.class);
    static final Key<BooleanType> vertexWaterKey = new Key<>(verticesPrefix + "IS_WATER", BooleanType.class);

    private Param<Double> redistributionFactorKey = new Param<>("Height factor", Double.class, "0-1", "Dunno", 1.0);

    @Override
    public Constraints getContract() {
        return new Constraints(
                asKeySet(vertices, faces, vertexWaterKey),
                asKeySet(),
                asKeySet(vertexHeightKey)
        );
    }

    @Override
    public Set<Param> getRequestedParameters() {
        return asParamSet(redistributionFactorKey);
    }

    @Override
    public void execute(IslandMap map, Context context)
            throws NoSuchKeyException, KeyTypeMismatch, DuplicateKeyException {

        double scaleFactor = context.getParamOrDefault(redistributionFactorKey);
        scaleFactor = 1 / (0.8 + (scaleFactor * 3.2));
        Map<Coord, Double> verticesHeight = new HashMap<>();
        List<Map.Entry<Coord, Double>> orderedVertices;
        CoordSet vertices = map.getVertices();

        for (Coord coord : vertices) {
            if (!coord.getProperty(vertexWaterKey).value) {
                verticesHeight.put(coord, coord.getProperty(vertexHeightKey).value);
            }
        }
        double minV = Collections.min(verticesHeight.values());
        double maxV = Collections.max(verticesHeight.values());
        verticesHeight.replaceAll((key, val) -> ((val - minV) / (maxV - minV)));
        orderedVertices = new ArrayList<>(verticesHeight.entrySet());
        orderedVertices.sort((o1, o2) -> (int) Math.ceil(1000 * (o1.getValue() - o2.getValue())));
        for (int i = 0; i < orderedVertices.size(); i++) {
            double y = i / (double) (orderedVertices.size() - 1);
            double x = Math.pow(1, scaleFactor) - Math.pow(1 - y, scaleFactor);
            orderedVertices.get(i).setValue(x);
        }
        for (Map.Entry<Coord, Double> c : orderedVertices) {
            c.setValue(c.getValue() * (maxV - minV) + minV);
        }
        for (Coord coord : vertices) {
            if (!coord.getProperty(vertexWaterKey).value) {
                coord.putProperty(vertexHeightKey, new DoubleType(verticesHeight.get(coord)));
            }
        }
        for (Face face : map.getFaces()) {
            double sum = 0;
            int total = 0;
            for (Coord border : face.getBorderVertices()) {
                sum += border.getProperty(vertexHeightKey).value;
                total += 1;
            }
            face.getCenter().putProperty(vertexHeightKey, new DoubleType(sum / (double) total));
        }
    }
}
