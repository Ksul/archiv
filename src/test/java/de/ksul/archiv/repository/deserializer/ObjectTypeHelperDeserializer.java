package de.ksul.archiv.repository.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.objecttype.ObjectTypeHelper;
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
public class ObjectTypeHelperDeserializer extends JsonDeserializer<ObjectTypeHelper> {

    private Session session;
    public ObjectTypeHelperDeserializer(Session session) {
        super();
        this.session = session;
    }

    @Override
    public ObjectTypeHelper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectTypeHelper objectTypeHelper = new ObjectTypeHelper(this.session, null);

        return objectTypeHelper;
    }
}
