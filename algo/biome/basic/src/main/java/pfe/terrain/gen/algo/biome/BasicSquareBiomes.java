package pfe.terrain.gen.algo.biome;

import pfe.terrain.gen.algo.constraints.Constraints;
import pfe.terrain.gen.algo.constraints.Contract;
import pfe.terrain.gen.algo.constraints.context.Context;
import pfe.terrain.gen.algo.constraints.key.Key;
import pfe.terrain.gen.algo.constraints.key.SerializableKey;
import pfe.terrain.gen.algo.island.Biome;
import pfe.terrain.gen.algo.island.TerrainMap;
import pfe.terrain.gen.algo.island.geometry.Face;
import pfe.terrain.gen.algo.types.MarkerType;

import java.util.HashSet;
import java.util.Set;

public class BasicSquareBiomes extends Contract {

    public final Key<MarkerType> FACE_BORDER_KEY =
            new Key<>(FACES_PREFIX + "IS_BORDER", MarkerType.class);

    public final Key<Biome> FACE_BIOME_KEY =
            new SerializableKey<>(FACES_PREFIX + "BIOME", "biome", Biome.class);

    @Override
    public Constraints getContract() {
        return new Constraints(
                asKeySet(FACE_BORDER_KEY, FACES),
                asKeySet(FACE_BIOME_KEY)
        );
    }

    @Override
    public String getDescription() {
        return "Puts desert everywhere possible (so until the borders are reached), it's basically a test algorithm";
    }

    @Override
    public void execute(TerrainMap map, Context context) {
        Set<Face> borderFaces = new HashSet<>();
        for (Face face : map.getProperty(FACES)) {
            if (face.hasProperty(FACE_BORDER_KEY)) {
                borderFaces.add(face);
            }
        }
        for (Face face : map.getProperty(FACES)) {
            if (borderFaces.contains(face)) {
                face.putProperty(FACE_BIOME_KEY, Biome.OCEAN);
            } else {
                face.putProperty(FACE_BIOME_KEY, Biome.SUB_TROPICAL_DESERT);
            }
        }
    }

}
