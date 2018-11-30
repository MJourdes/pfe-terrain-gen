package pfe.terrain.gen;

import pfe.terrain.gen.algo.*;
import pfe.terrain.gen.algo.constraints.Constraints;
import pfe.terrain.gen.algo.constraints.Contract;
import pfe.terrain.gen.algo.geometry.Coord;
import pfe.terrain.gen.algo.geometry.Face;
import pfe.terrain.gen.algo.types.BooleanType;
import pfe.terrain.gen.algo.types.DoubleType;

import java.util.*;

import static pfe.terrain.gen.RiverGenerator.*;

public class LakesFromRivers extends Contract {

    public static final Param<Integer> lakeSizeParam =
            new Param<>("lakesSize", Integer.class, "1-10", "The maximum size the lakes will grow to.", 4,
                    "Maximum lake size");

    @Override
    public Set<Param> getRequestedParameters() {
        return asParamSet(lakeSizeParam);
    }

    // Produced

    public static final Key<Boolean> hasLakesKey = new Key<>("LAKES", Boolean.class);

    // Modified

    public static final Key<BooleanType> faceWaterKey =
            new SerializableKey<>(facesPrefix + "IS_WATER", "isWater", BooleanType.class);

    public static final Key<WaterKind> waterKindKey =
            new SerializableKey<>(facesPrefix + "WATER_KIND", "waterKind", WaterKind.class);



    @Override
    public Constraints getContract() {
        return new Constraints(
                asKeySet(vertices, seed, edges, faces),
                asKeySet(hasLakesKey),
                asKeySet(vertexWaterKey, heightKey, faceWaterKey, waterKindKey, riverFlowKey, isSourceKey, isRiverEndKey)
        );
    }

    private IslandMap islandMap;
    private Random random;
    private RiverGenerator riverGenerator;
    private Set<Coord> newRiverStarts;

    private double maxLakeSize;

    @Override
    public void execute(IslandMap map, Context context) {
        this.islandMap = map;
        this.random = new Random(map.getSeed());
        this.riverGenerator = new RiverGenerator(islandMap);
        this.maxLakeSize = context.getParamOrDefault(lakeSizeParam);
        this.newRiverStarts = new HashSet<>();

        Coord lakeStart = getRiverEndInHole();
        while (lakeStart != null) {
            generateLake(lakeStart);
            lakeStart = getRiverEndInHole();
        }
        boolean hasLakes = false;
        for (Face face : map.getFaces()) {
            if (face.getProperty(waterKindKey) == WaterKind.LAKE) {
                hasLakes = true;
                break;
            }
        }
        islandMap.putProperty(hasLakesKey, hasLakes);
        newRiverStarts.forEach(start -> start.putProperty(vertexWaterKey, new BooleanType(true)));
    }

    private void generateLake(Coord start) {
        Face baseLake = findLowestFace(start);
        double lakeHeight = baseLake.getCenter().getProperty(heightKey).value;
        Set<Face> lakeTiles = new HashSet<>();
        lakeTiles.add(baseLake);
        if (!turnIntoLake(baseLake, lakeHeight)) {
            return;
        }
        List<Coord> candidates = getRiverStartCandidates(lakeTiles);
        while (candidates.isEmpty() && lakeTiles.size() < maxLakeSize) {
            Face newLakeFace = findLowestNeighbour(lakeTiles);
            lakeTiles.add(newLakeFace);
            if (newLakeFace.getProperty(faceWaterKey).value) {
                if (newLakeFace.getProperty(waterKindKey) == WaterKind.OCEAN) {
                    turnLakeIntoOcean(lakeTiles);
                }
                return;
            }
            lakeHeight = getMaxHeight(lakeTiles);
            levelFaces(lakeTiles, lakeHeight);
            if (!turnIntoLake(newLakeFace, lakeHeight)) {
                turnLakeIntoOcean(lakeTiles);
                return;
            }
            candidates = getRiverStartCandidates(lakeTiles);
        }
        if (!candidates.isEmpty()) {
            Set<Coord> seen = new HashSet<>();
            lakeTiles.forEach(tile -> seen.addAll(tile.getBorderVertices()));
            Coord riverStart = candidates.get(random.nextInt(candidates.size()));
            newRiverStarts.add(riverStart);
            riverStart.putProperty(vertexWaterKey, new BooleanType(false));
            riverGenerator.generateRiverFrom(riverStart, seen);
        }
    }

    private Coord getRiverEndInHole() {
        for (Coord vertex : islandMap.getEdgeVertices()) {
            if (vertex.getProperty(isRiverEndKey) && !vertex.getProperty(vertexWaterKey).value) {
                return vertex;
            }
        }
        return null;
    }

    private boolean turnIntoLake(Face face, double lakeHeight) {
        WaterKind kind = WaterKind.LAKE;
        for (Face connected : face.getNeighbors()) {
            if (connected.getProperty(waterKindKey) == WaterKind.OCEAN) {
                kind = WaterKind.OCEAN;
                break;
            }
        }
        if (kind == WaterKind.OCEAN) {
            lakeHeight = 0;
        }
        for (Coord vertex : face.getBorderVertices()) {
            turnIntoWaterPoint(vertex, lakeHeight);
        }
        turnIntoWaterPoint(face.getCenter(), lakeHeight);
        face.putProperty(faceWaterKey, new BooleanType(true));
        face.putProperty(waterKindKey, kind);
        recalculateCenterHeight(face.getNeighbors());
        return kind == WaterKind.LAKE;
    }

    private void turnLakeIntoOcean(Set<Face> lake) {
        for (Face face : lake) {
            for (Coord vertex : face.getBorderVertices()) {
                turnIntoWaterPoint(vertex, 0);
            }
            turnIntoWaterPoint(face.getCenter(), 0);
            face.putProperty(waterKindKey, WaterKind.OCEAN);
            recalculateCenterHeight(face.getNeighbors());
        }
    }

    private void turnIntoWaterPoint(Coord vertex, double height) {
        vertex.putProperty(vertexWaterKey, new BooleanType(true));
        vertex.putProperty(heightKey, new DoubleType(height));
    }

    private List<Coord> getRiverStartCandidates(Set<Face> lake) {
        Set<Coord> result = new HashSet<>();
        for (Face face : lake) {
            for (Coord vertex : face.getBorderVertices()) {
                Set<Coord> connected = islandMap.getConnectedVertices(vertex);
                for (Coord neighbour : connected) {
                    if (neighbour.getProperty(heightKey).value < vertex.getProperty(heightKey).value) {
                        result.add(vertex);
                        break;
                    }
                }
            }
        }
        List<Coord> list = new ArrayList<>(result);
        list.sort((o1, o2) -> (int) (o1.x + o1.y - o2.x - o2.y));
        return list;
    }

    private void levelFaces(Set<Face> faces, double level) {
        for (Face face : faces) {
            for (Coord vertex : face.getBorderVertices()) {
                vertex.putProperty(heightKey, new DoubleType(level));
            }
            face.putProperty(heightKey, new DoubleType(level));
        }
    }

    private Face findLowestFace(Coord start) {
        List<Face> candidates = new ArrayList<>();
        for (Face face : islandMap.getFaces()) {
            Set<Coord> borders = face.getBorderVertices();
            if (borders.contains(start)) {
                candidates.add(face);
            }
        }
        Face min = candidates.get(0);
        for (int i = 1; i < candidates.size(); i++) {
            Face face = candidates.get(i);
            if (face.getCenter().getProperty(heightKey).value < min.getCenter().getProperty(heightKey).value) {
                min = face;
            }
        }
        return min;
    }

    private Face findLowestNeighbour(Set<Face> faces) {
        Face min = null;
        for (Face face : faces) {
            for (Face current : face.getNeighbors()) {
                if (faces.contains(current)) {
                    continue;
                }
                if (min == null || current.getCenter().getProperty(heightKey).value <
                        min.getCenter().getProperty(heightKey).value) {
                    min = current;
                }
            }
        }
        return min;
    }

    private double getMaxHeight(Set<Face> faces) {
        Face max = null;
        for (Face face : faces) {
            if (max == null || face.getCenter().getProperty(heightKey).value >
                    max.getCenter().getProperty(heightKey).value) {
                max = face;
            }
        }
        if (max == null) {
            throw new IllegalArgumentException("Set used for maxHeight is empty!");
        }
        return max.getCenter().getProperty(heightKey).value;
    }

    private void recalculateCenterHeight(Set<Face> faces) {
        for (Face face : faces) {
            double average = 0;
            for (Coord vertex : face.getBorderVertices()) {
                average += vertex.getProperty(heightKey).value;
            }
            face.getCenter().putProperty(heightKey, new DoubleType(average / face.getBorderVertices().size()));
        }
    }

}