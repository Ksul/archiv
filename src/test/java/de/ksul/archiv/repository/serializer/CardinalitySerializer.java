package de.ksul.archiv.repository.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.07.18
 * Time: 15:05
 */
public class CardinalitySerializer extends StdSerializer<Cardinality> {


    public CardinalitySerializer() {
        super(Cardinality.class);
    }

    public CardinalitySerializer(Class t) {
        super(t);
    }

    public void serialize(Cardinality cardinality, JsonGenerator generator,
                          SerializerProvider provider)
            throws IOException, JsonProcessingException {
        generator.writeString(cardinality.value());
    }
}

