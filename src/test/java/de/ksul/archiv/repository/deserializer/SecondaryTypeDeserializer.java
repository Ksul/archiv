package de.ksul.archiv.repository.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.objecttype.ObjectTypeHelper;
import org.apache.chemistry.opencmis.client.runtime.objecttype.SecondaryTypeImpl;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.SecondaryTypeDefinitionImpl;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.07.18
 * Time: 16:11
 */
public class SecondaryTypeDeserializer extends JsonDeserializer<SecondaryType> {

    private Session session;

    public SecondaryTypeDeserializer(Session session) {
        super();
        this.session = session;
    }

    @Override
    public SecondaryType deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();
        SecondaryTypeDefinitionImpl secondaryTypeDefinition = new SecondaryTypeDefinitionImpl();
        if (node.has("propertyDefinitions")) {
            TypeReference<HashMap<String, PropertyDefinition<?>>> typeRef
                    = new TypeReference<HashMap<String, PropertyDefinition<?>>>() {};

            secondaryTypeDefinition.setPropertyDefinitions(mapper.readValue(node.get("propertyDefinitions").traverse(mapper), typeRef));
        }
        SecondaryTypeImpl secondaryType = new SecondaryTypeImpl(this.session, secondaryTypeDefinition);
        if (node.has("id"))
            secondaryType.setId(node.get("id").asText());
        if (node.has("baseId"))
            secondaryType.setBaseTypeId(BaseTypeId.fromValue(node.get("baseId").asText()));
        return secondaryType;
    }
}
