package de.ksul.archiv.repository;

import de.ksul.archiv.PDFConnector;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.ObjectServiceImpl;
import org.apache.chemistry.opencmis.client.runtime.*;
import org.apache.chemistry.opencmis.client.runtime.objecttype.DocumentTypeImpl;
import org.apache.chemistry.opencmis.client.runtime.objecttype.FolderTypeImpl;
import org.apache.chemistry.opencmis.client.runtime.objecttype.SecondaryTypeImpl;
import org.apache.chemistry.opencmis.client.runtime.repository.ObjectFactoryImpl;
import org.apache.chemistry.opencmis.client.runtime.util.CollectionIterable;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.*;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.chemistry.opencmis.commons.spi.*;
import org.apache.commons.io.IOUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 12/4/16
 * Time: 2:47 PM
 */
@Service
public class CMISSessionGeneratorMock implements CMISSessionGenerator {

    private static Logger logger = LoggerFactory.getLogger(CMISSessionGeneratorMock.class.getName());
    private ResourceLoader resourceLoader;

    private Map<String, PropertyDefinition<?>> propertyDefinitionMap;
    private Map<String, PropertyDefinition<?>> secondaryPropertyDefinitionMap;
    private Map<String, SecondaryType> secondaryTypeStore;


    private SessionImpl sessionImpl;
    private SessionFactory sessionFactory;
    private FolderTypeImpl folderType;
    private DocumentTypeImpl documentType;
    private DocumentTypeImpl archivType;
    private RepositoryInfo repositoryInfo;
    private CmisBinding binding;
    private ObjectFactory objectFactory;
    private ObjectServiceImpl objectService;
    private AuthenticationProvider authenticationProvider;
    private Repository repository = new Repository();



    public CMISSessionGeneratorMock(ResourceLoader resourceLoader) {


        //       cmisBindingHelper = mock(CmisBindingHelper.class);
        //      given(cmisBindingHelper.createBinding(anyMap(), any(AuthenticationProvider.class), any(TypeDefinitionCache.class))).willReturn(binding);
        this.resourceLoader = resourceLoader;
        propertyDefinitionMap = getPropertyDefinitionMap();
        secondaryPropertyDefinitionMap = getSecondaryPropertyDefinitionMap();
        objectService = mockObjectService();
        binding = mockBinding();
        mockCollectionIterable();



        repositoryInfo = mockRepositoryInfo();
        objectFactory = new ObjectFactoryImpl();
        authenticationProvider = mock(AuthenticationProvider.class);
        sessionImpl = mockSession();
        when(sessionImpl.getRepositoryId()).thenReturn("0");
        objectFactory.initialize(sessionImpl, null);
        sessionFactory = mockSessionFactory();

        FolderTypeDefinitionImpl folderTypeDefinition = new FolderTypeDefinitionImpl();
        folderTypeDefinition.setPropertyDefinitions(propertyDefinitionMap);
        folderType = new FolderTypeImpl(sessionImpl, folderTypeDefinition);
        folderType.setId("cmis:folder");
        folderType.setBaseTypeId(BaseTypeId.CMIS_FOLDER);

        DocumentTypeDefinitionImpl documentTypeDefinition = new DocumentTypeDefinitionImpl();
        documentTypeDefinition.setPropertyDefinitions(propertyDefinitionMap);
        documentType = new DocumentTypeImpl(sessionImpl, documentTypeDefinition);
        documentType.setId("cmis:document");
        documentType.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
        documentType.setIsVersionable(true);
        archivType = new DocumentTypeImpl(sessionImpl, documentTypeDefinition);
        archivType.setId("D:my:archivContent");
        archivType.setIsVersionable(true);
        archivType.setParentTypeId("cmis:document");
        archivType.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
        secondaryTypeStore = createSecondaryTypes();

        Map<String, Object> properties = new HashMap<>();

        repository.insert(null, createFileableCmisObject(null,  null, "/", folderType, null));
        repository.insert("/", createFileableCmisObject(null, "/", "Datenverzeichnis",  folderType, null));
        repository.insert("/Datenverzeichnis", createFileableCmisObject(null, "/Datenverzeichnis", "Skripte",  folderType,  null));
        repository.insert("/Datenverzeichnis/Skripte", createFileableCmisObject(null, "/Datenverzeichnis/Skripte", "backup.js.sample", documentType, "application/x-javascript"), createStream("// "));
        repository.insert("/Datenverzeichnis/Skripte", createFileableCmisObject(null, "/Datenverzeichnis/Skripte", "alfresco docs.js.sample",  documentType, "application/x-javascript"), createStream("// "));
        repository.insert("/Datenverzeichnis/Skripte", createFileableCmisObject(null, "/Datenverzeichnis/Skripte", "doc.xml",  documentType, "text/xml"), createFileStream("classpath:static/rules/doc.xml"));
        repository.insert("/Datenverzeichnis/Skripte", createFileableCmisObject(null, "/Datenverzeichnis/Skripte", "doc.xsd",  documentType, "text/xml"), createFileStream("classpath:static/rules/doc.xsd"));
        repository.insert("/Datenverzeichnis/Skripte", createFileableCmisObject(null, "/Datenverzeichnis/Skripte", "recognition.js",  documentType, "application/x-javascript"), createFileStream("classpath:static/js/recognition.js"));


    }

    private ContentStream createStream(String content) {
        ContentStreamImpl contentStream = new ContentStreamImpl();
        contentStream.setStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        return contentStream;
    }

    private ContentStream createFileStream(String fileName) {
        ContentStreamImpl contentStream = new ContentStreamImpl();
        try {
            MarkableFileInputStream markableFileInputStream = new MarkableFileInputStream(new FileInputStream(resourceLoader.getResource(fileName).getFile()));
            markableFileInputStream.mark(0);
            contentStream.setStream(markableFileInputStream);
        } catch (IOException e) {
            contentStream.setStream(new ByteArrayInputStream(("Can't read File: " + fileName).getBytes(StandardCharsets.UTF_8)));
        }
        return contentStream;
    }


    private Map<String, SecondaryType> createSecondaryTypes() {
        Map<String, SecondaryType> map = new HashMap<String, SecondaryType>();
        
        SecondaryTypeDefinitionImpl titled = new SecondaryTypeDefinitionImpl();
        titled.setDisplayName("Titled");
        titled.setId("P:cm:titled");
        titled.setLocalName("titled");
        titled.setQueryName("cm:titled");
        titled.setParentTypeId("cmis:secondary");
        titled.setPropertyDefinitions(secondaryPropertyDefinitionMap);
        map.put("P:cm:titled", new SecondaryTypeImpl(sessionImpl, titled));

        SecondaryTypeDefinitionImpl amountable = new SecondaryTypeDefinitionImpl();
        amountable.setDisplayName("Amountable");
        amountable.setId("P:my:amountable");
        amountable.setLocalName("amountable");
        amountable.setQueryName("my:amountable");
        amountable.setParentTypeId("cmis:secondary");
        amountable.setPropertyDefinitions(secondaryPropertyDefinitionMap);
        map.put("P:my:amountable", new SecondaryTypeImpl(sessionImpl, amountable));

        SecondaryTypeDefinitionImpl idable = new SecondaryTypeDefinitionImpl();
        idable.setDisplayName("IDable");
        idable.setId("P:my:idable");
        idable.setLocalName("idable");
        idable.setQueryName("my:idable");
        idable.setParentTypeId("cmis:secondary");
        idable.setPropertyDefinitions(secondaryPropertyDefinitionMap);
        map.put("P:my:idable", new SecondaryTypeImpl(sessionImpl, idable));

        SecondaryTypeDefinitionImpl emailed = new SecondaryTypeDefinitionImpl();
        emailed.setDisplayName("Emailed");
        emailed.setId("P:cm:emailed");
        emailed.setLocalName("emailed");
        emailed.setQueryName("cm:idable");
        emailed.setParentTypeId("cmis:secondary");
        emailed.setPropertyDefinitions(secondaryPropertyDefinitionMap);
        map.put("P:cm:emailed", new SecondaryTypeImpl(sessionImpl, emailed));

        return map;
    }

    private Map<String, PropertyDefinition<?>> getSecondaryPropertyDefinitionMap() {
        Map<String, PropertyDefinition<?>> map = new HashMap<String, PropertyDefinition<?>>();

        PropertyStringDefinitionImpl propertyTitleDefinition = new PropertyStringDefinitionImpl();
        propertyTitleDefinition.setId("cm:title");
        propertyTitleDefinition.setDisplayName("Title");
        propertyTitleDefinition.setQueryName("cm:title");
        propertyTitleDefinition.setLocalName("title");
        propertyTitleDefinition.setCardinality(Cardinality.SINGLE);
        propertyTitleDefinition.setPropertyType(PropertyType.STRING);
        propertyTitleDefinition.setUpdatability(Updatability.READWRITE);
        map.put("cm:title", propertyTitleDefinition);

        PropertyDecimalDefinitionImpl propertyAmountDefinition = new PropertyDecimalDefinitionImpl();
        propertyAmountDefinition.setId("my:amount");
        propertyAmountDefinition.setDisplayName("Amount");
        propertyAmountDefinition.setQueryName("my:amount");
        propertyAmountDefinition.setLocalName("amount");
        propertyAmountDefinition.setCardinality(Cardinality.SINGLE);
        propertyAmountDefinition.setPropertyType(PropertyType.DECIMAL);
        propertyAmountDefinition.setUpdatability(Updatability.READWRITE);
        map.put("my:amount", propertyAmountDefinition);

        PropertyBooleanDefinitionImpl propertyTaxDefinition = new PropertyBooleanDefinitionImpl();
        propertyTaxDefinition.setId("my:tax");
        propertyTaxDefinition.setDisplayName("Tax");
        propertyTaxDefinition.setQueryName("my:tax");
        propertyTaxDefinition.setLocalName("tax");
        propertyTaxDefinition.setCardinality(Cardinality.SINGLE);
        propertyTaxDefinition.setPropertyType(PropertyType.BOOLEAN);
        propertyTaxDefinition.setUpdatability(Updatability.READWRITE);
        map.put("my:tax", propertyTaxDefinition);

        PropertyDateTimeDefinitionImpl propertySentDateDefinition = new PropertyDateTimeDefinitionImpl();
        propertySentDateDefinition.setId("cm:sentdate");
        propertySentDateDefinition.setDisplayName("Sentdate");
        propertySentDateDefinition.setQueryName("cm:sentdate");
        propertySentDateDefinition.setLocalName("sentdate");
        propertySentDateDefinition.setCardinality(Cardinality.SINGLE);
        propertySentDateDefinition.setPropertyType(PropertyType.DATETIME);
        propertySentDateDefinition.setUpdatability(Updatability.READWRITE);
        map.put("cm:sentdate", propertySentDateDefinition);

        PropertyStringDefinitionImpl propertyIdValueDefinition = new PropertyStringDefinitionImpl();
        propertyIdValueDefinition.setId("my:idvalue");
        propertyIdValueDefinition.setDisplayName("ID Value");
        propertyIdValueDefinition.setQueryName("my:idvalue");
        propertyIdValueDefinition.setLocalName("idvalue");
        propertyIdValueDefinition.setCardinality(Cardinality.SINGLE);
        propertyIdValueDefinition.setPropertyType(PropertyType.STRING);
        propertyIdValueDefinition.setUpdatability(Updatability.READWRITE);
        map.put("my:idvalue", propertyIdValueDefinition);

        PropertyStringDefinitionImpl propertyDescriptionDefinition = new PropertyStringDefinitionImpl();
        propertyDescriptionDefinition.setId("cm:description");
        propertyDescriptionDefinition.setDisplayName("Description");
        propertyDescriptionDefinition.setQueryName("cm:description");
        propertyDescriptionDefinition.setLocalName("description");
        propertyDescriptionDefinition.setCardinality(Cardinality.SINGLE);
        propertyDescriptionDefinition.setPropertyType(PropertyType.STRING);
        propertyDescriptionDefinition.setUpdatability(Updatability.READWRITE);
        map.put("cm:description", propertyDescriptionDefinition);

        return map;
    }

    private Map<String, PropertyDefinition<?>> getPropertyDefinitionMap() {
        Map<String, PropertyDefinition<?>> map = new HashMap<>();
        
        PropertyIdDefinitionImpl propertyIdDefinition = new PropertyIdDefinitionImpl();
        propertyIdDefinition.setId("cmis:objectId");
        propertyIdDefinition.setDisplayName("Object ID");
        propertyIdDefinition.setQueryName("cmis:objectId");
        propertyIdDefinition.setLocalName("objectId");
        propertyIdDefinition.setCardinality(Cardinality.SINGLE);
        propertyIdDefinition.setPropertyType(PropertyType.ID);
        propertyIdDefinition.setUpdatability(Updatability.READONLY);
        map.put("cmis:objectId", propertyIdDefinition);

        PropertyStringDefinitionImpl propertyPathDefinition = new PropertyStringDefinitionImpl();
        propertyPathDefinition.setId("cmis:path");
        propertyPathDefinition.setDisplayName("Path");
        propertyPathDefinition.setQueryName("cmis:path");
        propertyPathDefinition.setLocalName("path");
        propertyPathDefinition.setCardinality(Cardinality.SINGLE);
        propertyPathDefinition.setPropertyType(PropertyType.STRING);
        propertyPathDefinition.setUpdatability(Updatability.READONLY);
        map.put("cmis:path", propertyPathDefinition);

        PropertyStringDefinitionImpl propertyNameDefinition = new PropertyStringDefinitionImpl();
        propertyNameDefinition.setId("cmis:name");
        propertyNameDefinition.setDisplayName("Name");
        propertyNameDefinition.setQueryName("cmis:name");
        propertyNameDefinition.setLocalName("name");
        propertyNameDefinition.setCardinality(Cardinality.SINGLE);
        propertyNameDefinition.setPropertyType(PropertyType.STRING);
        propertyNameDefinition.setUpdatability(Updatability.READWRITE);
        map.put("cmis:name", propertyNameDefinition);

        PropertyIdDefinitionImpl propertyObjectTypeIdDefinition = new PropertyIdDefinitionImpl();
        propertyObjectTypeIdDefinition.setId("cmis:objectTypeId");
        propertyObjectTypeIdDefinition.setDisplayName("Object Type Id");
        propertyObjectTypeIdDefinition.setQueryName("cmis:objectTypeId");
        propertyObjectTypeIdDefinition.setLocalName("objectTypeId");
        propertyObjectTypeIdDefinition.setCardinality(Cardinality.SINGLE);
        propertyObjectTypeIdDefinition.setPropertyType(PropertyType.ID);
        propertyObjectTypeIdDefinition.setUpdatability(Updatability.READONLY);
        map.put("cmis:objectTypeId", propertyObjectTypeIdDefinition);

        PropertyIdDefinitionImpl propertyBaseTypeIdDefinition = new PropertyIdDefinitionImpl();
        propertyBaseTypeIdDefinition.setId("cmis:baseTypeId");
        propertyBaseTypeIdDefinition.setDisplayName("Base Type Id");
        propertyBaseTypeIdDefinition.setQueryName("cmis:baseTypeId");
        propertyBaseTypeIdDefinition.setLocalName("baseTypeId");
        propertyBaseTypeIdDefinition.setCardinality(Cardinality.SINGLE);
        propertyBaseTypeIdDefinition.setPropertyType(PropertyType.ID);
        propertyBaseTypeIdDefinition.setUpdatability(Updatability.READONLY);
        map.put("cmis:baseTypeId", propertyBaseTypeIdDefinition);

        PropertyIdDefinitionImpl propertyParentIdDefinition = new PropertyIdDefinitionImpl();
        propertyParentIdDefinition.setId("cmis:parentId");
        propertyParentIdDefinition.setDisplayName("Parent Id");
        propertyParentIdDefinition.setQueryName("cmis:parentId");
        propertyParentIdDefinition.setLocalName("parentId");
        propertyParentIdDefinition.setCardinality(Cardinality.SINGLE);
        propertyParentIdDefinition.setPropertyType(PropertyType.ID);
        propertyParentIdDefinition.setUpdatability(Updatability.READONLY);
        map.put("cmis:parentId", propertyParentIdDefinition);

        PropertyBooleanDefinitionImpl propertyIsVersionCheckedOutDefinition = new PropertyBooleanDefinitionImpl();
        propertyIsVersionCheckedOutDefinition.setId("cmis:isVersionSeriesCheckedOut");
        propertyIsVersionCheckedOutDefinition.setDisplayName("Is version Checked Out");
        propertyIsVersionCheckedOutDefinition.setQueryName("cmis:isVersionSeriesCheckedOut");
        propertyIsVersionCheckedOutDefinition.setLocalName("isVersionSeriesCheckedOut");
        propertyIsVersionCheckedOutDefinition.setCardinality(Cardinality.SINGLE);
        propertyIsVersionCheckedOutDefinition.setPropertyType(PropertyType.BOOLEAN);
        propertyIsVersionCheckedOutDefinition.setUpdatability(Updatability.READONLY);
        map.put("cmis:isVersionSeriesCheckedOut", propertyIsVersionCheckedOutDefinition);

        PropertyBooleanDefinitionImpl propertyIsPrivateWorkingCopyDefinition = new PropertyBooleanDefinitionImpl();
        propertyIsPrivateWorkingCopyDefinition.setId("cmis:isPrivateWorkingCopy");
        propertyIsPrivateWorkingCopyDefinition.setDisplayName("Is private working copy");
        propertyIsPrivateWorkingCopyDefinition.setQueryName("cmis:isPrivateWorkingCopy");
        propertyIsPrivateWorkingCopyDefinition.setLocalName("isPrivateWorkingCopy");
        propertyIsPrivateWorkingCopyDefinition.setCardinality(Cardinality.SINGLE);
        propertyIsPrivateWorkingCopyDefinition.setPropertyType(PropertyType.BOOLEAN);
        propertyIsPrivateWorkingCopyDefinition.setUpdatability(Updatability.READONLY);
        map.put("cmis:isPrivateWorkingCopy", propertyIsPrivateWorkingCopyDefinition);

        PropertyStringDefinitionImpl propertyVersionLabelDefinition = new PropertyStringDefinitionImpl();
        propertyVersionLabelDefinition.setId("cmis:versionLabel");
        propertyVersionLabelDefinition.setDisplayName("Version Label");
        propertyVersionLabelDefinition.setQueryName("cmis:versionLabel");
        propertyVersionLabelDefinition.setLocalName("versionLabel");
        propertyVersionLabelDefinition.setCardinality(Cardinality.SINGLE);
        propertyVersionLabelDefinition.setPropertyType(PropertyType.STRING);
        propertyVersionLabelDefinition.setUpdatability(Updatability.READONLY);
        map.put("cmis:versionLabel", propertyVersionLabelDefinition);

        PropertyBooleanDefinitionImpl propertyIsMajorVersionDefinition = new PropertyBooleanDefinitionImpl();
        propertyIsMajorVersionDefinition.setId("cmis:isMajorVersion");
        propertyIsMajorVersionDefinition.setDisplayName("Is Major Version");
        propertyIsMajorVersionDefinition.setQueryName("cmis:isMajorVersion");
        propertyIsMajorVersionDefinition.setLocalName("isMajorVersion");
        propertyIsMajorVersionDefinition.setCardinality(Cardinality.SINGLE);
        propertyIsMajorVersionDefinition.setPropertyType(PropertyType.BOOLEAN);
        propertyIsMajorVersionDefinition.setUpdatability(Updatability.READONLY);
        map.put("cmis:isMajorVersion", propertyIsMajorVersionDefinition);

        PropertyStringDefinitionImpl propertyCheckinCommentDefinition = new PropertyStringDefinitionImpl();
        propertyCheckinCommentDefinition.setId("cmis:checkinComment");
        propertyCheckinCommentDefinition.setDisplayName("Checkin Comment");
        propertyCheckinCommentDefinition.setQueryName("cmis:checkinComment");
        propertyCheckinCommentDefinition.setLocalName("checkinComment");
        propertyCheckinCommentDefinition.setCardinality(Cardinality.SINGLE);
        propertyCheckinCommentDefinition.setPropertyType(PropertyType.STRING);
        propertyCheckinCommentDefinition.setUpdatability(Updatability.READONLY);
        map.put("cmis:checkinComment", propertyCheckinCommentDefinition);

        PropertyStringDefinitionImpl propertyContentStreamMimeTypeDefinition = new PropertyStringDefinitionImpl();
        propertyContentStreamMimeTypeDefinition.setId("cmis:contentStreamMimeType");
        propertyContentStreamMimeTypeDefinition.setDisplayName("Content Stream MIME Type");
        propertyContentStreamMimeTypeDefinition.setQueryName("cmis:contentStreamMimeType");
        propertyContentStreamMimeTypeDefinition.setLocalName("contentStreamMimeType");
        propertyContentStreamMimeTypeDefinition.setCardinality(Cardinality.SINGLE);
        propertyContentStreamMimeTypeDefinition.setPropertyType(PropertyType.STRING);
        propertyContentStreamMimeTypeDefinition.setUpdatability(Updatability.READONLY);

        map.put("cmis:contentStreamMimeType", propertyContentStreamMimeTypeDefinition);
        PropertyIdDefinitionImpl propertySecondaryObjectTypeIdDefinition = new PropertyIdDefinitionImpl();
        propertySecondaryObjectTypeIdDefinition.setId("cmis:secondaryObjectTypeIds");
        propertySecondaryObjectTypeIdDefinition.setDisplayName("Secondary Object Type Id");
        propertySecondaryObjectTypeIdDefinition.setQueryName("cmis:secondaryObjectTypeIds");
        propertySecondaryObjectTypeIdDefinition.setLocalName("secondaryObjectTypeIds");
        propertySecondaryObjectTypeIdDefinition.setCardinality(Cardinality.MULTI);
        propertySecondaryObjectTypeIdDefinition.setPropertyType(PropertyType.ID);
        propertySecondaryObjectTypeIdDefinition.setUpdatability(Updatability.READWRITE);
        map.put("cmis:secondaryObjectTypeIds", propertySecondaryObjectTypeIdDefinition);

        PropertyStringDefinitionImpl propertyPersonDefinition = new PropertyStringDefinitionImpl();
        propertyPersonDefinition.setId("my:person");
        propertyPersonDefinition.setDisplayName("Person");
        propertyPersonDefinition.setQueryName("my:person");
        propertyPersonDefinition.setLocalName("person");
        propertyPersonDefinition.setCardinality(Cardinality.SINGLE);
        propertyPersonDefinition.setPropertyType(PropertyType.STRING);
        propertyPersonDefinition.setUpdatability(Updatability.READWRITE);
        map.put("my:person", propertyPersonDefinition);

        PropertyDateTimeDefinitionImpl propertyDocumentDateDefinition = new PropertyDateTimeDefinitionImpl();
        propertyDocumentDateDefinition.setId("my:documentDate");
        propertyDocumentDateDefinition.setDisplayName("Documentdate");
        propertyDocumentDateDefinition.setQueryName("my:documentDate");
        propertyDocumentDateDefinition.setLocalName("documentDate");
        propertyDocumentDateDefinition.setCardinality(Cardinality.SINGLE);
        propertyDocumentDateDefinition.setPropertyType(PropertyType.DATETIME);
        propertyDocumentDateDefinition.setUpdatability(Updatability.READWRITE);
        map.put("my:documentDate", propertyDocumentDateDefinition);

        PropertyDateTimeDefinitionImpl propertyCreateDateDefinition = new PropertyDateTimeDefinitionImpl();
        propertyCreateDateDefinition.setId("cmis:creationDate");
        propertyCreateDateDefinition.setDisplayName("Creation Date");
        propertyCreateDateDefinition.setQueryName("cmis:creationDate");
        propertyCreateDateDefinition.setLocalName("creationDate");
        propertyCreateDateDefinition.setCardinality(Cardinality.SINGLE);
        propertyCreateDateDefinition.setPropertyType(PropertyType.DATETIME);
        propertyCreateDateDefinition.setUpdatability(Updatability.READONLY);
        map.put("cmis:creationDate", propertyCreateDateDefinition);

        PropertyDateTimeDefinitionImpl propertyLastModificationDateDefinition = new PropertyDateTimeDefinitionImpl();
        propertyLastModificationDateDefinition.setId("cmis:lastModificationDate");
        propertyLastModificationDateDefinition.setDisplayName("Last Modified Date");
        propertyLastModificationDateDefinition.setQueryName("cmis:lastModificationDate");
        propertyLastModificationDateDefinition.setLocalName("lastModificationDate");
        propertyLastModificationDateDefinition.setCardinality(Cardinality.SINGLE);
        propertyLastModificationDateDefinition.setPropertyType(PropertyType.DATETIME);
        propertyLastModificationDateDefinition.setUpdatability(Updatability.READONLY);
        map.put("cmis:lastModificationDate", propertyLastModificationDateDefinition);
        return map;
    }


    private FileableCmisObject createFileableCmisObject(Map<String, Object> props, String path, String name, ObjectType objectType, String mimeType) {
        FileableCmisObject fileableCmisObject;
        String parentId;
        String objectId = repository.getId();
        PropertiesImpl properties;
        ObjectDataImpl objectData = new ObjectDataImpl();
        if (props == null)
            properties = new PropertiesImpl();
        else
            properties = (PropertiesImpl) convertProperties(props);
        if (!properties.getProperties().containsKey("cmis:objectId")) {

            properties.addProperty(fillProperty("cmis:objectId", objectId));
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
                repository.setRootId(objectId);
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
            } catch (InterruptedException e) {
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

    @Override
    public Session generateSession() {

        return sessionImpl;
    }

    private SessionFactory mockSessionFactory() {

        SessionFactory sessionFactory = mock(SessionFactory.class);
        when(sessionFactory.createSession(null)).thenThrow(new NullPointerException("Parameters null"));
        when(sessionFactory.createSession(new HashMap<>())).thenReturn(sessionImpl);
        when(sessionFactory.getRepositories(null)).thenThrow(new NullPointerException("Parameters null"));
        return sessionFactory;
    }

    private SessionImpl mockSession() {
        SessionImpl session = mock(SessionImpl.class);
        when(session.getBinding()).thenReturn(binding);
        when(session.getRepositoryInfo()).thenReturn(repositoryInfo);

        when(session.getObjectFactory()).thenReturn(objectFactory);
        when(session.createOperationContext()).thenCallRealMethod();
        when(session.getDefaultContext()).thenReturn(new OperationContextImpl(null, false, true, false,
                IncludeRelationships.NONE, null, true, null, true, 100));

        when(session.createQueryStatement(any(String.class))).then(new Answer<QueryStatement>() {
            public QueryStatement answer(InvocationOnMock invocation) throws Throwable {
                return new QueryStatementImpl(sessionImpl, (String) invocation.getArguments()[0]);
            }
        });
        when(session.query(any(String.class), any(Boolean.class), any(OperationContext.class))).thenCallRealMethod();
        when(session.query(any(String.class), any(Boolean.class))).thenCallRealMethod();
        when(session.queryObjects(anyString(), anyString(), anyBoolean(),any(OperationContext.class))).thenCallRealMethod();

        when(session.createDocument(anyMap(), any(ObjectId.class), any(ContentStream.class), any(VersioningState.class))).thenAnswer(new Answer<ObjectId>() {

            public ObjectId answer(InvocationOnMock invocation) throws Throwable {
                return createFileableCmisObject(invocation, false);
            }
        });
        when(session.createDocument(anyMap(), any(ObjectId.class), any(ContentStream.class), any(VersioningState.class), anyList(), anyList(), anyList())).thenAnswer(new Answer<ObjectId>() {

            public ObjectId answer(InvocationOnMock invocation) throws Throwable {
                return createFileableCmisObject(invocation, false);
            }
        });
        when(session.createFolder(anyMap(), any(ObjectId.class), anyList(), anyList(), anyList())).thenAnswer(new Answer<ObjectId>() {

            public ObjectId answer(InvocationOnMock invocation) throws Throwable {

                return createFileableCmisObject(invocation, true);
            }
        });
        when(session.createFolder(anyMap(), any(ObjectId.class))).thenAnswer(new Answer<ObjectId>() {

            public ObjectId answer(InvocationOnMock invocation) throws Throwable {

                return createFileableCmisObject(invocation, true);
            }
        });
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) throws Throwable {
                String objectId = ((ObjectId) invocation.getArguments()[0]).getId();
                FileableCmisObject cmisObject = repository.getById(objectId);
                repository.delete(cmisObject);
                return null;
            }

        }).when(session).delete(any(ObjectId.class), anyBoolean());
        when(session.deleteTree(any(ObjectId.class), anyBoolean(), any(UnfileObject.class), anyBoolean())).thenAnswer(new Answer<List<String>>() {
            public List<String> answer(InvocationOnMock invocation) throws Throwable {
                String objectId = ((ObjectId) invocation.getArguments()[0]).getId();
                FileableCmisObject cmisObject = repository.getById(objectId);
                return repository.deleteTree(cmisObject);
            }
        });
        when(session.createObjectId(any(String.class))).thenAnswer(new Answer<ObjectId>() {

            public ObjectId answer(InvocationOnMock invocation) throws Throwable {
                return new ObjectIdImpl((String) invocation.getArguments()[0]);
            }
        });
        when(session.getObject(any(ObjectId.class))).thenAnswer(new Answer<FileableCmisObject>() {
            public FileableCmisObject answer(InvocationOnMock invocation) throws Throwable {
                return repository.getById(((ObjectId) invocation.getArguments()[0]).getId());
            }
        });
        when(session.getObject(any(String.class), any(OperationContext.class))).thenAnswer(new Answer<FileableCmisObject>() {
            public FileableCmisObject answer(InvocationOnMock invocation) throws Throwable {
                return repository.getById((String) invocation.getArguments()[0]);
            }
        });
        when(session.getObject(any(ObjectId.class), any(OperationContext.class))).thenAnswer(new Answer<FileableCmisObject>() {
            public FileableCmisObject answer(InvocationOnMock invocation) throws Throwable {
                return repository.getById(((ObjectId) invocation.getArguments()[0]).getId());
            }
        });
        when(session.getObjectByPath(any(String.class))).thenAnswer(new Answer<FileableCmisObject>() {
            public FileableCmisObject answer(InvocationOnMock invocation) throws Throwable {
                FileableCmisObject cmisObject = repository.getByPath((String) invocation.getArguments()[0]);
                if (cmisObject == null)
                    throw new CmisObjectNotFoundException((String) invocation.getArguments()[0] + " not found!");
                return cmisObject;
            }
        });
        when(session.getObjectByPath(any(String.class), any(OperationContext.class))).thenAnswer(new Answer<FileableCmisObject>() {
            public FileableCmisObject answer(InvocationOnMock invocation) throws Throwable {
                FileableCmisObject cmisObject = repository.getByPath((String) invocation.getArguments()[0]);
                if (cmisObject == null)
                    throw new CmisObjectNotFoundException((String) invocation.getArguments()[0] + " not found!");
                return cmisObject;
            }
        });
        when(session.getContentStream(any(ObjectId.class), any(String.class), any(BigInteger.class), any(BigInteger.class))).thenAnswer(new Answer<ContentStream>() {
            public ContentStream answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Document document = (Document) args[0];
                return repository.getContent(document);
            }
        });
        when(session.getTypeDefinition(any(String.class))).thenAnswer(new Answer<ObjectType>() {
            public ObjectType answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String string = (String) args[0];
                if (string.equalsIgnoreCase("cmis:document"))
                    return documentType;
                else if (string.equalsIgnoreCase("cmis:folder"))
                    return folderType;
                else if (string.contains("my:archivContent"))
                    return archivType;
                else if (string.startsWith("P:"))
                    return secondaryTypeStore.get(string);
                else
                return documentType;
            }
        });
        when(session.getTypeDefinition(any(String.class), anyBoolean())).thenAnswer(new Answer<ObjectType>() {
            public ObjectType answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String string = (String) args[0];
                if (string.equalsIgnoreCase("cmis:document"))
                    return documentType;
                else if (string.equalsIgnoreCase("cmis:folder"))
                    return folderType;
                else if (string.contains("my:archivContent"))
                    return archivType;
                else if (string.startsWith("P:"))
                    return secondaryTypeStore.get(string);
                return documentType;
            }
        });
        return session;
    }

    private ObjectId createFileableCmisObject(InvocationOnMock invocation, boolean folder) {
        boolean majorVersion = false;
        ObjectType objectType;
        Object[] args = invocation.getArguments();
        Map<String, Object> props = (Map<String, Object>) args[0];

        if (props.get(PropertyIds.OBJECT_TYPE_ID) == null) {
            throw new IllegalArgumentException();
        }
        if (props.get(PropertyIds.NAME) == null) {
            throw new IllegalArgumentException();
        }
        String objectTypeName = (String) props.get(PropertyIds.OBJECT_TYPE_ID);
        if (objectTypeName.contains(BaseTypeId.CMIS_DOCUMENT.value()))
            objectType = documentType;
        else if (objectTypeName.contains(BaseTypeId.CMIS_FOLDER.value()))
            objectType = folderType;
        else
            objectType = archivType;
        String name = (String) props.get(PropertyIds.NAME);
        FileableCmisObject cmis = (FileableCmisObject) args[1];
        String path = cmis.getPaths().get(0) ;
        FileableCmisObject newObject;
        if (!folder && invocation.getArguments().length > 2 && ((VersioningState) invocation.getArguments()[3]).equals(VersioningState.MAJOR))
           props.put("cmis:versionLabel", "1.0");
        else
            props.put("cmis:versionLabel", "0.1");
        if (!folder) {
            newObject = createFileableCmisObject(props, path, name,  objectType,  ((ContentStream) invocation.getArguments()[2]).getMimeType());
            repository.insert(path, newObject, (ContentStream) invocation.getArguments()[2]);
        }
        else {
            newObject = createFileableCmisObject(props, path, name, objectType, null);
            repository.insert(path, newObject);
        }

        return new ObjectIdImpl(newObject.getId());
    }


    private CollectionIterable<FileableCmisObject> mockCollectionIterable() {
        CollectionIterable<FileableCmisObject> collectionIterable = mock(CollectionIterable.class);
        return collectionIterable;
    }


    private CmisBinding mockBinding() {
        CmisBinding binding = mock(CmisBinding.class);
        when(binding.getObjectFactory()).thenReturn(new BindingsObjectFactoryImpl());
        when(binding.getObjectService()).thenReturn(objectService);
        when(binding.getNavigationService()).then(new Answer<NavigationService>() {
            public NavigationService answer(InvocationOnMock invocation) throws Throwable {
                NavigationService navigationService = mock(NavigationService.class);
                when(navigationService.getFolderParent(any(String.class), any(String.class), any(String.class), any(ExtensionsData.class))).then(new Answer<ObjectData>() {
                    public ObjectData answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        String id = (String) args[1];
                        FileableCmisObject result = null;
                        FileableCmisObject cmisObject = repository.getParent(id);
                        if (cmisObject != null)
                            return getObjectDataFromCmisObject(cmisObject);
                        else
                            return null;
                    }
                });
                when(navigationService.getObjectParents(anyString(), anyString(), anyString(), anyBoolean(), any(IncludeRelationships.class), anyString(), anyBoolean(), any(ExtensionsData.class))).then(new Answer<List<ObjectParentData>>() {
                    public List<ObjectParentData> answer(InvocationOnMock invocation) throws Throwable {
                        List<ObjectParentData> result = new ArrayList<>();
                        FileableCmisObject cmisObject = repository.getById((String) invocation.getArguments()[1]);
                        FileableCmisObject parentObject = repository.getParent((String) invocation.getArguments()[1]);
                        ObjectParentDataImpl objectData = new ObjectParentDataImpl();
                        objectData.setObject(getObjectDataFromCmisObject(parentObject));
                        if (cmisObject.getType() instanceof FolderType)
                            objectData.setRelativePathSegment(parentObject.getPropertyValue("cmis:path"));
                        else
                            objectData.setRelativePathSegment(cmisObject.getName());

                        result.add(objectData);
                        return result;
                    }
                });
                when(navigationService.getChildren(anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(IncludeRelationships.class), anyString(), anyBoolean(), any(BigInteger.class), any(BigInteger.class), any(ExtensionsData.class))).then(new Answer<ObjectInFolderList>() {
                    public ObjectInFolderList answer(InvocationOnMock invocation) throws Throwable {
                        ObjectInFolderListImpl objectInFolderList = new ObjectInFolderListImpl();
                        BigInteger skip = (BigInteger) invocation.getArguments()[9];
                        BigInteger maxItems = (BigInteger) invocation.getArguments()[8];
                        List<ObjectInFolderData> folderDatas = new ArrayList<>();
                        List<FileableCmisObject> results = repository.getChildren((String) invocation.getArguments()[1], ((BigInteger) invocation.getArguments()[9]).intValue(), ((BigInteger) invocation.getArguments()[8]).intValue());
                        for (FileableCmisObject cmisObject : results) {
                            ObjectInFolderDataImpl objectInFolderData = new ObjectInFolderDataImpl();
                            objectInFolderData.setObject(getObjectDataFromCmisObject(cmisObject));
                            objectInFolderData.setPathSegment(cmisObject.getPropertyValue("cmis:path"));
                            folderDatas.add(objectInFolderData);
                        }
                        if (invocation.getArguments()[3] != null) {
                            String[] order = new String[2];
                            String[] sortColumns = invocation.getArguments()[3].toString().split(",");
                            Collections.sort(folderDatas, new Comparator<ObjectInFolderData>() {
                                @Override
                                public int compare(ObjectInFolderData o1, ObjectInFolderData o2) {
                                    int ret = 0;
                                    for (int j = 0; j < sortColumns.length; j++) {
                                        String[] column = sortColumns[j].trim().split(" ");
                                        order[0] = column[0];
                                        order[1] = column.length > 1 ? !column[1].isEmpty() ? column[1] : "ASC" : "ASC";
                                        Comparable valA = null, valB = null;
                                        for (int i = 0; i < o1.getObject().getProperties().getPropertyList().size(); i++) {
                                            if (((AbstractPropertyData) o1.getObject().getProperties().getPropertyList().get(i)).getId().equalsIgnoreCase(order[0])) {
                                                valA = (Comparable) o1.getObject().getProperties().getPropertyList().get(i).getFirstValue();
                                                break;
                                            }
                                        }
                                        for (int i = 0; i < o2.getObject().getProperties().getPropertyList().size(); i++) {
                                            if (((AbstractPropertyData) o2.getObject().getProperties().getPropertyList().get(i)).getId().equalsIgnoreCase(order[0])) {
                                                valB = (Comparable) o2.getObject().getProperties().getPropertyList().get(i).getFirstValue();
                                                break;
                                            }
                                        }
                                        if (valA != null && valB != null)
                                            ret = valA.compareTo(valB) * (order[1].equalsIgnoreCase("ASC") ? 1 : -1);
                                        else if (valA == null && valB == null)
                                            ret = 0;
                                        else if (valA == null)
                                            ret = order[1].equalsIgnoreCase("ASC") ? 1 : 1;
                                        else if (valB == null)
                                            ret = order[1].equalsIgnoreCase("ASC") ? -1 : -1 ;
                                        if (ret != 0)
                                            break;
                                    }
                                    return ret;
                                }
                            });
                        }
                        objectInFolderList.setObjects(folderDatas.subList(skip.intValue(), skip.intValue() + maxItems.intValue() > folderDatas.size() ? folderDatas.size() :  skip.intValue() + maxItems.intValue()));
                        objectInFolderList.setNumItems(BigInteger.valueOf(folderDatas.size()));
                        objectInFolderList.setHasMoreItems(skip.intValue() + maxItems.intValue() > folderDatas.size() ? false : true);
                        return objectInFolderList;
                    }
                });
                return navigationService;
            }
        });
        when(binding.getDiscoveryService()).then(new Answer<DiscoveryService>() {
            public DiscoveryService answer(InvocationOnMock invocation) throws Throwable {
                DiscoveryService discoveryService = mock(DiscoveryService.class);
                when(discoveryService.query(any(String.class), any(String.class), any(Boolean.class), any(Boolean.class), any(IncludeRelationships.class), any(String.class), any(BigInteger.class), any(BigInteger.class), any(ExtensionsData.class))).then(new Answer<ObjectList>() {

                    class ObjectTypeHelper {
                        boolean isObjectType(FileableCmisObject cmisObject, String objectTypeId){
                            if (objectTypeId == null)
                                return true;
                            ObjectType objectType = cmisObject.getType();
                            do {
                                if (objectType.getId().contains(objectTypeId))
                                    return true;
                                objectType = objectType.getParentType();
                            } while(objectType != null);
                            return false;
                        }

                        boolean isObjectInSearch(FileableCmisObject cmisObject, List<String> contains) {
                            if (contains == null || contains.isEmpty())
                                return true;
                            else {

                                for (String x: contains){
                                    List<String> liste = Arrays.asList(new StringBuilder(x).reverse().toString().split(":", 2)).stream().map(element -> new StringBuilder(element).reverse().toString()).collect(Collectors.toList());
                                    Collections.reverse(liste);
                                    if (!liste.get(0).contains("TEXT") && cmisObject.getPropertyValue(liste.get(0)).toString().contains(liste.get(1)))
                                        return true;
                                    if (liste.get(0).contains("TEXT") && cmisObject instanceof Document &&  ((Document) cmisObject).getContentStream() != null )   {
                                        byte[] content = new byte[0];
                                        try {
                                            content = IOUtils.toByteArray(((Document) cmisObject).getContentStream().getStream());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        if ( ((Document) cmisObject).getContentStream().getMimeType().contains("text/plain") && new String(content).contains(liste.get(1)))
                                            return true;
                                        if ( ((Document) cmisObject).getContentStream().getMimeType().contains("application/pdf")) {
                                            PDFConnector con = new PDFConnector();
                                            if (con.pdftoText(new ByteArrayInputStream(content)).contains(liste.get(1)))
                                                return true;
                                        }
                                    }
                                }
                            }

                            return false;
                        }
                    }

                    public ObjectList answer(InvocationOnMock invocation) throws Throwable {
                        int typen = 0;
                        int i = 0;
                        ObjectTypeHelper helper = new ObjectTypeHelper();
                        List<FileableCmisObject> liste;
                        List<String> contains = new ArrayList<>();
                        ObjectListImpl objectList = new ObjectListImpl();
                        Object[] args = invocation.getArguments();
                        String statement = (String) args[1];
                        BigInteger skip = (BigInteger) args[7];
                        BigInteger maxItems = (BigInteger) args[6];
                        List<ObjectData> list = new ArrayList<>();
                        final String search = statement.substring(statement.indexOf("'") + 1, statement.indexOf("'", statement.indexOf("'") + 1));
                        String typ = null;
                        List stmt = Arrays.asList(statement.split(" "));
                        Iterator<String> stmtIt = stmt.iterator();
                        while (stmtIt.hasNext()) {
                            String part = stmtIt.next();
                            if (part.contains("CONTAINS"))
                                contains.add(stmtIt.next().replaceAll("[']|[)]|[)]|[*]", ""));
                            if (part.contains("TEXT:"))
                                contains.add(part.replaceAll("[']|[)]|[)]|[*]", ""));
                        }

                        if (statement.contains("from ")) {
                            typ = (String) stmt.get(stmt.indexOf("from") + 1);
                        }

                        if (statement.contains("IN_FOLDER")) {
                            liste = repository.getChildren(search);
                        } else if (statement.contains("IN_TREE")) {
                            liste = repository.getChildrenForAllLevels(search);
                        } else {
                            liste = repository.query(search);
                        }

                        for (FileableCmisObject cmisObject : liste) {
                            
                            if (helper.isObjectType(cmisObject, typ) && helper.isObjectInSearch(cmisObject, contains))
                                list.add(getObjectDataFromCmisObject(cmisObject));
                        }

                        if (statement.contains("ORDER BY")) {
                            String[] order = new String[2];
                            String parts = statement.substring(statement.indexOf("ORDER BY") + 9);
                            final String[] sortColumns = parts.split(",");
                            Collections.sort(list, new Comparator<ObjectData>() {
                                @Override
                                public int compare(ObjectData o1, ObjectData o2) {
                                    int ret = 0;
                                    for (int j = 0; j < sortColumns.length; j++) {
                                        String[] column = sortColumns[j].trim().split(" ");
                                        order[0] = column[0];
                                        order[1] = column.length > 1 ? !column[1].isEmpty() ? column[1] : "ASC" : "ASC";
                                        Comparable valA = null, valB = null;
                                        for (int i = 0; i < o1.getProperties().getPropertyList().size(); i++) {
                                            if (((AbstractPropertyData) o1.getProperties().getPropertyList().get(i)).getId().equalsIgnoreCase(order[0])) {
                                                valA = (Comparable) o1.getProperties().getPropertyList().get(i).getFirstValue();
                                                break;
                                            }
                                        }
                                        for (int i = 0; i < o2.getProperties().getPropertyList().size(); i++) {
                                            if (((AbstractPropertyData) o2.getProperties().getPropertyList().get(i)).getId().equalsIgnoreCase(order[0])) {
                                                valB = (Comparable) o2.getProperties().getPropertyList().get(i).getFirstValue();
                                                break;
                                            }
                                        }
                                        if (valA != null && valB != null)
                                            ret = valA.compareTo(valB) * (order[1].equalsIgnoreCase("ASC") ? 1 : -1);
                                        else if (valA == null && valB == null)
                                            ret = 0;
                                        else if (valA == null)
                                            ret = order[1].equalsIgnoreCase("ASC") ? 1 : 1;
                                        else if (valB == null)
                                            ret = order[1].equalsIgnoreCase("ASC") ? -1 : -1;
                                        if (ret != 0)
                                            break;
                                    }
                                    return ret;
                                }
                            });
                        }
                        objectList.setObjects(list.subList(skip.intValue(), skip.intValue() + maxItems.intValue() > list.size() ? list.size() : skip.intValue() + maxItems.intValue()));
                        objectList.setNumItems(BigInteger.valueOf(list.size()));
                        objectList.setHasMoreItems(skip.intValue() + maxItems.intValue() > list.size() ? false : true);
                        return objectList;
                    }
                });
                return discoveryService;
            }
        });
        when(binding.getVersioningService()).then(new Answer<VersioningService>() {
            public VersioningService answer(InvocationOnMock invocation) throws Throwable {
                VersioningService versioningService = mock(VersioningService.class);
                doAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        Holder<String> holder = (Holder) invocation.getArguments()[1];
                        Document document = (Document) repository.getById(holder.getValue());
                        if (document.isPrivateWorkingCopy()) throw new CmisVersioningException();
                        ((PropertyImpl) document.getProperty("cmis:isPrivateWorkingCopy")).setValue(true);
                        return null;
                    }
                }).when(versioningService).checkOut(anyString(), any(Holder.class), any(ExtensionsData.class), any(Holder.class));
                doAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        Holder<String> holder = (Holder) invocation.getArguments()[1];
                        boolean major = (boolean) invocation.getArguments()[2];
                        Properties properties = (Properties) invocation.getArguments()[3];
                        ContentStream stream = (ContentStream) invocation.getArguments()[4];
                        String checkinComment = (String) invocation.getArguments()[5];
                        Document document = (Document) repository.getById(holder.getValue());
                        if (!document.isPrivateWorkingCopy()) throw new CmisVersioningException();
                        if (properties != null && properties.getProperties() != null && !properties.getProperties().isEmpty()) {
                            document.updateProperties(properties.getProperties());
                        }
                        if (stream != null) {
                            repository.changeContent(document, stream);
                        }
                        ((PropertyImpl) document.getProperty("cmis:isPrivateWorkingCopy")).setValue(false);
                        if (major)
                            ((PropertyImpl) document.getProperty("cmis:versionLabel")).setValue(new BigDecimal(document.getProperty("cmis:versionLabel").getValueAsString()).add(new BigDecimal("1")).toString());
                        else
                            ((PropertyImpl) document.getProperty("cmis:versionLabel")).setValue(new BigDecimal(document.getProperty("cmis:versionLabel").getValueAsString()).add(new BigDecimal("0.1")).toString());
                        if (checkinComment != null && !checkinComment.isEmpty())
                            ((PropertyImpl) document.getProperty("cmis:checkinComment")).setValue(checkinComment);
                        ((PropertyImpl) document.getProperty("cmis:lastModificationDate")).setValue( copyDateTimeValue(new Date().getTime()));
                        return null;
                    }
                }).when(versioningService).checkIn(anyString(), any(Holder.class), anyBoolean(), any(Properties.class), any(ContentStream.class), anyString(), anyListOf(String.class), any(Acl.class), any(Acl.class), any(ExtensionsData.class));
                return versioningService;
            }
        });
        return binding;
    }

    private ObjectServiceImpl mockObjectService() {
        ObjectServiceImpl objectService = mock(ObjectServiceImpl.class);
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Holder<String> holder = (Holder) invocation.getArguments()[1];
                String objectId = holder.getValue();
                ContentStreamImpl stream = (ContentStreamImpl) repository.getContent(new ObjectIdImpl(objectId));
                stream.setStream(((ContentStream) invocation.getArguments()[4]).getStream());
                return null;
            }

        }).when(objectService).setContentStream(anyString(), any(Holder.class), anyBoolean(), any(Holder.class), any(ContentStream.class), any(ExtensionsData.class));
        when(objectService.getObject(anyString(), anyString(), anyString(), anyBoolean(), any(IncludeRelationships.class), anyString(), anyBoolean(), anyBoolean(), any(ExtensionsData.class))).thenAnswer(new Answer<ObjectData>() {
            public ObjectData answer(InvocationOnMock invocation) throws Throwable {
                String objectId = (String) invocation.getArguments()[1];
                return getObjectDataFromCmisObject(repository.getById(objectId));
            }
        });
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                FileableCmisObject cmisObjectNew;
                String objectId = (String) ((Holder) invocation.getArguments()[1]).getValue();
                FileableCmisObject cmisObject = repository.getById(objectId);
                ObjectDataImpl objectData = (ObjectDataImpl) getObjectDataFromCmisObject(cmisObject);
                for (PropertyData property : ((Properties) invocation.getArguments()[3]).getPropertyList()) {
                    PropertiesImpl properties = (PropertiesImpl) objectData.getProperties();
                    properties.addProperty(property);
                }

                if (cmisObject.getType() instanceof FolderType)
                    cmisObjectNew = new FolderImpl(sessionImpl, cmisObject.getType(), objectData, new OperationContextImpl());
                else {

                    cmisObjectNew = new DocumentImpl(sessionImpl, cmisObject.getType(), objectData, new OperationContextImpl());
                    ((PropertyImpl) cmisObjectNew.getProperty("cmis:lastModificationDate")).setValue( copyDateTimeValue(new Date().getTime()));
                }
                repository.update(cmisObject, cmisObjectNew);
                return null;
            }
        }).when(objectService).updateProperties(anyString(), any(Holder.class), any(Holder.class), any(Properties.class), any(ExtensionsData.class));
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                String objectId = (String) ((Holder) invocation.getArguments()[1]).getValue();
                String targetFolderId = (String) invocation.getArguments()[2];
                String sourceFolderId = (String) invocation.getArguments()[3];
                FileableCmisObject cmisObject = repository.getById(objectId);
                repository.move(targetFolderId, cmisObject);
                for (Property property : cmisObject.getProperties()) {
                    if (property.getId().equalsIgnoreCase("cmis:parentId")) {
                        ((PropertyImpl) property).setValue(targetFolderId);
                        break;
                    }
                }
                return null;
            }
        }).when(objectService).moveObject(anyString(), any(Holder.class), anyString(), anyString(), any(ExtensionsData.class));
/*        when(objectService.createFolder(anyString(), any(Properties.class), anyString(), anyListOf(String.class), any(Acl.class), any(Acl.class), any(ExtensionsData.class))).thenAnswer(new Answer<ObjectId>() {

            public ObjectId answer(InvocationOnMock invocation) throws Throwable {

                return createFileableCmisObject(invocation, true);
            }
        });*/
        return objectService;
    }

    private ObjectData getObjectDataFromCmisObject(FileableCmisObject cmisObject) {
        if (cmisObject == null)
            throw new RuntimeException("cmisObject must be set!");
        AbstractPropertyData propertyData = null;
        ObjectDataImpl objectData = new ObjectDataImpl();
        Collection<PropertyData<?>> list = new ArrayList<PropertyData<?>>();
        for (Property property : cmisObject.getProperties()) {
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

    private BindingsObjectFactory mockBindingsObjectFactory() {
        BindingsObjectFactory bindingsObjectFactory = mock(BindingsObjectFactory.class);
        when(bindingsObjectFactory.createContentStream(any(String.class), any(BigInteger.class), any(String.class), any(InputStream.class))).thenAnswer(
                new Answer<ContentStream>() {

                    public ContentStream answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();

                        String filename = (String) args[0];
                        BigInteger length = (BigInteger) args[1];
                        String mimetype = (String) args[2];
                        InputStream stream = (InputStream) args[3];

                        return new ContentStreamImpl(filename, length, mimetype, stream);
                    }
                });
        return bindingsObjectFactory;
    }


    private RepositoryInfo mockRepositoryInfo() {
        RepositoryInfo info = mock(RepositoryInfo.class);
        when(info.getId()).thenReturn("0");
        when(info.getRootFolderId()).thenReturn(repository.getRootId());
        return info;
    }

    private  Properties convertProperties(final Map<String, Object> props) {

        PropertiesImpl result = new PropertiesImpl();
        PropertyDefinition<?> definition;

        for (String id : props.keySet()) {

            result.addProperty(fillProperty(id, props.get(id)));

        }

        return result;
    }

    private AbstractPropertyData<?> fillProperty(String id, Object value) {

        PropertyDefinition<?> definition;
        AbstractPropertyData<?> property = null;


            if (!propertyDefinitionMap.containsKey(id) && !secondaryPropertyDefinitionMap.containsKey(id))
                throw new CmisRuntimeException(("Invalid properties " + id));
            if (propertyDefinitionMap.containsKey(id))
                definition = propertyDefinitionMap.get(id);
            else
                definition = secondaryPropertyDefinitionMap.get(id);

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

    private List<String> copyStringValues(List<Object> source) {
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

    private String copyStringValue(Object source) {
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

    private Boolean copyBooleanValue(Object source) {
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

    private BigInteger copyIntegerValue(Object source) {
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

    private BigDecimal copyDecimalValue(Object source) {
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

    private GregorianCalendar copyDateTimeValue(Object source) {
        GregorianCalendar result = null;
        if (source != null) {
            GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                if (source instanceof Number) {
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


}
