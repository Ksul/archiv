package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.runtime.DocumentImpl;
import org.apache.chemistry.opencmis.client.runtime.FolderImpl;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionImpl;
import org.apache.chemistry.opencmis.client.runtime.objecttype.DocumentTypeImpl;
import org.apache.chemistry.opencmis.client.runtime.objecttype.FolderTypeImpl;
import org.apache.chemistry.opencmis.client.runtime.objecttype.SecondaryTypeImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 10:02
 */
public class MockUtils {

    private static SessionImpl sessionImpl;

    private static  MockUtils mockUtils;


    static MockUtils getInstance() {
        if (mockUtils == null)
            mockUtils = new MockUtils();
        return mockUtils;
    }

    public static SessionImpl getSession() {
        return sessionImpl;
    }

    public static void setSession(SessionImpl sessionImpl) {
        MockUtils.sessionImpl = sessionImpl;
    }

    private Map<String, PropertyDefinition<?>> propertyDefinitionMap;
    Map<String, PropertyDefinition<?>> getPropertyDefinitionMap() {

        if (propertyDefinitionMap == null) {
            propertyDefinitionMap = new HashMap<>();
            PropertyIdDefinitionImpl propertyIdDefinition = new PropertyIdDefinitionImpl();
            propertyIdDefinition.setId("cmis:objectId");
            propertyIdDefinition.setDisplayName("Object ID");
            propertyIdDefinition.setQueryName("cmis:objectId");
            propertyIdDefinition.setLocalName("objectId");
            propertyIdDefinition.setCardinality(Cardinality.SINGLE);
            propertyIdDefinition.setPropertyType(PropertyType.ID);
            propertyIdDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:objectId", propertyIdDefinition);

            PropertyStringDefinitionImpl propertyPathDefinition = new PropertyStringDefinitionImpl();
            propertyPathDefinition.setId("cmis:path");
            propertyPathDefinition.setDisplayName("Path");
            propertyPathDefinition.setQueryName("cmis:path");
            propertyPathDefinition.setLocalName("path");
            propertyPathDefinition.setCardinality(Cardinality.SINGLE);
            propertyPathDefinition.setPropertyType(PropertyType.STRING);
            propertyPathDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:path", propertyPathDefinition);

            PropertyStringDefinitionImpl propertyNameDefinition = new PropertyStringDefinitionImpl();
            propertyNameDefinition.setId("cmis:name");
            propertyNameDefinition.setDisplayName("Name");
            propertyNameDefinition.setQueryName("cmis:name");
            propertyNameDefinition.setLocalName("name");
            propertyNameDefinition.setCardinality(Cardinality.SINGLE);
            propertyNameDefinition.setPropertyType(PropertyType.STRING);
            propertyNameDefinition.setUpdatability(Updatability.READWRITE);
            propertyDefinitionMap.put("cmis:name", propertyNameDefinition);

            PropertyIdDefinitionImpl propertyObjectTypeIdDefinition = new PropertyIdDefinitionImpl();
            propertyObjectTypeIdDefinition.setId("cmis:objectTypeId");
            propertyObjectTypeIdDefinition.setDisplayName("Object Type Id");
            propertyObjectTypeIdDefinition.setQueryName("cmis:objectTypeId");
            propertyObjectTypeIdDefinition.setLocalName("objectTypeId");
            propertyObjectTypeIdDefinition.setCardinality(Cardinality.SINGLE);
            propertyObjectTypeIdDefinition.setPropertyType(PropertyType.ID);
            propertyObjectTypeIdDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:objectTypeId", propertyObjectTypeIdDefinition);

            PropertyIdDefinitionImpl propertyBaseTypeIdDefinition = new PropertyIdDefinitionImpl();
            propertyBaseTypeIdDefinition.setId("cmis:baseTypeId");
            propertyBaseTypeIdDefinition.setDisplayName("Base Type Id");
            propertyBaseTypeIdDefinition.setQueryName("cmis:baseTypeId");
            propertyBaseTypeIdDefinition.setLocalName("baseTypeId");
            propertyBaseTypeIdDefinition.setCardinality(Cardinality.SINGLE);
            propertyBaseTypeIdDefinition.setPropertyType(PropertyType.ID);
            propertyBaseTypeIdDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:baseTypeId", propertyBaseTypeIdDefinition);

            PropertyIdDefinitionImpl propertyParentIdDefinition = new PropertyIdDefinitionImpl();
            propertyParentIdDefinition.setId("cmis:parentId");
            propertyParentIdDefinition.setDisplayName("Parent Id");
            propertyParentIdDefinition.setQueryName("cmis:parentId");
            propertyParentIdDefinition.setLocalName("parentId");
            propertyParentIdDefinition.setCardinality(Cardinality.SINGLE);
            propertyParentIdDefinition.setPropertyType(PropertyType.ID);
            propertyParentIdDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:parentId", propertyParentIdDefinition);

            PropertyBooleanDefinitionImpl propertyIsVersionCheckedOutDefinition = new PropertyBooleanDefinitionImpl();
            propertyIsVersionCheckedOutDefinition.setId("cmis:isVersionSeriesCheckedOut");
            propertyIsVersionCheckedOutDefinition.setDisplayName("Is version Checked Out");
            propertyIsVersionCheckedOutDefinition.setQueryName("cmis:isVersionSeriesCheckedOut");
            propertyIsVersionCheckedOutDefinition.setLocalName("isVersionSeriesCheckedOut");
            propertyIsVersionCheckedOutDefinition.setCardinality(Cardinality.SINGLE);
            propertyIsVersionCheckedOutDefinition.setPropertyType(PropertyType.BOOLEAN);
            propertyIsVersionCheckedOutDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:isVersionSeriesCheckedOut", propertyIsVersionCheckedOutDefinition);

            PropertyBooleanDefinitionImpl propertyIsPrivateWorkingCopyDefinition = new PropertyBooleanDefinitionImpl();
            propertyIsPrivateWorkingCopyDefinition.setId("cmis:isPrivateWorkingCopy");
            propertyIsPrivateWorkingCopyDefinition.setDisplayName("Is private working copy");
            propertyIsPrivateWorkingCopyDefinition.setQueryName("cmis:isPrivateWorkingCopy");
            propertyIsPrivateWorkingCopyDefinition.setLocalName("isPrivateWorkingCopy");
            propertyIsPrivateWorkingCopyDefinition.setCardinality(Cardinality.SINGLE);
            propertyIsPrivateWorkingCopyDefinition.setPropertyType(PropertyType.BOOLEAN);
            propertyIsPrivateWorkingCopyDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:isPrivateWorkingCopy", propertyIsPrivateWorkingCopyDefinition);

            PropertyStringDefinitionImpl propertyVersionLabelDefinition = new PropertyStringDefinitionImpl();
            propertyVersionLabelDefinition.setId("cmis:versionLabel");
            propertyVersionLabelDefinition.setDisplayName("Version Label");
            propertyVersionLabelDefinition.setQueryName("cmis:versionLabel");
            propertyVersionLabelDefinition.setLocalName("versionLabel");
            propertyVersionLabelDefinition.setCardinality(Cardinality.SINGLE);
            propertyVersionLabelDefinition.setPropertyType(PropertyType.STRING);
            propertyVersionLabelDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:versionLabel", propertyVersionLabelDefinition);

            PropertyBooleanDefinitionImpl propertyIsMajorVersionDefinition = new PropertyBooleanDefinitionImpl();
            propertyIsMajorVersionDefinition.setId("cmis:isMajorVersion");
            propertyIsMajorVersionDefinition.setDisplayName("Is Major Version");
            propertyIsMajorVersionDefinition.setQueryName("cmis:isMajorVersion");
            propertyIsMajorVersionDefinition.setLocalName("isMajorVersion");
            propertyIsMajorVersionDefinition.setCardinality(Cardinality.SINGLE);
            propertyIsMajorVersionDefinition.setPropertyType(PropertyType.BOOLEAN);
            propertyIsMajorVersionDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:isMajorVersion", propertyIsMajorVersionDefinition);

            PropertyBooleanDefinitionImpl propertyVersionSeriesIdDefinition = new PropertyBooleanDefinitionImpl();
            propertyVersionSeriesIdDefinition.setId("cmis:versionSeriesId");
            propertyVersionSeriesIdDefinition.setDisplayName("Version series Id");
            propertyVersionSeriesIdDefinition.setQueryName("cmis:versionSeriesId");
            propertyVersionSeriesIdDefinition.setLocalName("versionSeriesId");
            propertyVersionSeriesIdDefinition.setCardinality(Cardinality.SINGLE);
            propertyVersionSeriesIdDefinition.setPropertyType(PropertyType.ID);
            propertyVersionSeriesIdDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:versionSeriesId", propertyVersionSeriesIdDefinition);

            PropertyStringDefinitionImpl propertyCheckinCommentDefinition = new PropertyStringDefinitionImpl();
            propertyCheckinCommentDefinition.setId("cmis:checkinComment");
            propertyCheckinCommentDefinition.setDisplayName("Checkin Comment");
            propertyCheckinCommentDefinition.setQueryName("cmis:checkinComment");
            propertyCheckinCommentDefinition.setLocalName("checkinComment");
            propertyCheckinCommentDefinition.setCardinality(Cardinality.SINGLE);
            propertyCheckinCommentDefinition.setPropertyType(PropertyType.STRING);
            propertyCheckinCommentDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:checkinComment", propertyCheckinCommentDefinition);

            PropertyStringDefinitionImpl propertyContentStreamMimeTypeDefinition = new PropertyStringDefinitionImpl();
            propertyContentStreamMimeTypeDefinition.setId("cmis:contentStreamMimeType");
            propertyContentStreamMimeTypeDefinition.setDisplayName("Content Stream MIME Type");
            propertyContentStreamMimeTypeDefinition.setQueryName("cmis:contentStreamMimeType");
            propertyContentStreamMimeTypeDefinition.setLocalName("contentStreamMimeType");
            propertyContentStreamMimeTypeDefinition.setCardinality(Cardinality.SINGLE);
            propertyContentStreamMimeTypeDefinition.setPropertyType(PropertyType.STRING);
            propertyContentStreamMimeTypeDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:contentStreamMimeType", propertyContentStreamMimeTypeDefinition);

            PropertyIdDefinitionImpl propertyContentStreamIdTypeDefinition = new PropertyIdDefinitionImpl();
            propertyContentStreamIdTypeDefinition.setId("cmis:contentStreamId");
            propertyContentStreamIdTypeDefinition.setDisplayName("Content Stream Id");
            propertyContentStreamIdTypeDefinition.setQueryName("cmis:contentStreamId");
            propertyContentStreamIdTypeDefinition.setLocalName("contentStreamId");
            propertyContentStreamIdTypeDefinition.setCardinality(Cardinality.SINGLE);
            propertyContentStreamIdTypeDefinition.setPropertyType(PropertyType.STRING);
            propertyContentStreamIdTypeDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:contentStreamId", propertyContentStreamIdTypeDefinition);

            PropertyIdDefinitionImpl propertySecondaryObjectTypeIdDefinition = new PropertyIdDefinitionImpl();
            propertySecondaryObjectTypeIdDefinition.setId("cmis:secondaryObjectTypeIds");
            propertySecondaryObjectTypeIdDefinition.setDisplayName("Secondary Object Type Id");
            propertySecondaryObjectTypeIdDefinition.setQueryName("cmis:secondaryObjectTypeIds");
            propertySecondaryObjectTypeIdDefinition.setLocalName("secondaryObjectTypeIds");
            propertySecondaryObjectTypeIdDefinition.setCardinality(Cardinality.MULTI);
            propertySecondaryObjectTypeIdDefinition.setPropertyType(PropertyType.ID);
            propertySecondaryObjectTypeIdDefinition.setUpdatability(Updatability.READWRITE);
            propertyDefinitionMap.put("cmis:secondaryObjectTypeIds", propertySecondaryObjectTypeIdDefinition);

            PropertyStringDefinitionImpl propertyPersonDefinition = new PropertyStringDefinitionImpl();
            propertyPersonDefinition.setId("my:person");
            propertyPersonDefinition.setDisplayName("Person");
            propertyPersonDefinition.setQueryName("my:person");
            propertyPersonDefinition.setLocalName("person");
            propertyPersonDefinition.setCardinality(Cardinality.SINGLE);
            propertyPersonDefinition.setPropertyType(PropertyType.STRING);
            propertyPersonDefinition.setUpdatability(Updatability.READWRITE);
            propertyDefinitionMap.put("my:person", propertyPersonDefinition);

            PropertyDateTimeDefinitionImpl propertyDocumentDateDefinition = new PropertyDateTimeDefinitionImpl();
            propertyDocumentDateDefinition.setId("my:documentDate");
            propertyDocumentDateDefinition.setDisplayName("Documentdate");
            propertyDocumentDateDefinition.setQueryName("my:documentDate");
            propertyDocumentDateDefinition.setLocalName("documentDate");
            propertyDocumentDateDefinition.setCardinality(Cardinality.SINGLE);
            propertyDocumentDateDefinition.setPropertyType(PropertyType.DATETIME);
            propertyDocumentDateDefinition.setUpdatability(Updatability.READWRITE);
            propertyDefinitionMap.put("my:documentDate", propertyDocumentDateDefinition);

            PropertyDateTimeDefinitionImpl propertyCreateDateDefinition = new PropertyDateTimeDefinitionImpl();
            propertyCreateDateDefinition.setId("cmis:creationDate");
            propertyCreateDateDefinition.setDisplayName("Creation Date");
            propertyCreateDateDefinition.setQueryName("cmis:creationDate");
            propertyCreateDateDefinition.setLocalName("creationDate");
            propertyCreateDateDefinition.setCardinality(Cardinality.SINGLE);
            propertyCreateDateDefinition.setPropertyType(PropertyType.DATETIME);
            propertyCreateDateDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:creationDate", propertyCreateDateDefinition);

            PropertyDateTimeDefinitionImpl propertyLastModificationDateDefinition = new PropertyDateTimeDefinitionImpl();
            propertyLastModificationDateDefinition.setId("cmis:lastModificationDate");
            propertyLastModificationDateDefinition.setDisplayName("Last Modified Date");
            propertyLastModificationDateDefinition.setQueryName("cmis:lastModificationDate");
            propertyLastModificationDateDefinition.setLocalName("lastModificationDate");
            propertyLastModificationDateDefinition.setCardinality(Cardinality.SINGLE);
            propertyLastModificationDateDefinition.setPropertyType(PropertyType.DATETIME);
            propertyLastModificationDateDefinition.setUpdatability(Updatability.READONLY);
            propertyDefinitionMap.put("cmis:lastModificationDate", propertyLastModificationDateDefinition);
        }
        return propertyDefinitionMap;
    }

    private static Map<String, PropertyDefinition<?>> secondaryPropertyDefinitionMap ;

    Map<String, PropertyDefinition<?>> getSecondaryPropertyDefinitionMap() {

        if (secondaryPropertyDefinitionMap == null) {
            secondaryPropertyDefinitionMap = new HashMap<>();
            PropertyStringDefinitionImpl propertyTitleDefinition = new PropertyStringDefinitionImpl();
            propertyTitleDefinition.setId("cm:title");
            propertyTitleDefinition.setDisplayName("Title");
            propertyTitleDefinition.setQueryName("cm:title");
            propertyTitleDefinition.setLocalName("title");
            propertyTitleDefinition.setCardinality(Cardinality.SINGLE);
            propertyTitleDefinition.setPropertyType(PropertyType.STRING);
            propertyTitleDefinition.setUpdatability(Updatability.READWRITE);
            secondaryPropertyDefinitionMap.put("cm:title", propertyTitleDefinition);

            PropertyDecimalDefinitionImpl propertyAmountDefinition = new PropertyDecimalDefinitionImpl();
            propertyAmountDefinition.setId("my:amount");
            propertyAmountDefinition.setDisplayName("Amount");
            propertyAmountDefinition.setQueryName("my:amount");
            propertyAmountDefinition.setLocalName("amount");
            propertyAmountDefinition.setCardinality(Cardinality.SINGLE);
            propertyAmountDefinition.setPropertyType(PropertyType.DECIMAL);
            propertyAmountDefinition.setUpdatability(Updatability.READWRITE);
            secondaryPropertyDefinitionMap.put("my:amount", propertyAmountDefinition);

            PropertyBooleanDefinitionImpl propertyTaxDefinition = new PropertyBooleanDefinitionImpl();
            propertyTaxDefinition.setId("my:tax");
            propertyTaxDefinition.setDisplayName("Tax");
            propertyTaxDefinition.setQueryName("my:tax");
            propertyTaxDefinition.setLocalName("tax");
            propertyTaxDefinition.setCardinality(Cardinality.SINGLE);
            propertyTaxDefinition.setPropertyType(PropertyType.BOOLEAN);
            propertyTaxDefinition.setUpdatability(Updatability.READWRITE);
            secondaryPropertyDefinitionMap.put("my:tax", propertyTaxDefinition);

            PropertyDateTimeDefinitionImpl propertySentDateDefinition = new PropertyDateTimeDefinitionImpl();
            propertySentDateDefinition.setId("cm:sentdate");
            propertySentDateDefinition.setDisplayName("Sentdate");
            propertySentDateDefinition.setQueryName("cm:sentdate");
            propertySentDateDefinition.setLocalName("sentdate");
            propertySentDateDefinition.setCardinality(Cardinality.SINGLE);
            propertySentDateDefinition.setPropertyType(PropertyType.DATETIME);
            propertySentDateDefinition.setUpdatability(Updatability.READWRITE);
            secondaryPropertyDefinitionMap.put("cm:sentdate", propertySentDateDefinition);

            PropertyStringDefinitionImpl propertyIdValueDefinition = new PropertyStringDefinitionImpl();
            propertyIdValueDefinition.setId("my:idvalue");
            propertyIdValueDefinition.setDisplayName("ID Value");
            propertyIdValueDefinition.setQueryName("my:idvalue");
            propertyIdValueDefinition.setLocalName("idvalue");
            propertyIdValueDefinition.setCardinality(Cardinality.SINGLE);
            propertyIdValueDefinition.setPropertyType(PropertyType.STRING);
            propertyIdValueDefinition.setUpdatability(Updatability.READWRITE);
            secondaryPropertyDefinitionMap.put("my:idvalue", propertyIdValueDefinition);

            PropertyStringDefinitionImpl propertyDescriptionDefinition = new PropertyStringDefinitionImpl();
            propertyDescriptionDefinition.setId("cm:description");
            propertyDescriptionDefinition.setDisplayName("Description");
            propertyDescriptionDefinition.setQueryName("cm:description");
            propertyDescriptionDefinition.setLocalName("description");
            propertyDescriptionDefinition.setCardinality(Cardinality.SINGLE);
            propertyDescriptionDefinition.setPropertyType(PropertyType.STRING);
            propertyDescriptionDefinition.setUpdatability(Updatability.READWRITE);
            secondaryPropertyDefinitionMap.put("cm:description", propertyDescriptionDefinition);
        }
         return secondaryPropertyDefinitionMap;
    }

    Map<String, PropertyDefinition<?>> getAllPropertyDefinitionMap() {
        Map<String, PropertyDefinition<?>>  allPropertyDefinitionMap = new HashMap<>();
        allPropertyDefinitionMap.putAll(getPropertyDefinitionMap());
        allPropertyDefinitionMap.putAll(getSecondaryPropertyDefinitionMap());
        return allPropertyDefinitionMap;
        
    }

    private static Map<String, SecondaryType> secondaryTypeStore;

    Map<String, SecondaryType>  getSecondaryTypeStore(){
        if (secondaryTypeStore == null) {
            secondaryTypeStore = new HashMap<>();
            SecondaryTypeDefinitionImpl titled = new SecondaryTypeDefinitionImpl();
            titled.setDisplayName("Titled");
            titled.setId("P:cm:titled");
            titled.setLocalName("titled");
            titled.setQueryName("cm:titled");
            titled.setParentTypeId("cmis:secondary");
            titled.setPropertyDefinitions(getSecondaryPropertyDefinitionMap());
            secondaryTypeStore.put("P:cm:titled", new SecondaryTypeImpl(sessionImpl, titled));

            SecondaryTypeDefinitionImpl amountable = new SecondaryTypeDefinitionImpl();
            amountable.setDisplayName("Amountable");
            amountable.setId("P:my:amountable");
            amountable.setLocalName("amountable");
            amountable.setQueryName("my:amountable");
            amountable.setParentTypeId("cmis:secondary");
            amountable.setPropertyDefinitions(getSecondaryPropertyDefinitionMap());
            secondaryTypeStore.put("P:my:amountable", new SecondaryTypeImpl(sessionImpl, amountable));

            SecondaryTypeDefinitionImpl idable = new SecondaryTypeDefinitionImpl();
            idable.setDisplayName("IDable");
            idable.setId("P:my:idable");
            idable.setLocalName("idable");
            idable.setQueryName("my:idable");
            idable.setParentTypeId("cmis:secondary");
            idable.setPropertyDefinitions(getSecondaryPropertyDefinitionMap());
            secondaryTypeStore.put("P:my:idable", new SecondaryTypeImpl(sessionImpl, idable));

            SecondaryTypeDefinitionImpl emailed = new SecondaryTypeDefinitionImpl();
            emailed.setDisplayName("Emailed");
            emailed.setId("P:cm:emailed");
            emailed.setLocalName("emailed");
            emailed.setQueryName("cm:idable");
            emailed.setParentTypeId("cmis:secondary");
            emailed.setPropertyDefinitions(getSecondaryPropertyDefinitionMap());
            secondaryTypeStore.put("P:cm:emailed", new SecondaryTypeImpl(sessionImpl, emailed));
        }
        return  secondaryTypeStore;
    }

    private static FolderTypeImpl folderType;

    FolderTypeImpl getFolderType(){

        if (folderType == null) {
            FolderTypeDefinitionImpl folderTypeDefinition = new FolderTypeDefinitionImpl();
            folderTypeDefinition.setPropertyDefinitions(getPropertyDefinitionMap());
            folderType = new FolderTypeImpl(sessionImpl, folderTypeDefinition);
            folderType.setId("cmis:folder");
            folderType.setBaseTypeId(BaseTypeId.CMIS_FOLDER);
        }
        return folderType;
    }


    private static DocumentTypeImpl documentType;

    DocumentTypeImpl getDocumentType() {

        if (documentType == null) {
            DocumentTypeDefinitionImpl documentTypeDefinition = new DocumentTypeDefinitionImpl();
            documentTypeDefinition.setPropertyDefinitions(getPropertyDefinitionMap());
            documentType = new DocumentTypeImpl(sessionImpl, documentTypeDefinition);
            documentType.setId("cmis:document");
            documentType.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
            documentType.setIsVersionable(true);
        }
        return documentType;
    }

    private static DocumentTypeImpl archivType;

    DocumentTypeImpl getArchivType() {

        if (archivType == null) {
            DocumentTypeDefinitionImpl documentTypeDefinition = new DocumentTypeDefinitionImpl();
            documentTypeDefinition.setPropertyDefinitions(getPropertyDefinitionMap());
            archivType = new DocumentTypeImpl(sessionImpl, documentTypeDefinition);
            archivType.setId("D:my:archivContent");
            archivType.setIsVersionable(true);
            archivType.setParentTypeId("cmis:document");
            archivType.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
        }
        return archivType;
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

    GregorianCalendar copyDateTimeValue(Object source) {
        GregorianCalendar result = null;
        if (source != null) {
            GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            if (source instanceof  GregorianCalendar) {
                result = (GregorianCalendar) source;
            } else if (source instanceof Number) {
                cal.setTimeInMillis(((Number) source).longValue());
                result = cal;
            } else if (source instanceof Date){
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

     FileableCmisObject createFileableCmisObject(Repository repository, Map<String, Object> props, String path, String name, ObjectType objectType, String mimeType) {
        FileableCmisObject fileableCmisObject;
        String parentId;
        String versionSeriesId = repository.UUId();
        PropertiesImpl properties;
        ObjectDataImpl objectData = new ObjectDataImpl();
        if (props == null)
            properties = new PropertiesImpl();
        else
            properties = (PropertiesImpl) convertProperties(props);
        if (!properties.getProperties().containsKey("cmis:versionSeriesId")) {

            properties.addProperty(fillProperty("cmis:versionSeriesId", versionSeriesId));
        }
        if (!properties.getProperties().containsKey("cmis:objectId")) {
            if (properties.getProperties().containsKey("cmis:versionLabel")) {
                properties.addProperty(fillProperty("cmis:objectId", versionSeriesId + ";" + properties.getProperties().get("cmis:versionLabel").getFirstValue().toString()));
            } else {
                properties.addProperty(fillProperty("cmis:objectId", versionSeriesId));
            }
        }
        if (!properties.getProperties().containsKey("cmis:name")) {
            properties.addProperty(fillProperty("cmis:name", name));
        }
        if (!properties.getProperties().containsKey("cmis:baseTypeId")) {
            properties.addProperty(fillProperty("cmis:baseTypeId", objectType.getBaseType() != null ? objectType.getBaseType().getId() : objectType.getId()));
        }
        if (!properties.getProperties().containsKey("cmis:objectTypeId")) {
            properties.addProperty(fillProperty("cmis:objectTypeId", objectType.getId()));
        }
        if (!properties.getProperties().containsKey("cmis:creationDate")) {
            properties.addProperty(fillProperty("cmis:creationDate", new Date().getTime()));
        }
        if (!properties.getProperties().containsKey("cmis:lastModificationDate")) {
            properties.addProperty(fillProperty("cmis:lastModificationDate", new Date().getTime()));
        }
        if (!properties.getProperties().containsKey("cmis:secondaryObjectTypeIds")) {
            properties.addProperty(fillProperty("cmis:secondaryObjectTypeIds", Collections.emptyList()));
        }
        if (objectType.getId().equalsIgnoreCase("cmis:folder")) {
            if (path == null) {
                parentId = "-1";
            } else
                parentId = repository.getByPath(path).getId();
            if (!properties.getProperties().containsKey("cmis:parentId")) {
                properties.addProperty(fillProperty("cmis:parentId", parentId));
            }
            if (!properties.getProperties().containsKey("cmis:path")) {
                properties.addProperty(fillProperty("cmis:path", (path != null ? path : "") + (name.equalsIgnoreCase("/") || path.endsWith("/") ? "" : "/") + name));
            }

            objectData.setProperties(properties);
            fileableCmisObject = new FolderImpl(sessionImpl, objectType, objectData, new OperationContextImpl());
        } else {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
            if (!properties.getProperties().containsKey("cmis:isVersionSeriesCheckedOut")) {
                properties.addProperty(fillProperty("cmis:isVersionSeriesCheckedOut", false));
            }
            if (!properties.getProperties().containsKey("cmis:isPrivateWorkingCopy")) {
                properties.addProperty(fillProperty("cmis:isPrivateWorkingCopy", false));
            }
            if (!properties.getProperties().containsKey("cmis:versionLabel")) {
                properties.addProperty(fillProperty("cmis:versionLabel", "1.0"));
            }
            if (!properties.getProperties().containsKey("cmis:contentStreamId")) {
                properties.addProperty(fillProperty("cmis:contentStreamId", null));
            }
            if (!properties.getProperties().containsKey("cmis:contentStreamMimeType")) {
                properties.addProperty(fillProperty("cmis:contentStreamMimeType", mimeType));
            }
            if (!properties.getProperties().containsKey("cmis:checkinComment")) {
                properties.addProperty(fillProperty("cmis:checkinComment", "Initial Version"));
            }
            objectData.setProperties(properties);
            fileableCmisObject = new DocumentImpl(sessionImpl, objectType, objectData, new OperationContextImpl());
        }
        return fileableCmisObject;
    }

    Properties convertProperties(final Map<String, Object> props) {

        PropertiesImpl result = new PropertiesImpl();
        PropertyDefinition<?> definition;

        for (String id : props.keySet()) {

            result.addProperty(fillProperty(id, props.get(id)));

        }

        return result;
    }

    Map<String, Object> convProperties(List<Property<?>> properties) {
    Map<String, Object> props = new HashMap<>();
        for (Property prop : properties) {
        if (prop.getDefinition().getPropertyType().equals(PropertyType.DATETIME) && prop.getValue() != null) {
            props.put(prop.getLocalName(), ((GregorianCalendar) prop.getValue()).getTime().getTime());
        } else if (prop.getDefinition().getPropertyType().equals(PropertyType.DECIMAL) && prop.getValue() != null) {
            props.put(prop.getLocalName(), prop.getValue());
        } else if (prop.getDefinition().getPropertyType().equals(PropertyType.BOOLEAN) && prop.getValue() != null) {
            props.put(prop.getLocalName(), prop.getValue());
        } else if (prop.getDefinition().getPropertyType().equals(PropertyType.INTEGER) && prop.getValue() != null) {
            props.put(prop.getLocalName(), prop.getValue());
        } else {
            if (prop.getValueAsString() != null)
                props.put(prop.getLocalName(), prop.getValueAsString());
        }
    }
        return props;
}


    AbstractPropertyData<?> fillProperty(String id, Object value) {

        PropertyDefinition<?> definition;
        AbstractPropertyData<?> property = null;


        if (!getPropertyDefinitionMap().containsKey(id) && !getSecondaryPropertyDefinitionMap().containsKey(id))
            throw new CmisRuntimeException(("Invalid properties " + id));
        if (getPropertyDefinitionMap().containsKey(id))
            definition = getPropertyDefinitionMap().get(id);
        else
            definition = getSecondaryPropertyDefinitionMap().get(id);

        switch (definition.getPropertyType()) {
            case STRING:
                property = new PropertyStringImpl();
                ((PropertyStringImpl) property).setValue(copyStringValue(value));
                break;
            case ID:
                property = new PropertyIdImpl();
                if (value instanceof  List)
                    ((PropertyIdImpl) property).setValues(copyStringValues((List) value));
                else
                    ((PropertyIdImpl) property).setValue(copyStringValue(value));
                break;
            case BOOLEAN:
                property = new PropertyBooleanImpl();
                ((PropertyBooleanImpl) property).setValue(copyBooleanValue(value));
                break;
            case INTEGER:
                property = new PropertyIntegerImpl();
                ((PropertyIntegerImpl) property).setValue(copyIntegerValue(value));
                break;
            case DECIMAL:
                property = new PropertyDecimalImpl();
                ((PropertyDecimalImpl) property).setValue(copyDecimalValue(value));
                break;
            case DATETIME:
                property = new PropertyDateTimeImpl();
                ((PropertyDateTimeImpl) property).setValue(copyDateTimeValue(value));
                break;
            case HTML:
                property = new PropertyHtmlImpl();
                ((PropertyHtmlImpl) property).setValue(copyStringValue(value));
                break;
            case URI:
                property = new PropertyUriImpl();
                ((PropertyUriImpl) property).setValue(copyStringValue(value));
                break;
            default:
                throw new CmisRuntimeException("Unknown property data type!");
        }

        property.setId(id);
        property.setDisplayName(definition.getDisplayName());
        property.setQueryName(definition.getQueryName());
        property.setLocalName(definition.getLocalName());


        return property;
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

    Boolean copyBooleanValue(Object source) {
        Boolean result = null;
        if (source != null) {
            if (source instanceof Boolean) {
                result = (Boolean) source;
            } else if (source instanceof String) {
                result =  Boolean.parseBoolean((String) source);
            } else {
                throw new CmisRuntimeException("Invalid property value: " + source);
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

    BigDecimal copyDecimalValue(Object source) {
        BigDecimal result = null;
        if (source != null) {
            if (source instanceof BigDecimal) {
                result = (BigDecimal) source;
            } else if (source instanceof BigInteger) {
                result = new BigDecimal((BigInteger) source);
            } else if(source instanceof String) {
                result = new BigDecimal((String) source);
            } else if(source instanceof Double) {
                result = new BigDecimal((Double) source);
            } else {
                throw new CmisRuntimeException("Invalid property value: " + source);
            }
        }

        return result;
    }

    FileableCmisObject createObject(List<Property<?>> properties) {
        FileableCmisObject cmisObject = null;
        ObjectData objectData = MockUtils.getInstance().getObjectDataFromProperties(properties);
        if (objectData.getProperties().getProperties().get(PropertyIds.OBJECT_TYPE_ID).getFirstValue().toString().equalsIgnoreCase("cmis:folder"))
            cmisObject = new FolderImpl(sessionImpl, getFolderType(), objectData, new OperationContextImpl());
        else
            cmisObject = new DocumentImpl(sessionImpl, getDocumentType(), objectData, new OperationContextImpl());
        return cmisObject;
    }


}
