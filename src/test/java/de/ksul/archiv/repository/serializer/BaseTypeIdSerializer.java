package de.ksul.archiv.repository.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Updatability;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.07.18
 * Time: 15:05
 */
public class BaseTypeIdSerializer extends StdSerializer<BaseTypeId> {


    public BaseTypeIdSerializer() {
        super(BaseTypeId.class);
    }

    public BaseTypeIdSerializer(Class t) {
        super(t);
    }

    public void serialize(BaseTypeId baseTypeId, JsonGenerator generator,
                          SerializerProvider provider)
            throws IOException, JsonProcessingException {
        generator.writeString(baseTypeId.value());
    }
}

