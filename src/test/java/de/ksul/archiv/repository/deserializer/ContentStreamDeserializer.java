package de.ksul.archiv.repository.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.07.18
 * Time: 16:11
 */
public class ContentStreamDeserializer  extends JsonDeserializer<ContentStream> {

    public ContentStreamDeserializer() {
        super();
    }

    @Override
    public ContentStream deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ContentStreamImpl contentStream = new ContentStreamImpl();
        JsonNode node = jp.getCodec().readTree(jp);
        if (node.has("length"))
            contentStream.setLength(BigInteger.valueOf(node.get("length").asLong()));
        if (node.has("stream"))
            contentStream.setStream(new ByteArrayInputStream(node.get("stream").binaryValue()));
        if (node.has("fileName"))
            contentStream.setFileName(node.get("fileName").asText());
        if (node.has("mimeType"))
            contentStream.setMimeType(node.get("mimeType").asText());
        return contentStream;
    }
}
