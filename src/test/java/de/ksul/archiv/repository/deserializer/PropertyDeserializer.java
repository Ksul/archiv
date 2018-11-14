package de.ksul.archiv.repository.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.07.18
 * Time: 11:01
 */
public class PropertyDeserializer<T> extends JsonDeserializer<Property<T>> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final CollectionType stringType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
    private static final CollectionType booleanType = TypeFactory.defaultInstance().constructCollectionType(List.class, Boolean.class);
    private static final CollectionType integerType = TypeFactory.defaultInstance().constructCollectionType(List.class, BigInteger.class);
    private static final CollectionType decimalType = TypeFactory.defaultInstance().constructCollectionType(List.class, BigDecimal.class);
    private static final CollectionType datetimeType = TypeFactory.defaultInstance().constructCollectionType(List.class, GregorianCalendar.class);


    public PropertyDeserializer() {
        super();
    }


    @Override
    public Property deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        final ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode node = jp.getCodec().readTree(jp);
        if (node.has("propertyDefinition")) {
            TypeReference<PropertyDefinition<?>> typeRef
                    = new TypeReference<PropertyDefinition<?>>() {
            };
            PropertyDefinition pd = mapper.readValue(node.get("propertyDefinition").traverse(jp.getCodec()), typeRef);
            PropertyImpl property;
            switch (pd.getPropertyType()) {
                case STRING:
                    property = new PropertyImpl(pd, mapper.readerFor(stringType).readValue(node.get("values")));
                    break;
                case ID:
                    property = new PropertyImpl(pd, mapper.readerFor(stringType).readValue(node.get("values")));
                    break;
                case BOOLEAN:
                    property = new PropertyImpl(pd, mapper.readerFor(booleanType).readValue(node.get("values")));
                    break;
                case INTEGER:
                    property = new PropertyImpl(pd, mapper.readerFor(integerType).readValue(node.get("values")));
                    break;
                case DECIMAL:
                    property = new PropertyImpl(pd, mapper.readerFor(decimalType).readValue(node.get("values")));
                    break;
                case DATETIME:
                    property = new PropertyImpl(pd, mapper.readerFor(datetimeType).readValue(node.get("values")));
                    break;
                case HTML:
                    property = new PropertyImpl(pd, mapper.readerFor(stringType).readValue(node.get("values")));
                    break;
                case URI:
                    property = new PropertyImpl(pd, mapper.readerFor(stringType).readValue(node.get("values")));
                    break;
                default:
                    throw new CmisRuntimeException("Unknown property data type!");
            }
            if (node.has("displayName"))
                property.setDisplayName(node.get("displayName").asText());
            if (node.has("localName"))
                property.setLocalName(node.get("localName").asText());
            if (node.has("queryName"))
                property.setQueryName(node.get("queryName").asText());
            if (node.has("id"))
                property.setId(node.get("id").asText());

            return property;
        }
        return null;
    }


}
