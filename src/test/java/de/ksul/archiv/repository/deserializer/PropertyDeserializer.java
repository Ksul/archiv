package de.ksul.archiv.repository.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.*;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.CmisEnumHelper;
import org.apache.chemistry.opencmis.commons.impl.DateTimeHelper;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.apache.chemistry.opencmis.commons.impl.JSONConstants.*;
import static org.apache.chemistry.opencmis.commons.impl.JSONConstants.JSON_PROPERTY_TYPE_UPDATABILITY;
import static org.apache.chemistry.opencmis.commons.impl.JSONConstants.PROPERTY_TYPE_KEYS;

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
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode pdNode = node.get("propertyDefinition");
        PropertyDefinition pd = convertPropertyDefinition(pdNode);
        PropertyImpl property;
        switch (pd.getPropertyType()) {
            case STRING:
                property = new PropertyImpl(pd,  mapper.readerFor(stringType).readValue(node.get("values")));
                break;
            case ID:
                property = new PropertyImpl(pd,  mapper.readerFor(stringType).readValue(node.get("values")));
                break;
            case BOOLEAN:
                property = new PropertyImpl(pd,  mapper.readerFor(booleanType).readValue(node.get("values")));
                break;
            case INTEGER:
                property = new PropertyImpl(pd,  mapper.readerFor(integerType).readValue(node.get("values")));
                break;
            case DECIMAL:
                property = new PropertyImpl(pd,  mapper.readerFor(decimalType).readValue(node.get("values")));
                break;
            case DATETIME:
                property = new PropertyImpl(pd,  mapper.readerFor(datetimeType).readValue(node.get("values")));
                break;
            case HTML:
                property = new PropertyImpl(pd,  mapper.readerFor(stringType).readValue(node.get("values")));
                break;
            case URI:
                property = new PropertyImpl(pd,  mapper.readerFor(stringType).readValue(node.get("values")));
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


    public PropertyDefinition convertPropertyDefinition(JsonNode json) {
        if (json == null) {
            return null;
        }

        AbstractPropertyDefinition<?> result = null;

        String id = json.get(JSON_PROPERTY_ID).asText();

        // find property type
        PropertyType propertyType = CmisEnumHelper.fromValue(json.get(JSON_PROPERTY_TYPE_PROPERTY_TYPE).asText(), PropertyType.class);
        if (propertyType == null) {
            throw new CmisRuntimeException("Invalid property type '" + id + "'! Data type not set!");
        }

        // find
        Cardinality cardinality = CmisEnumHelper.fromValue(json.get(JSON_PROPERTY_TYPE_CARDINALITY).asText(), Cardinality.class);
        if (cardinality == null) {
            throw new CmisRuntimeException("Invalid property type '" + id + "'! Cardinality not set!");
        }

        switch (propertyType) {
            case STRING:
                result = new PropertyStringDefinitionImpl();
                //((PropertyStringDefinitionImpl) result).setMaxLength(BigInteger.valueOf(json.get(JSON_PROPERTY_TYPE_MAX_LENGTH).asInt()));
                // ((PropertyStringDefinitionImpl) result).setChoices(convertChoicesString(json.get(JSON_PROPERTY_TYPE_CHOICE)));
                break;
            case ID:
                result = new PropertyIdDefinitionImpl();
                //((PropertyIdDefinitionImpl) result).setChoices(convertChoicesString(json.get(JSON_PROPERTY_TYPE_CHOICE)));
                break;
            case BOOLEAN:
                result = new PropertyBooleanDefinitionImpl();
                //((PropertyBooleanDefinitionImpl) result).setChoices(convertChoicesBoolean(json.get(JSON_PROPERTY_TYPE_CHOICE)));
                break;
            case INTEGER:
                result = new PropertyIntegerDefinitionImpl();
                //((PropertyIntegerDefinitionImpl) result).setMinValue(BigInteger.valueOf(json.get(JSON_PROPERTY_TYPE_MIN_VALUE).asInt()));
                //((PropertyIntegerDefinitionImpl) result).setMaxValue(BigInteger.valueOf(json.get(JSON_PROPERTY_TYPE_MAX_VALUE).asInt()));
                //((PropertyIntegerDefinitionImpl) result).setChoices(convertChoicesInteger(json.get(JSON_PROPERTY_TYPE_CHOICE)));
                break;
            case DATETIME:
                result = new PropertyDateTimeDefinitionImpl();
                //((PropertyDateTimeDefinitionImpl) result).setDateTimeResolution(CmisEnumHelper.fromValue(json.get(JSON_PROPERTY_TYPE_RESOLUTION).asText(), DateTimeResolution.class));
                //((PropertyDateTimeDefinitionImpl) result).setChoices(convertChoicesDateTime(json.get(JSON_PROPERTY_TYPE_CHOICE)));
                break;
            case DECIMAL:
                result = new PropertyDecimalDefinitionImpl();
//                ((PropertyDecimalDefinitionImpl) result).setMinValue(BigDecimal.valueOf(json.get(JSON_PROPERTY_TYPE_MIN_VALUE).asLong()));
//                ((PropertyDecimalDefinitionImpl) result).setMaxValue(BigDecimal.valueOf(json.get(JSON_PROPERTY_TYPE_MAX_VALUE).asLong()));
//
//                String precisionStr = json.get(JSON_PROPERTY_TYPE_PRECISION).asText();
//                if (precisionStr != null) {
//                    if ("32".equals(precisionStr)) {
//                        ((PropertyDecimalDefinitionImpl) result).setPrecision(DecimalPrecision.BITS32);
//                    } else if ("64".equals(precisionStr)) {
//                        ((PropertyDecimalDefinitionImpl) result).setPrecision(DecimalPrecision.BITS64);
//                    }
//                }

                //((PropertyDecimalDefinitionImpl) result).setChoices(convertChoicesDecimal(json.get(JSON_PROPERTY_TYPE_CHOICE)));
                break;
            case HTML:
                result = new PropertyHtmlDefinitionImpl();
                //((PropertyHtmlDefinitionImpl) result).setChoices(convertChoicesString(json.get(JSON_PROPERTY_TYPE_CHOICE)));
                break;
            case URI:
                result = new PropertyUriDefinitionImpl();
                //((PropertyUriDefinitionImpl) result).setChoices(convertChoicesString(json.get(JSON_PROPERTY_TYPE_CHOICE)));
                break;
            default:
                throw new CmisRuntimeException("Property type '" + id + "' does not match a data type!");
        }

        // default value
        Object defaultValue = json.get(JSON_PROPERTY_TYPE_DEAULT_VALUE);
        if (defaultValue != null) {
            if (defaultValue instanceof List) {
                List values = new ArrayList();
                for (Object value : (List) defaultValue) {
                    values.add(getCMISValue(value, propertyType));
                }
                result.setDefaultValue(values);
            } else {
                result.setDefaultValue((List) Collections.singletonList(getCMISValue(defaultValue, propertyType)));
            }
        }

        // generic
        result.setId(id);
        result.setPropertyType(propertyType);
        result.setCardinality(cardinality);
        if (json.has(JSON_PROPERTY_TYPE_LOCALNAME))
            result.setLocalName(json.get(JSON_PROPERTY_TYPE_LOCALNAME).asText());
        if (json.has(JSON_PROPERTY_TYPE_LOCALNAMESPACE))
            result.setLocalNamespace(json.get(JSON_PROPERTY_TYPE_LOCALNAMESPACE).asText());
        if (json.has(JSON_PROPERTY_TYPE_QUERYNAME))
            result.setQueryName(json.get(JSON_PROPERTY_TYPE_QUERYNAME).asText());
        if (json.has(JSON_PROPERTY_TYPE_CARDINALITY))
            result.setCardinality(Cardinality.fromValue(json.get(JSON_PROPERTY_TYPE_CARDINALITY).asText()));
        if (json.has(JSON_PROPERTY_TYPE_UPDATABILITY))
            result.setUpdatability(Updatability.fromValue(json.get(JSON_PROPERTY_TYPE_UPDATABILITY).asText()));
        if (json.has("isInherited" ))
            result.setIsInherited(json.get("isInherited").asBoolean());
        if (json.has("isQueryable" ))
            result.setIsQueryable(json.get("isQueryable").asBoolean());
        if (json.has("isOrderable" ))
            result.setIsOrderable(json.get("isOrderable").asBoolean());
        if (json.has("isRequired" ))
            result.setIsRequired(json.get("isRequired").asBoolean());
//        result.setDescription(json.get(JSON_PROPERTY_TYPE_DESCRIPTION).asText());
//        result.setDisplayName(json.get(JSON_PROPERTY_TYPE_DISPLAYNAME).asText());
//        result.setIsInherited(json.get(JSON_PROPERTY_TYPE_INHERITED).asBoolean());
//        result.setIsOpenChoice(json.get(JSON_PROPERTY_TYPE_OPENCHOICE).asBoolean());
//        result.setIsOrderable(json.get(JSON_PROPERTY_TYPE_ORDERABLE).asBoolean());
//        result.setIsQueryable(json.get(JSON_PROPERTY_TYPE_QUERYABLE).asBoolean());
//        result.setIsRequired(json.get(JSON_PROPERTY_TYPE_REQUIRED).asBoolean());

        // handle extensions
        //convertExtension(json, result, PROPERTY_TYPE_KEYS);

        return result;
    }


    public Object getCMISValue(final Object value, final PropertyType propertyType) {
        if (value == null) {
            return null;
        }

        switch (propertyType) {
            case STRING:
            case ID:
            case HTML:
            case URI:
                if (value instanceof String) {
                    return value;
                }
                throw new CmisRuntimeException("Invalid String value!");
            case BOOLEAN:
                if (value instanceof Boolean) {
                    return value;
                }
                throw new CmisRuntimeException("Invalid Boolean value!");
            case INTEGER:
                if (value instanceof BigInteger) {
                    return value;
                }
                throw new CmisRuntimeException("Invalid Integer value!");
            case DECIMAL:
                if (value instanceof BigDecimal) {
                    return value;
                }
                throw new CmisRuntimeException("Invalid Decimal value!");
            case DATETIME:
                if (value instanceof Number) {
                    GregorianCalendar cal = new GregorianCalendar(DateTimeHelper.GMT);
                    cal.setTimeInMillis(((Number) value).longValue());
                    return cal;
                } else if (value instanceof String) {
                    GregorianCalendar cal = DateTimeHelper.parseXmlDateTime((String) value);
                    if (cal == null) {
                        throw new CmisRuntimeException("Invalid DateTime value!");
                    }
                    return cal;
                }
                throw new CmisRuntimeException("Invalid DateTime value!");
            default:
        }

        throw new CmisRuntimeException("Unkown property type!");
    }





}
