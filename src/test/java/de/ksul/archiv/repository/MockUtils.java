package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
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
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
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
    private PropertyDefinitionWrapper propertyDefinitionWrapper = new PropertyDefinitionWrapper();

    private static MockUtils mockUtils;


    public static MockUtils getInstance() {
        if (mockUtils == null)
            mockUtils = new MockUtils();
        return mockUtils;
    }

    public SessionImpl getSession() {
        return sessionImpl;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void setSession(SessionImpl sessionImpl) {
        this.sessionImpl = sessionImpl;
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
            propertyObjectTypeIdDefinition.setUpdatability(Updatability.ONCREATE);
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

    private static Map<String, PropertyDefinition<?>> secondaryPropertyDefinitionMap;

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
        Map<String, PropertyDefinition<?>> allPropertyDefinitionMap = new HashMap<>();
        allPropertyDefinitionMap.putAll(getPropertyDefinitionMap());
        allPropertyDefinitionMap.putAll(getSecondaryPropertyDefinitionMap());
        return allPropertyDefinitionMap;

    }

    private static Map<String, SecondaryType> secondaryTypeStore;

    Map<String, SecondaryType> getSecondaryTypeStore() {
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
        return secondaryTypeStore;
    }

    private static ItemTypeImpl itemType;

    public ItemTypeImpl getItemType() {
        if (itemType == null) {
            ItemTypeDefinitionImpl itemTypeDefinition = new ItemTypeDefinitionImpl();
            itemTypeDefinition.setPropertyDefinitions(getPropertyDefinitionMap());
            itemType = new ItemTypeImpl(sessionImpl, itemTypeDefinition);
            itemType.setId(EnumBaseObjectTypeIds.CMIS_ITEM.value());
            itemType.setBaseTypeId(BaseTypeId.CMIS_ITEM);
        }
        return itemType;
    }

    private static FolderTypeImpl folderType;

    public FolderTypeImpl getFolderType() {

        if (folderType == null) {
            FolderTypeDefinitionImpl folderTypeDefinition = new FolderTypeDefinitionImpl();
            folderTypeDefinition.setPropertyDefinitions(getPropertyDefinitionMap());
            folderType = new FolderTypeImpl(sessionImpl, folderTypeDefinition);
            folderType.setId(EnumBaseObjectTypeIds.CMIS_FOLDER.value());
            folderType.setBaseTypeId(BaseTypeId.CMIS_FOLDER);
        }
        return folderType;
    }


    private static DocumentTypeImpl documentType;

    public DocumentTypeImpl getDocumentType() {

        if (documentType == null) {
            DocumentTypeDefinitionImpl documentTypeDefinition = new DocumentTypeDefinitionImpl();
            documentTypeDefinition.setPropertyDefinitions(getPropertyDefinitionMap());
            documentType = new DocumentTypeImpl(sessionImpl, documentTypeDefinition);
            documentType.setId(EnumBaseObjectTypeIds.CMIS_DOCUMENT.value());
            documentType.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
            documentType.setIsVersionable(true);
        }
        return documentType;
    }

    private static DocumentTypeImpl archivType;

    DocumentTypeImpl getArchivType() {

        if (archivType == null) {
            DocumentTypeDefinitionImpl documentTypeDefinition = new DocumentTypeDefinitionImpl();
            documentTypeDefinition.setPropertyDefinitions(propertyDefinitionWrapper.getPropertyDefinitionMap("{archiv.model}archivModel"));
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


    public FileableCmisObject createFileableCmisObject(Repository repository, Map<String, PropertyData<?>> props, String path, String name, ObjectType objectType, String mimeType) {
        FileableCmisObject fileableCmisObject;
        String parentId;
        String versionSeriesId = repository.UUId();
        PropertiesImpl properties;
        ObjectDataImpl objectData = new ObjectDataImpl();
        if (props == null)
            properties = new PropertiesImpl();
        else
            properties = (PropertiesImpl) convertProperties(props);
        if (!properties.getProperties().containsKey(PropertyIds.VERSION_SERIES_ID)) {

            properties.addProperty(fillProperty(PropertyIds.VERSION_SERIES_ID, versionSeriesId));
        }
        properties.addProperty(fillProperty(PropertyIds.OBJECT_ID, versionSeriesId));
        properties.addProperty(fillProperty(PropertyIds.BASE_TYPE_ID, objectType.getBaseType() != null ? objectType.getBaseType().getId() : objectType.getId()));
        if (!properties.getProperties().containsKey(PropertyIds.OBJECT_TYPE_ID)) {
            properties.addProperty(fillProperty(PropertyIds.OBJECT_TYPE_ID, objectType.getId()));
        }
        properties.addProperty(fillProperty(PropertyIds.CREATION_DATE, new Date().getTime()));
        properties.addProperty(fillProperty(PropertyIds.LAST_MODIFICATION_DATE, new Date().getTime()));
        if (!properties.getProperties().containsKey(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)) {
            properties.addProperty(fillProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Collections.emptyList()));
        }
        if (objectType.getId().equalsIgnoreCase(EnumBaseObjectTypeIds.CMIS_FOLDER.value())) {
            if (path == null) {
                parentId = "-1";
                properties.addProperty(fillProperty(PropertyIds.PATH, "/"));

            } else {
                parentId = repository.getByPath(path).getId();
                properties.addProperty(fillProperty(PropertyIds.PATH, (path != null ? path : "") + (name.equalsIgnoreCase("/") || path.endsWith("/") ? "" : "/") + name));
            }
            properties.addProperty(fillProperty(PropertyIds.NAME, name));
            properties.addProperty(fillProperty(PropertyIds.PARENT_ID, parentId));

            objectData.setProperties(properties);
            fileableCmisObject = new FolderImpl(sessionImpl, objectType, objectData, new OperationContextImpl());
        } else {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
            properties.addProperty(fillProperty(PropertyIds.NAME, name));
            properties.addProperty(fillProperty(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false));
            properties.addProperty(fillProperty(PropertyIds.IS_PRIVATE_WORKING_COPY, false));
            properties.addProperty(fillProperty(PropertyIds.VERSION_LABEL, "0.1"));
            properties.addProperty(fillProperty(PropertyIds.CONTENT_STREAM_ID, null));
            properties.addProperty(fillProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE, mimeType));
            properties.addProperty(fillProperty(PropertyIds.CHECKIN_COMMENT, "Initial Version"));
            objectData.setProperties(properties);
            fileableCmisObject = new DocumentImpl(sessionImpl, objectType, objectData, new OperationContextImpl());
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
            properties.add(new PropertyImpl(MockUtils.getInstance().getAllPropertyDefinitionMap().get(prop.getId()), prop.getValues()));
        return properties;
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
                if (value instanceof List)
                    ((PropertyStringImpl) property).setValues(copyStringValues((List) value));
                else
                    ((PropertyStringImpl) property).setValue(copyStringValue(value));
                break;
            case ID:
                property = new PropertyIdImpl();
                if (value instanceof List)
                    ((PropertyIdImpl) property).setValues(copyStringValues((List) value));
                else
                    ((PropertyIdImpl) property).setValue(copyStringValue(value));
                break;
            case BOOLEAN:
                property = new PropertyBooleanImpl();
                if (value instanceof List)
                    ((PropertyBooleanImpl) property).setValues(copyBooleanValues((List) value));
                else
                    ((PropertyBooleanImpl) property).setValue(copyBooleanValue(value));
                break;
            case INTEGER:
                property = new PropertyIntegerImpl();
                if (value instanceof List)
                    ((PropertyIntegerImpl) property).setValues(copyIntegerValues((List) value));
                else
                    ((PropertyIntegerImpl) property).setValue(copyIntegerValue(value));
                break;
            case DECIMAL:
                property = new PropertyDecimalImpl();
                if (value instanceof List)
                    ((PropertyDecimalImpl) property).setValues(copyDecimalValues((List) value));
                else
                    ((PropertyDecimalImpl) property).setValue(copyDecimalValue(value));
                break;
            case DATETIME:
                property = new PropertyDateTimeImpl();
                if (value instanceof List)
                    ((PropertyDateTimeImpl) property).setValues(copyDateTimeValues((List) value));
                else
                    ((PropertyDateTimeImpl) property).setValue(copyDateTimeValue(value));
                break;
            case HTML:
                property = new PropertyHtmlImpl();
                if (value instanceof List)
                    ((PropertyHtmlImpl) property).setValues(copyStringValues((List) value));
                else
                    ((PropertyHtmlImpl) property).setValue(copyStringValue(value));
                break;
            case URI:
                property = new PropertyUriImpl();
                if (value instanceof List)
                    ((PropertyUriImpl) property).setValues(copyStringValues((List) value));
                else
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
            cmisObject = new FolderImpl(sessionImpl, getFolderType(), objectData, new OperationContextImpl());

        } else if (objectData.getProperties().getProperties().get(PropertyIds.BASE_TYPE_ID).getFirstValue().toString().equalsIgnoreCase(EnumBaseObjectTypeIds.CMIS_ITEM.value())) {
            cmisObject = new ItemImpl(sessionImpl, getDocumentType(), objectData, new OperationContextImpl());
        } else if (objectData.getProperties().getProperties().get(PropertyIds.BASE_TYPE_ID).getFirstValue().toString().equalsIgnoreCase(EnumBaseObjectTypeIds.CMIS_DOCUMENT.value())) {
            if (objectData.getProperties().getProperties().get(PropertyIds.OBJECT_TYPE_ID).getFirstValue().toString().equalsIgnoreCase(EnumBaseObjectTypeIds.CMIS_DOCUMENT.value())) {
                cmisObject = new DocumentImpl(sessionImpl, getDocumentType(), objectData, new OperationContextImpl());
            } else {
                cmisObject = new DocumentImpl(sessionImpl, getArchivType(), objectData, new OperationContextImpl());
            }
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
