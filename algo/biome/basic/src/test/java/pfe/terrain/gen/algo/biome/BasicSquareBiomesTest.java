package pfe.terrain.gen.algo.biome;

import org.junit.Before;
import org.junit.Test;
import pfe.terrain.gen.algo.Biome;
import pfe.terrain.gen.algo.IslandMap;
import pfe.terrain.gen.algo.Key;
import pfe.terrain.gen.algo.geometry.Coord;
import pfe.terrain.gen.algo.geometry.Edge;
import pfe.terrain.gen.algo.geometry.Face;
import pfe.terrain.gen.algo.geometry.FaceSet;
import pfe.terrain.gen.algo.types.BooleanType;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BasicSquareBiomesTest {

    private BasicSquareBiomes biomeMapper;
    private IslandMap islandMap;

    private Face land = buildTestFace();
    private Face ocean = buildTestFace();

    private static Face buildTestFace() {
        return new Face(new Coord(0, 0),
                Collections.singletonList(
                        new Edge(new Coord(0, 0), new Coord(0, 0))
                ));
    }

    @Before
    public void setUp() throws Exception {
        biomeMapper = new BasicSquareBiomes();
        islandMap = new IslandMap();
        islandMap.putProperty(new Key<>("FACES", FaceSet.class), new FaceSet(Arrays.asList(land, ocean)));
        ocean.putProperty(biomeMapper.faceBorderKey, new BooleanType(true));
        land.putProperty(biomeMapper.faceBorderKey, new BooleanType(false));
    }

    @Test
    public void dumbSquareBiomesTest() throws Exception {
        biomeMapper.execute(islandMap);
        assertThat(land.getProperty(biomeMapper.faceBiomeKey), is(Biome.SUB_TROPICAL_DESERT));
        assertThat(ocean.getProperty(biomeMapper.faceBiomeKey), is(Biome.OCEAN));
    }


}
