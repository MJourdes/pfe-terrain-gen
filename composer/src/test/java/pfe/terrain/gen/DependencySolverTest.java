package pfe.terrain.gen;

import org.junit.Test;
import pfe.terrain.gen.algo.Key;
import pfe.terrain.gen.algo.constraints.Contract;
import pfe.terrain.gen.algo.geometry.CoordSet;
import pfe.terrain.gen.algo.geometry.EdgeSet;
import pfe.terrain.gen.algo.geometry.FaceSet;
import pfe.terrain.gen.exception.DuplicatedProductionException;
import pfe.terrain.gen.exception.MissingRequiredException;
import pfe.terrain.gen.exception.UnsolvableException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DependencySolverTest {

    private DependencySolver dependencySolver;

    // EP -> B -> A
    @Test
    public void simpleLineTree() throws Exception {
        Contract A = new TestContract("A", Collections.singletonList(new Key<>("POINTS", CoordSet.class)),
                new ArrayList<>());
        Contract B = new TestContract("B", Collections.singletonList(new Key<>("EDGES", Void.class)),
                Collections.singletonList(new Key<>("POINTS", CoordSet.class)));
        Contract EP = new TestContract("C", new ArrayList<>(),
                Collections.singletonList(new Key<>("EDGES", Void.class)));
        dependencySolver = new DependencySolver(Arrays.asList(A, B), new ArrayList<>(), EP);
        List<Contract> got = dependencySolver.orderContracts();
        assertEquals(2, got.size());
        assertEquals(A, got.get(0));
        assertEquals(B, got.get(1));
    }

    // EP -> C
    //    -> B -> A
    @Test
    public void simpleTree() throws Exception {
        Contract A = new TestContract("A", Collections.singletonList(new Key<>("POINTS", CoordSet.class)),
                new ArrayList<>());
        Contract B = new TestContract("B", Collections.singletonList(new Key<>("EDGES", Void.class)),
                Collections.singletonList(new Key<>("POINTS", CoordSet.class)));
        Contract C = new TestContract("C", Collections.singletonList(new Key<>("FACES", Void.class)),
                new ArrayList<>());
        Contract EP = new TestContract("EP", new ArrayList<>(),
                Arrays.asList(new Key<>("EDGES", Void.class), new Key<>("FACES", Void.class)));
        dependencySolver = new DependencySolver(Arrays.asList(A, B, C), new ArrayList<>(), EP);
        List<Contract> got = dependencySolver.orderContracts();
        assertEquals(3, got.size());
        assertEquals(A, got.get(0));
        assertTrue(got.get(1) == B || got.get(1) == C);
        assertTrue(got.get(2) == B || got.get(2) == C);
        assertTrue(got.contains(B));
        assertTrue(got.contains(C));
    }

    // EP -> C
    //    -> B -> A
    // + available D (like B) but we want B specifically
    // + available E (like A) but we want A specifically
    @Test
    public void simplePriority() throws Exception {
        Contract A = new TestContract("A", Collections.singletonList(new Key<>("POINTS", CoordSet.class)),
                new ArrayList<>());
        Contract B = new TestContract("B", Collections.singletonList(new Key<>("EDGES", Void.class)),
                Collections.singletonList(new Key<>("POINTS", CoordSet.class)));
        Contract C = new TestContract("C", Collections.singletonList(new Key<>("FACES", Void.class)),
                new ArrayList<>());
        Contract D = new TestContract("D", Collections.singletonList(new Key<>("EDGES", Void.class)),
                Collections.singletonList(new Key<>("POINTS", CoordSet.class)));
        Contract E = new TestContract("A", Collections.singletonList(new Key<>("POINTS", CoordSet.class)),
                new ArrayList<>());
        Contract EP = new TestContract("EP", new ArrayList<>(),
                Arrays.asList(new Key<>("EDGES", Void.class), new Key<>("FACES", Void.class)));
        dependencySolver = new DependencySolver(Arrays.asList(D, E, A, B, C), Arrays.asList(A, B), EP);
        List<Contract> got = dependencySolver.orderContracts();
        assertEquals(3, got.size());
        assertEquals(A, got.get(0));
        assertTrue(got.get(1) == B || got.get(1) == C);
        assertTrue(got.get(2) == B || got.get(2) == C);
        assertTrue(got.contains(B));
        assertTrue(got.contains(C));
    }

    @Test (expected = MissingRequiredException.class)
    public void missingResource() throws Exception{
        Contract A = new TestContract("A",new ArrayList<>(),Arrays.asList(new Key<>("EDGES",Void.class)));

        new DependencySolver(Arrays.asList(A),Arrays.asList(A),new FinalContract()).orderContracts();
    }

    @Test(expected = DuplicatedProductionException.class)
    public void sameContracts() throws Exception{
        List<Contract> contracts = new ArrayList<>();

        contracts.add(new TestContract("1",Arrays.asList(new Key<>("EDGES", EdgeSet.class)),Arrays.asList(new Key<>("VERTICES",CoordSet.class))));
        contracts.add(new TestContract("2",Arrays.asList(new Key<>("EDGES",EdgeSet.class)),Arrays.asList(new Key<>("VERTICES",CoordSet.class))));

        contracts.add(new TestContract("3",Arrays.asList(new Key<>("VERTICES",CoordSet.class)),new ArrayList<>()));
        contracts.add(new TestContract("4",Arrays.asList(new Key<>("VERTICES",CoordSet.class)),new ArrayList<>()));

        contracts.add(new TestContract("6",
                Arrays.asList(new Key<>("FACES",FaceSet.class)),
                Arrays.asList(new Key<>("EDGES", EdgeSet.class))));
        contracts.add(new TestContract("6",
                Arrays.asList(new Key<>("FACES",FaceSet.class)),
                Arrays.asList(new Key<>("EDGES", EdgeSet.class))));

        DependencySolver solver = new DependencySolver(contracts,contracts,new FinalContract());

        List<Contract> orders = solver.orderContracts();
    }

    @Test(expected = UnsolvableException.class)
    public void unsolvableTest() throws Exception{
        List<Contract> contracts = new ArrayList<>();

        contracts.add(new TestContract("1",Arrays.asList(new Key<>("EDGES", EdgeSet.class)),Arrays.asList(new Key<>("VERTICES",CoordSet.class))));
        contracts.add(new TestContract("2",Arrays.asList(new Key<>("VERTICES",CoordSet.class)),Arrays.asList(new Key<>("EDGES",EdgeSet.class))));

        new DependencySolver(contracts,contracts,
                new TestContract("3",new ArrayList<>(),Arrays.asList(new Key<>("EDGES",EdgeSet.class)))).
                orderContracts();

    }


}
