package de.ksul.archiv.repository.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.objecttype.DocumentTypeImpl;
import org.apache.chemistry.opencmis.client.runtime.objecttype.FolderTypeImpl;
import org.apache.chemistry.opencmis.client.runtime.objecttype.ItemTypeImpl;
import org.apache.chemistry.opencmis.client.runtime.objecttype.ObjectTypeHelper;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.07.18
 * Time: 16:11
 */
public class ObjectTypeDeserializer extends JsonDeserializer<ObjectType> {

    private Session session;
    public ObjectTypeDeserializer(Session session) {
        super();
        this.session = session;
    }

    @Override
    public ObjectType deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();
        ObjectType objectType = null;
        HashMap<String, PropertyDefinition<?>> propertyDefinitionHashMap = null;
        if (node.has("baseId")) {
            BaseTypeId baseId = BaseTypeId.fromValue(node.get("baseId").asText());
            if (node.has("propertyDefinitions")) {
                TypeReference<HashMap<String, PropertyDefinition<?>>> typeRef
                        = new TypeReference<HashMap<String, PropertyDefinition<?>>>() {};

                propertyDefinitionHashMap = mapper.readValue(node.get("propertyDefinitions").traverse(mapper), typeRef);
            }
            switch (baseId) {
                case CMIS_DOCUMENT:{
                    DocumentTypeDefinitionImpl documentTypeDefinition = new DocumentTypeDefinitionImpl();
                    documentTypeDefinition.setPropertyDefinitions(propertyDefinitionHashMap);
                    objectType = new DocumentTypeImpl(session, documentTypeDefinition);
                    break;
                }
                case CMIS_FOLDER: {
                    FolderTypeDefinitionImpl folderTypeDefinition = new FolderTypeDefinitionImpl();
                    folderTypeDefinition.setPropertyDefinitions(propertyDefinitionHashMap);
                    objectType = new FolderTypeImpl(session, folderTypeDefinition);
                    break;
                }
                case CMIS_ITEM: {
                    ItemTypeDefinitionImpl itemTypeDefinition = new ItemTypeDefinitionImpl();
                    itemTypeDefinition.setPropertyDefinitions(propertyDefinitionHashMap);
                    objectType = new ItemTypeImpl(session, itemTypeDefinition);
                    break;
                }
            }
            if (node.has("id"))
                ((AbstractTypeDefinition) objectType).setId(node.get("id").asText());
            if (node.has("baseId"))
                ((AbstractTypeDefinition) objectType).setBaseTypeId(BaseTypeId.fromValue(node.get("baseId").asText()));
            if (node.has("contentStreamAllowed"))
                ((DocumentTypeDefinitionImpl) objectType).setContentStreamAllowed(ContentStreamAllowed.fromValue(node.get("contentStreamAllowed").asText()));
            if (node.has("isVersionable"))
                ((DocumentTypeDefinitionImpl) objectType).setIsVersionable(node.get("isVersionable").asBoolean());
            return objectType;
        }
        return null;
    }
}
