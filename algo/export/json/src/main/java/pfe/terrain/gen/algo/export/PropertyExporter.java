package pfe.terrain.gen.algo.export;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pfe.terrain.gen.algo.Mappable;
import pfe.terrain.gen.algo.constraints.key.Key;
import pfe.terrain.gen.algo.types.SerializableType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PropertyExporter<T extends Mappable> {

    private Map<T, Integer> indexes;

    public PropertyExporter(Map<T, Integer> indexes) {
        this.indexes = indexes;
    }

    public JsonArray getPropsArray() {
        JsonArray propertiesArray = new JsonArray();
        List<Map.Entry<T, Integer>> orderedEntries = new ArrayList<>(indexes.entrySet());
        orderedEntries.sort(Comparator.comparingInt(Map.Entry::getValue));
        for (Map.Entry<T, Integer> entry : orderedEntries) {
            Map<Key<?>, Object> properties = entry.getKey().getProperties();
            if (properties.isEmpty()) {
                continue;
            }
            JsonArray itemProperties = new JsonArray();
            for (Key key : properties.keySet()) {
                if (!key.isSerialized()) {
                    continue;
                }
                Object value = properties.get(key);
                if (!(value instanceof SerializableType)) {
                    continue;
                }
                JsonElement serialized = ((SerializableType) properties.get(key)).serialize();
                if (serialized == null) {
                    continue;
                }
                JsonObject propertyObject = new JsonObject();
                propertyObject.addProperty("p", key.getSerializedName());
                propertyObject.add("v", serialized);
                itemProperties.add(propertyObject);
            }
            if (itemProperties.size() > 0) {
                JsonObject item = new JsonObject();
                item.add("vals", itemProperties);
                item.addProperty("key", entry.getValue());
                propertiesArray.add(item);
            }
        }
        return propertiesArray;
    }

}
