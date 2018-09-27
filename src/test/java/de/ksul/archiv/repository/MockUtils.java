package de.ksul.archiv.repository;

import de.ksul.archiv.configuration.ArchivProperties;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.runtime.*;
import org.apache.chemistry.opencmis.client.runtime.objecttype.DocumentTypeImpl;
import org.apache.chemistry.opencmis.client.runtime.objecttype.FolderTypeImpl;
import org.apache.chemistry.opencmis.client.runtime.objecttype.ItemTypeImpl;
import org.apache.chemistry.opencmis.client.runtime.objecttype.SecondaryTypeImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumBaseObjectTypeIds;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 10:02
 */
public class MockUtils {

    private SessionImpl sessionImpl;
    private ResourceLoader resourceLoader;
    private PropertyDefinitionBuilder propertyDefinitionBuilder ;

    private static MockUtils mockUtils;



    public static MockUtils getInstance() {
        if (mockUtils == null) {
            mockUtils = new MockUtils();
        }
        return mockUtils;
    }

    public SessionImpl getSession() {
        return sessionImpl;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void setArchivTestProperties(ArchivProperties archivProperties) {
        propertyDefinitionBuilder = new PropertyDefinitionBuilder(archivProperties);
    }

    public void setSession(SessionImpl sessionImpl) {
        this.sessionImpl = sessionImpl;
    }

    public PropertyDefinitionBuilder getPropertyDefinitionBuilder() {
        return propertyDefinitionBuilder;
    }

    private static ItemTypeImpl itemType;

    public ItemTypeImpl getItemType() {
        if (itemType == null) {
            ItemTypeDefinitionImpl itemTypeDefinition = new ItemTypeDefinitionImpl();
            Map<String, PropertyDefinition<?>> itemDefinitionMap = propertyDefinitionBuilder.getPropertyDefinitionMap("cmis:item");
            itemTypeDefinition.setPropertyDefinitions(itemDefinitionMap);
            itemType = new ItemTypeImpl(sessionImpl, itemTypeDefinition);
            itemType.setId(EnumBaseObjectTypeIds.CMIS_ITEM.value());
            itemType.setBaseTypeId(BaseTypeId.CMIS_ITEM);
        }
        return itemType;
    }

    private static FolderTypeImpl folderType;

    public FolderTypeImpl getFolderType(String id) {

        if (folderType == null) {
            FolderTypeDefinitionImpl folderTypeDefinition = new FolderTypeDefinitionImpl();
            Map<String, PropertyDefinition<?>> folderDefinitionMap = propertyDefinitionBuilder.getPropertyDefinitionMap(id);
            folderTypeDefinition.setPropertyDefinitions(folderDefinitionMap);
            folderType = new FolderTypeImpl(sessionImpl, folderTypeDefinition);
            folderType.setId(EnumBaseObjectTypeIds.CMIS_FOLDER.value());
            folderType.setBaseTypeId(BaseTypeId.CMIS_FOLDER);
        }
        return folderType;
    }


    public DocumentTypeImpl getDocumentType(String id) {

            DocumentTypeDefinitionImpl documentTypeDefinition = new DocumentTypeDefinitionImpl();
            Map<String, PropertyDefinition<?>> documentDefinitionMap = propertyDefinitionBuilder.getPropertyDefinitionMap(id);
            documentTypeDefinition.setPropertyDefinitions(documentDefinitionMap);
            DocumentTypeImpl documentType = new DocumentTypeImpl(sessionImpl, documentTypeDefinition);
            documentType.setId(EnumBaseObjectTypeIds.CMIS_DOCUMENT.value());
            documentType.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
            documentType.setId(id);
            documentType.setIsVersionable(true);

        return documentType;
    }


    SecondaryTypeImpl getSecondaryType(String type) {
        Map<String, PropertyDefinition<?>> secondaryDefinitionMap = propertyDefinitionBuilder.getPropertyDefinitionMap(type);
        SecondaryTypeDefinitionImpl secondaryTypeDefinition = new SecondaryTypeDefinitionImpl();
        secondaryTypeDefinition.setPropertyDefinitions(secondaryDefinitionMap);
        SecondaryTypeImpl secondaryType = new SecondaryTypeImpl(sessionImpl, secondaryTypeDefinition);
        secondaryType.setId(type);
        secondaryType.setBaseTypeId(BaseTypeId.CMIS_SECONDARY);
        return secondaryType;
    }

    ObjectData getObjectDataFromProperties(List<Property<?>> properties) {

        AbstractPropertyData propertyData = null;
        ObjectDataImpl objectData = new ObjectDataImpl();
        Collection<PropertyData<?>> list = new ArrayList<PropertyData<?>>();
        for (Property property : properties) {
            if (property.getType().equals(PropertyType.ID))
                propertyData = new PropertyIdImpl();
            else if (property.getType().equals(PropertyType.STRING))
                propertyData = new PropertyStringImpl();
            else if (property.getType().equals(PropertyType.BOOLEAN))
                propertyData = new PropertyBooleanImpl();
            else if (property.getType().equals(PropertyType.DATETIME))
                propertyData = new PropertyDateTimeImpl();
            else if (property.getType().equals(PropertyType.DECIMAL))
                propertyData = new PropertyDecimalImpl();
            propertyData.setId(property.getId());
            propertyData.setLocalName(property.getLocalName());
            propertyData.setQueryName(property.getQueryName());
            propertyData.setDisplayName(property.getDisplayName());
            propertyData.setValues(property.getValues());
            list.add(propertyData);
        }
        objectData.setProperties(new PropertiesImpl(list));
        return objectData;
    }


    public FileableCmisObject createFileableCmisObject(Repository repository, Map<String, PropertyData<?>> props, String path, String name, ObjectType objectType, String mimeType) {
        FileableCmisObject fileableCmisObject = null;
        String parentId;
        String versionSeriesId = repository.UUId();
        PropertiesImpl properties;
        ObjectDataImpl objectData = new ObjectDataImpl();
        if (props == null)
            properties = new PropertiesImpl();
        else
            properties = (PropertiesImpl) convertProperties(props);

        properties.addProperty(fillProperty(PropertyIds.OBJECT_ID, versionSeriesId));
        properties.addProperty(fillProperty(PropertyIds.BASE_TYPE_ID, objectType.getBaseTypeId() != null ? objectType.getBaseTypeId().value() : objectType.getId()));
        if (!properties.getProperties().containsKey(PropertyIds.OBJECT_TYPE_ID)) {
            properties.addProperty(fillProperty(PropertyIds.OBJECT_TYPE_ID, objectType.getId()));
        }
        properties.addProperty(fillProperty(PropertyIds.CREATION_DATE, new Date().getTime()));
        properties.addProperty(fillProperty(PropertyIds.LAST_MODIFICATION_DATE, new Date().getTime()));

        properties.addProperty(fillProperty(PropertyIds.NAME, name));
        if (objectType.getId().equalsIgnoreCase(EnumBaseObjectTypeIds.CMIS_FOLDER.value())) {
            if (path == null) {
                parentId = "-1";
                properties.addProperty(fillProperty(PropertyIds.PATH, "/"));

            } else {
                parentId = repository.getByPath(path).getId();
                properties.addProperty(fillProperty(PropertyIds.PATH, (path != null ? path : "") + (name.equalsIgnoreCase("/") || path.endsWith("/") ? "" : "/") + name));
            }

            if (!properties.getProperties().containsKey(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)) {
                ArrayList<String> liste = new ArrayList<>(3);
                liste.add("P:cm:titled");
                liste.add("P:sys:localized");
                liste.add("P:app:uifacets");
                properties.addProperty(fillProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, liste));
                properties.addProperty(fillProperty("cm:title", null));
            }
            properties.addProperty(fillProperty(PropertyIds.PARENT_ID, parentId));

            objectData.setProperties(properties);
            fileableCmisObject = new FolderImpl(sessionImpl, objectType, objectData, new OperationContextImpl());
        } else if (objectType.getBaseTypeId().value().equals(EnumBaseObjectTypeIds.CMIS_DOCUMENT.value()) ){
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
            if (!properties.getProperties().containsKey(PropertyIds.VERSION_SERIES_ID)) {

                properties.addProperty(fillProperty(PropertyIds.VERSION_SERIES_ID, versionSeriesId));
            }

            properties.addProperty(fillProperty(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false));
            properties.addProperty(fillProperty(PropertyIds.IS_PRIVATE_WORKING_COPY, false));
            properties.addProperty(fillProperty(PropertyIds.VERSION_LABEL, "0.1"));
            properties.addProperty(fillProperty(PropertyIds.CONTENT_STREAM_ID, null));
            properties.addProperty(fillProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE, mimeType));
            properties.addProperty(fillProperty(PropertyIds.CHECKIN_COMMENT, "Initial Version"));
            if (objectType.getId().equalsIgnoreCase("D:my:archivContent")) {
                properties.addProperty(fillProperty("my:person", "Klaus"));
                properties.addProperty(fillProperty("my:documentDate", new Date().getTime()));
            }
            if (!properties.getProperties().containsKey(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)) {
                ArrayList<String> liste = new ArrayList<>(4);
                liste.add("P:cm:titled");
                liste.add("P:app:inlineeditable");
                liste.add("P:sys:localized");
                liste.add("P:cm:author");
                properties.addProperty(fillProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, liste));
                properties.addProperty(fillProperty("cm:title", null));
            }
            objectData.setProperties(properties);
            fileableCmisObject = new DocumentImpl(sessionImpl, objectType, objectData, new OperationContextImpl());
           } else if(objectType.getId().equalsIgnoreCase(EnumBaseObjectTypeIds.CMIS_ITEM.value()) ) {
            objectData.setProperties(properties);
            fileableCmisObject = new ItemImpl(sessionImpl, objectType, objectData, new OperationContextImpl());
        }
        return fileableCmisObject;
    }

    Properties convertProperties(final Map<String, PropertyData<?>> props) {

        PropertiesImpl result = new PropertiesImpl();

        for (String id : props.keySet()) {

            result.addProperty(fillProperty(id, props.get(id).getValues()));

        }

        return result;
    }

    Map<String, PropertyData<?>> convProperties(List<Property<?>> properties) {
        Map<String, PropertyData<?>> props = new HashMap<>();
        for (Property prop : properties) {
            props.put(prop.getId(), prop);
        }
        return props;
    }

    List<Property<?>> convPropertyData(Map<String, PropertyData<?>> propertyDataMap) {
        List<Property<?>> properties = new ArrayList<>();
        for (PropertyData<?> prop : propertyDataMap.values())
            properties.add(new PropertyImpl(((PropertyImpl<?>) prop).getDefinition(), prop.getValues()));
        return properties;
    }


    AbstractPropertyData<?> fillProperty(String id, Object value) {

        PropertyDefinition<?> definition = getPropertyDefinitionBuilder().getCmisDictionaryService().findProperty(id).getPropertyDefinition();

        return createProperty(value, definition);
    }

    public AbstractPropertyData<?> createProperty(Object value, PropertyDefinition<?> definition) {

        AbstractPropertyData<?> property;
        switch (definition.getPropertyType()) {
            case STRING:
                property = new PropertyStringImpl();
                if (value instanceof List)
                    property.setValues(copyStringValues((List) value));
                else
                    ((PropertyStringImpl) property).setValue(copyStringValue(value));
                break;
            case ID:
                property = new PropertyIdImpl();
                if (value instanceof List)
                    property.setValues(copyStringValues((List) value));
                else
                    ((PropertyIdImpl) property).setValue(copyStringValue(value));
                break;
            case BOOLEAN:
                property = new PropertyBooleanImpl();
                if (value instanceof List)
                    property.setValues(copyBooleanValues((List) value));
                else
                    ((PropertyBooleanImpl) property).setValue(copyBooleanValue(value));
                break;
            case INTEGER:
                property = new PropertyIntegerImpl();
                if (value instanceof List)
                    property.setValues(copyIntegerValues((List) value));
                else
                    ((PropertyIntegerImpl) property).setValue(copyIntegerValue(value));
                break;
            case DECIMAL:
                property = new PropertyDecimalImpl();
                if (value instanceof List)
                    property.setValues(copyDecimalValues((List) value));
                else
                    ((PropertyDecimalImpl) property).setValue(copyDecimalValue(value));
                break;
            case DATETIME:
                property = new PropertyDateTimeImpl();
                if (value instanceof List)
                    property.setValues(copyDateTimeValues((List) value));
                else
                    ((PropertyDateTimeImpl) property).setValue(copyDateTimeValue(value));
                break;
            case HTML:
                property = new PropertyHtmlImpl();
                if (value instanceof List)
                    property.setValues(copyStringValues((List) value));
                else
                    ((PropertyHtmlImpl) property).setValue(copyStringValue(value));
                break;
            case URI:
                property = new PropertyUriImpl();
                if (value instanceof List)
                    property.setValues(copyStringValues((List) value));
                else
                    ((PropertyUriImpl) property).setValue(copyStringValue(value));
                break;
            default:
                throw new CmisRuntimeException("Unknown property data type!");
        }
        ((AbstractPropertyData) property).setPropertyDefinition(definition);
        property.setId(definition.getId());
        property.setDisplayName(definition.getDisplayName());
        property.setQueryName(definition.getQueryName());
        property.setLocalName(definition.getLocalName());

        return property;
    }

    List<GregorianCalendar> copyDateTimeValues(List<Object> source) {
        List<GregorianCalendar> result = null;
        if (source != null) {
            result = new ArrayList<GregorianCalendar>(source.size());
            for (Object obj : source) {
                GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                if (obj instanceof GregorianCalendar) {
                    result.add((GregorianCalendar) obj);
                } else if (obj instanceof Number) {
                    cal.setTimeInMillis(((Number) obj).longValue());
                    result.add(cal);
                } else if (obj instanceof Date) {
                    cal.setTime((Date) obj);
                    result.add(cal);
                } else if (obj instanceof String) {
                    Long value = Long.parseLong((String) obj);
                    cal.setTime(new Date(value));
                    result.add(cal);
                } else {
                    throw new CmisRuntimeException("Invalid property value: " + obj);
                }
            }
        }

        return result;
    }

    GregorianCalendar copyDateTimeValue(Object source) {
        GregorianCalendar result = null;
        if (source != null) {
            GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            if (source instanceof GregorianCalendar) {
                result = (GregorianCalendar) source;
            } else if (source instanceof Number) {
                cal.setTimeInMillis(((Number) source).longValue());
                result = cal;
            } else if (source instanceof Date) {
                cal.setTime((Date) source);
                result = cal;
            } else if (source instanceof String) {
                Long value = Long.parseLong((String) source);
                cal.setTime(new Date(value));
                result = cal;
            } else {
                throw new CmisRuntimeException("Invalid property value: " + source);
            }
        }

        return result;
    }

    List<String> copyStringValues(List<Object> source) {
        List<String> result = null;
        if (source != null) {
            result = new ArrayList<String>(source.size());
            for (Object obj : source) {
                if (obj instanceof String) {
                    result.add(obj.toString());
                } else {
                    throw new CmisRuntimeException("Invalid property value: " + obj);
                }
            }
        }

        return result;
    }

    String copyStringValue(Object source) {
        String result = null;
        if (source != null) {
            if (source instanceof String) {
                result = source.toString();
            } else {
                throw new CmisRuntimeException("Invalid property value: " + source);
            }
        }

        return result;
    }

    List<Boolean> copyBooleanValues(List<Object> source) {
        List<Boolean> result = null;
        if (source != null) {
            result = new ArrayList<Boolean>(source.size());
            for (Object obj : source) {
                if (obj instanceof Boolean) {
                    result.add((Boolean) obj);
                } else if (obj instanceof String) {
                    result.add(Boolean.parseBoolean(obj.toString()));
                } else {
                    throw new CmisRuntimeException("Invalid property value: " + obj);
                }
            }
        }

        return result;
    }

    Boolean copyBooleanValue(Object source) {
        Boolean result = null;
        if (source != null) {
            if (source instanceof Boolean) {
                result = (Boolean) source;
            } else if (source instanceof String) {
                result = Boolean.parseBoolean((String) source);
            } else {
                throw new CmisRuntimeException("Invalid property value: " + source);
            }
        }

        return result;
    }

    List<BigInteger> copyIntegerValues(List<Object> source) {
        List<BigInteger> result = null;
        if (source != null) {
            result = new ArrayList<BigInteger>(source.size());
            for (Object obj : source) {
                if (obj instanceof Integer) {
                    result.add((BigInteger) obj);
                } else if (obj instanceof String) {
                    result.add(new BigInteger(obj.toString()));
                } else {
                    throw new CmisRuntimeException("Invalid property value: " + obj);
                }
            }
        }

        return result;
    }

    BigInteger copyIntegerValue(Object source) {
        BigInteger result = null;
        if (source != null) {
            if (source instanceof BigInteger) {
                result = (BigInteger) source;
            } else if (source instanceof String) {
                result = new BigInteger((String) source);
            } else {
                throw new CmisRuntimeException("Invalid property value: " + source);
            }
        }

        return result;
    }

    List<BigDecimal> copyDecimalValues(List<Object> source) {
        List<BigDecimal> result = null;
        if (source != null) {
            result = new ArrayList<BigDecimal>(source.size());
            for (Object obj : source) {
                if (obj instanceof BigDecimal) {
                    result.add((BigDecimal) obj);
                } else if (obj instanceof BigInteger) {
                    result.add(new BigDecimal((BigInteger) obj));
                } else if (obj instanceof String) {
                    result.add(new BigDecimal((String) obj));
                } else if (obj instanceof Double) {
                    result.add(new BigDecimal((Double) obj));
                } else {
                    throw new CmisRuntimeException("Invalid property value: " + obj);
                }
            }
        }

        return result;
    }

    BigDecimal copyDecimalValue(Object source) {
        BigDecimal result = null;
        if (source != null) {
            if (source instanceof BigDecimal) {
                result = (BigDecimal) source;
            } else if (source instanceof BigInteger) {
                result = new BigDecimal((BigInteger) source);
            } else if (source instanceof String) {
                result = new BigDecimal((String) source);
            } else if (source instanceof Double) {
                result = new BigDecimal((Double) source);
            } else {
                throw new CmisRuntimeException("Invalid property value: " + source);
            }
        }

        return result;
    }

    FileableCmisObject createObject(Type obj) {
        FileableCmisObject cmisObject = null;
        ObjectData objectData = getObjectDataFromProperties(obj.getProperties());
        if (objectData.getProperties().getProperties().get(PropertyIds.BASE_TYPE_ID).getFirstValue().toString().equalsIgnoreCase(EnumBaseObjectTypeIds.CMIS_FOLDER.value())) {
            cmisObject = new FolderImpl(sessionImpl, obj.getObjectType(), objectData, new OperationContextImpl());

        } else if (objectData.getProperties().getProperties().get(PropertyIds.BASE_TYPE_ID).getFirstValue().toString().equalsIgnoreCase(EnumBaseObjectTypeIds.CMIS_ITEM.value())) {
            cmisObject = new ItemImpl(sessionImpl, obj.getObjectType(), objectData, new OperationContextImpl());
        } else if (objectData.getProperties().getProperties().get(PropertyIds.BASE_TYPE_ID).getFirstValue().toString().equalsIgnoreCase(EnumBaseObjectTypeIds.CMIS_DOCUMENT.value())) {
            cmisObject = new DocumentImpl(sessionImpl, obj.getObjectType(), objectData, new OperationContextImpl());
        }
        LinkedHashMap<String, Property<?>> newProps = new LinkedHashMap<>();
        for (Property<?> p : obj.getProperties()) {
            newProps.put(p.getId(), p);
        }
        try {
            Field propertiesField = AbstractCmisObject.class.getDeclaredField("properties");
            propertiesField.setAccessible(true);
            propertiesField.set(cmisObject, newProps);
        } catch (Exception e) {
            throw new RuntimeException(("Object not set!"));
        }
        return cmisObject;
    }

    public ContentStream createStream(String content, String mimeType) {
        ContentStreamImpl contentStream = new ContentStreamImpl();
        contentStream.setStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        contentStream.setMimeType(mimeType);
        return contentStream;
    }

    public ContentStream createFileStream(String fileName, String mimeType) {
        ContentStreamImpl contentStream = new ContentStreamImpl();
        try {
            MarkableFileInputStream markableFileInputStream = new MarkableFileInputStream(new FileInputStream(resourceLoader.getResource(fileName).getFile()));
            markableFileInputStream.mark(0);
            contentStream.setStream(markableFileInputStream);
            contentStream.setMimeType(mimeType);
        } catch (IOException e) {
            contentStream.setStream(new ByteArrayInputStream(("Can't read File: " + fileName).getBytes(StandardCharsets.UTF_8)));
        }
        return contentStream;
    }
}
