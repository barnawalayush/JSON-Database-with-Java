package server.converter;

import com.beust.jcommander.IStringConverter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class JsonElementConverter implements IStringConverter<JsonElement> {
    @Override
    public JsonElement convert(String value) {
        try {
            return new JsonPrimitive(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON string to JsonElement: " + e.getMessage(), e);
        }
    }
}
