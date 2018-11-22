package pfe.terrain.gen.algo.algorithms;

import pfe.terrain.gen.algo.Key;
import pfe.terrain.gen.algo.SerializableKey;
import pfe.terrain.gen.algo.constraints.Constraints;
import pfe.terrain.gen.algo.constraints.Contract;
import pfe.terrain.gen.algo.types.BooleanType;
import pfe.terrain.gen.algo.types.DoubleType;

public abstract class AltitudeGenerator extends Contract {

    protected final Key<BooleanType> verticeBorderKey = new Key<>(verticesPrefix + "IS_BORDER", BooleanType.class);
    protected final Key<BooleanType> faceBorderKey = new Key<>(facesPrefix + "IS_BORDER", BooleanType.class);

    protected Key<DoubleType> vertexHeightKey = new SerializableKey<>(verticesPrefix + "HEIGHT", "height", DoubleType.class);

    @Override
    public Constraints getContract() {
        return new Constraints(
                asSet(faces, vertices, verticeBorderKey, faceBorderKey),
                asSet(vertexHeightKey)
        );
    }

}
