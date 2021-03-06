package pfe.terrain.gen.algo.borders;

import pfe.terrain.gen.algo.constraints.Constraints;
import pfe.terrain.gen.algo.constraints.Contract;
import pfe.terrain.gen.algo.constraints.context.Context;
import pfe.terrain.gen.algo.constraints.key.Key;
import pfe.terrain.gen.algo.constraints.key.OptionalKey;
import pfe.terrain.gen.algo.constraints.key.SerializableKey;
import pfe.terrain.gen.algo.island.TerrainMap;
import pfe.terrain.gen.algo.island.geometry.Coord;
import pfe.terrain.gen.algo.island.geometry.Face;
import pfe.terrain.gen.algo.types.MarkerType;

import java.util.Set;
import java.util.stream.Collectors;

public class BasicBorders extends Contract {

    public final Key<MarkerType> VERTICE_BORDER_KEY =
            new SerializableKey<>(new OptionalKey<>(VERTICES_PREFIX + "IS_BORDER", MarkerType.class), "isBorder");

    public final Key<MarkerType> FACE_BORDER_KEY =
            new SerializableKey<>(new OptionalKey<>(FACES_PREFIX + "IS_BORDER", MarkerType.class), "isBorder");

    @Override
    public Constraints getContract() {
        return new Constraints(
                asKeySet(VERTICES, EDGES, FACES),
                asKeySet(VERTICE_BORDER_KEY, FACE_BORDER_KEY)
        );
    }

    @Override
    public String getDescription() {
        return "Annotate tiles as borders if they are less than 10% of the edge of the map";
    }

    @Override
    public void execute(TerrainMap terrainMap, Context context) {
        double offset = terrainMap.getProperty(SIZE) * 0.1;
        Set<Coord> borderVertices = terrainMap.getProperty(VERTICES).stream()
                .filter(c -> isBorder(c, terrainMap.getProperty(SIZE), offset))
                .collect(Collectors.toSet());
        Set<Face> borderFaces = terrainMap.getProperty(FACES).stream()
                .filter(f -> isBorder(f, terrainMap.getProperty(SIZE), offset))
                .collect(Collectors.toSet());
        for (Coord coord : borderVertices) {
            coord.putProperty(VERTICE_BORDER_KEY, new MarkerType());
        }
        for (Face face : borderFaces) {
            face.putProperty(FACE_BORDER_KEY, new MarkerType());
        }
    }

    private boolean isBorder(Coord coord, int islandSize, double offset) {
        return !inBounds(coord.x, islandSize, offset) || !inBounds(coord.y, islandSize, offset);
    }

    private boolean inBounds(double coord, int islandSize, double offset) {
        return coord > offset && coord < islandSize - offset;
    }

    private boolean isBorder(Face face, int islandSize, double offset) {
        for (Coord coord : face.getBorderVertices()) {
            if (isBorder(coord, islandSize, offset)) {
                return true;
            }
        }
        return false;
    }

}
