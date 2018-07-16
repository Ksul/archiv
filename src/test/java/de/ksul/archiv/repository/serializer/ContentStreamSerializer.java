package de.ksul.archiv.repository.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.07.18
 * Time: 15:47
 */
public class ContentStreamSerializer extends StdSerializer<ContentStream> {

    public ContentStreamSerializer() {
        super(ContentStream.class);
    }

    public ContentStreamSerializer(Class t) {
        super(t);
    }

    @Override
    public void serialize(ContentStream contentStream,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName("length");
        jsonGenerator.writeNumber(contentStream.getLength());
        if (contentStream.getFileName() != null) {
            jsonGenerator.writeFieldName("fileName");
            jsonGenerator.writeString(contentStream.getFileName());
        }
        if (contentStream.getMimeType() != null) {
            jsonGenerator.writeFieldName("mimeType");
            jsonGenerator.writeString(contentStream.getMimeType());
        }
        if (contentStream.getStream() != null) {
            jsonGenerator.writeFieldName("stream");
            jsonGenerator.writeBinary(contentStream.getStream(), -1);
        }
        jsonGenerator.writeEndObject();
    }
}
