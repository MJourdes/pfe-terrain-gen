package pfe.terrain.gen.algo.geometry;

import java.util.Collection;
import java.util.HashSet;

public class EdgeSet extends HashSet<Edge> {
    public EdgeSet() {
    }

    public EdgeSet(Collection<Edge> c) {
        super(c);
    }
}