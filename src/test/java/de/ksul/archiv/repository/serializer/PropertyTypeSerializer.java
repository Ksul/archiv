package de.ksul.archiv.repository.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.07.18
 * Time: 15:05
 */
public class PropertyTypeSerializer extends StdSerializer<PropertyType> {


    public PropertyTypeSerializer() {
        super(PropertyType.class);
    }

    public PropertyTypeSerializer(Class t) {
        super(t);
    }

    public void serialize(PropertyType propertyType, JsonGenerator generator,
                          SerializerProvider provider)
            throws IOException, JsonProcessingException {
        generator.writeString(propertyType.value());
    }
}

