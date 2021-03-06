package pfe.terrain.gen;

import org.junit.Before;
import org.junit.Test;
import pfe.terrain.gen.algo.constraints.Contract;
import pfe.terrain.gen.algo.constraints.context.Context;
import pfe.terrain.gen.algo.island.TerrainMap;
import pfe.terrain.gen.algo.island.WaterKind;
import pfe.terrain.gen.algo.island.geometry.Coord;
import pfe.terrain.gen.algo.island.geometry.Face;
import pfe.terrain.gen.algo.island.geometry.FaceSet;
import pfe.terrain.gen.algo.types.BooleanType;
import pfe.terrain.gen.algo.types.DoubleType;

import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LakeMoistureTest {

    private TerrainMap terrainMap;
    private LakeMoisture lakeMoisture;

    private Face farFromLake;
    private Face closeToLake;
    private Face oneTileAway;

    @Before
    public void setUp() {
        terrainMap = new TerrainMap();
        lakeMoisture = new LakeMoisture();
        farFromLake = generateFace(0, false);
        closeToLake = generateFace(1, false);
        oneTileAway = generateFace(2, false);
        Face lake = generateFace(3, true);
        closeToLake.addNeighbor(oneTileAway);
        oneTileAway.addNeighbor(closeToLake);
        lake.addNeighbor(closeToLake);
        closeToLake.addNeighbor(lake);
        terrainMap.putProperty(Contract.FACES, new FaceSet(new HashSet<>(Arrays.asList(
                farFromLake, closeToLake, oneTileAway, lake
        ))));
    }

    @SuppressWarnings("Duplicates") // no test lib necessary for a single test util method
    public static Face generateFace(int seed, boolean isLake) {
        Face result = new Face(new Coord(seed, 2), new HashSet<>());
        result.putProperty(AdapterUtils.FACE_MOISTURE, new DoubleType(0.0));
        result.putProperty(AdapterUtils.FACE_WATER_KEY, new BooleanType(isLake));
        result.putProperty(AdapterUtils.WATER_KIND_KEY, isLake ? WaterKind.LAKE : WaterKind.NONE);
        return result;
    }

    @Test
    public void addMoistureTest() {
        lakeMoisture.execute(terrainMap, new Context());
        assertThat(getMoisture(farFromLake), closeTo(0.0, 0.001));
        for (Face face : Arrays.asList(closeToLake, oneTileAway)) {
            assertThat(getMoisture(face), greaterThan(0.0));
        }
        assertThat(getMoisture(oneTileAway), lessThan(getMoisture(closeToLake)));
    }

    private double getMoisture(Face face) {
        return face.getProperty(AdapterUtils.FACE_MOISTURE).value;
    }


}
