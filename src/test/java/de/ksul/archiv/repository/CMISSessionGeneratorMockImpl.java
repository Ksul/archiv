package de.ksul.archiv.repository;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.query.parser.cqn.CQNParser;
import com.googlecode.cqengine.resultset.ResultSet;
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
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.chemistry.opencmis.commons.spi.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.mockito.Mockito.*;
import static com.googlecode.cqengine.codegen.AttributeBytecodeGenerator.createAttributes;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 12/4/16
 * Time: 2:47 PM
 */
public class CMISSessionGeneratorMockImpl implements CMISSessionGenerator {


    Map<String, PropertyDefinition<?>> propertyDefinitionMap;
    Map<String, PropertyDefinition<?>> secondaryPropertyDefinitionMap;
    Map<String, SecondaryType> secondaryTypeStore;


    SessionImpl sessionImpl;
    SessionFactory sessionFactory;
    ObjectType objectType;
    FolderTypeImpl folderType;
    DocumentTypeImpl documentType;
    RepositoryInfo repositoryInfo;
    CmisBinding binding;
    ObjectFactory objectFactory;
    ObjectServiceImpl objectService;
    AuthenticationProvider authenticationProvider;
    Repository repository = new Repository();

    private class Repository {

        ConcurrentIndexedCollection<FileableCmisObject> sqlStore = new ConcurrentIndexedCollection<FileableCmisObject>();
        Map<String, FileableCmisObject> pathStore = new HashMap<>();
        Map<ObjectId, FileableCmisObject> idStore = new HashMap<>();
        Map<ObjectId, ContentStream> contentStore = new HashMap<>();


        public List<FileableCmisObject> query(String query) {
            List<FileableCmisObject> ret = new ArrayList<>();
            CQNParser<FileableCmisObject> parser = CQNParser.forPojoWithAttributes(FileableCmisObject.class, createAttributes(FileableCmisObject.class));
            ResultSet<FileableCmisObject> results = parser.retrieve(sqlStore, query);
            for (FileableCmisObject cmisObject : results) {
                ret.add(cmisObject);
            }
            return ret;
        }

        public List<FileableCmisObject> getChildren(String id, int skip, int maxItems) {
            int i = 0;

            List<FileableCmisObject> ret = new ArrayList<>();
            if (idStore.containsKey(new ObjectIdImpl(id))) {
                for (FileableCmisObject cmisObject : idStore.values()) {
                    if (cmisObject.getProperty("cmis:parentId").getFirstValue().toString().equalsIgnoreCase(id)) {
                        skip--;
                        if (skip <= 0)
                            ret.add(cmisObject);
                        if (i >= maxItems)
                            break;
                        i++;
                    }
                }
            }
            return ret;
        }

        public void update(FileableCmisObject cmisObject, FileableCmisObject cmisObjectNew) {
            idStore.put(new ObjectIdImpl(cmisObject.getId()), cmisObjectNew);
            if (pathStore.containsKey(cmisObject.getName()))
                pathStore.remove(cmisObject.getName());
            pathStore.put(cmisObjectNew.getName(), cmisObjectNew);
            if (contentStore.containsKey(new ObjectIdImpl(cmisObject.getId())))
                contentStore.replace(new ObjectIdImpl(cmisObjectNew.getId()), contentStore.get(new ObjectIdImpl(cmisObject.getId())));
        }

        public void delete(FileableCmisObject cmisObject) {
            sqlStore.remove(cmisObject);
            idStore.remove(cmisObject.getId());
            pathStore.remove(cmisObject.getName());
            if (contentStore.containsKey(new ObjectIdImpl(cmisObject.getId())))
                contentStore.remove(new ObjectIdImpl(cmisObject.getId()));
        }

        public void insert(FileableCmisObject cmisObject) {
            String name = cmisObject.getName();
            sqlStore.add(cmisObject);
            pathStore.put(name, cmisObject);
            idStore.put(new ObjectIdImpl(cmisObject.getId()), cmisObject);
        }


        public void insert(FileableCmisObject cmisObject, ContentStream contentStream) {
            insert(cmisObject);
            contentStore.put(new ObjectIdImpl(cmisObject.getId()), contentStream);
        }

        public boolean contains(String name) {
            return pathStore.containsKey(name);
        }

        public boolean contains(ObjectId id) {
            return idStore.containsKey(id);
        }

        public FileableCmisObject get(String name) {
            return pathStore.get(name);
        }

        public FileableCmisObject get(ObjectId id) {
            return idStore.get(id);
        }

        public FileableCmisObject getByPath(String path) {
            FileableCmisObject parent = null;
            String[] parts;
            int i = 1;
            if (path.equalsIgnoreCase("/"))
                parts = new String[]{"/"};
            else {
                parts = path.split("/");

            }
            for (String part : parts) {
                if (part.isEmpty())
                    part = part + "/";
                if (this.contains(part)) {

                    FileableCmisObject cmisObject = this.get(part);
                    if (parent != null && !cmisObject.getParents().get(0).equals(parent))
                        return null;
                    parent = cmisObject;
                    if (i == parts.length)
                        return cmisObject;
                } else
                    return null;
                i++;
            }
            return null;
        }

        public ContentStream getContent(FileableCmisObject cmisObject) {
            return getContent(new ObjectIdImpl(cmisObject.getId()));
        }

        public ContentStream getContent(ObjectId id) {
            return contentStore.get(id);
        }
    }

    public CMISSessionGeneratorMockImpl() {


        //       cmisBindingHelper = mock(CmisBindingHelper.class);
        //      given(cmisBindingHelper.createBinding(anyMap(), any(AuthenticationProvider.class), any(TypeDefinitionCache.class))).willReturn(binding);

        propertyDefinitionMap = getStringPropertyDefinitionMap();
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

        DocumentTypeDefinitionImpl documentTypeDefinition = new DocumentTypeDefinitionImpl();
        documentTypeDefinition.setPropertyDefinitions(propertyDefinitionMap);
        documentType = new DocumentTypeImpl(sessionImpl, documentTypeDefinition);
        documentType.setId("D:my:archivContent");
        documentType.setIsVersionable(true);

        secondaryTypeStore = createSecondaryTypes();

        repository.insert(createFileableCmisObject("0", "root", "root", "", true, false));
        repository.insert(createFileableCmisObject("1", "/", "/", "0", true, false));
        repository.insert(createFileableCmisObject("2", "/", "Datenverzeichnis", "1", true, false));
        repository.insert(createFileableCmisObject("3", "Datenverzeichnis", "Skripte", "2", true, false));
        repository.insert(createFileableCmisObject("4", "Datenverzeichnis/Skripte", "backup.js.sample", "3", false, false), createStream("//123"));

    }

    private ContentStream createStream(String content) {
        ContentStreamImpl contentStream = new ContentStreamImpl();
        contentStream.setStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
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
        PropertyStringDefinitionImpl propertyPersonDefinition = new PropertyStringDefinitionImpl();
        propertyPersonDefinition.setId("my:person");
        propertyPersonDefinition.setDisplayName("Person");
        propertyPersonDefinition.setQueryName("my:person");
        propertyPersonDefinition.setLocalName("person");
        propertyPersonDefinition.setCardinality(Cardinality.SINGLE);
        propertyPersonDefinition.setPropertyType(PropertyType.STRING);
        propertyPersonDefinition.setUpdatability(Updatability.READWRITE);
        map.put("my:person", propertyPersonDefinition);
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

    private Map<String, PropertyDefinition<?>> getStringPropertyDefinitionMap() {
        Map<String, PropertyDefinition<?>> map = new HashMap();
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
        PropertyIdDefinitionImpl propertySecondaryObjectTypeIdDefinition = new PropertyIdDefinitionImpl();
        propertySecondaryObjectTypeIdDefinition.setId("cmis:secondaryObjectTypeIds");
        propertySecondaryObjectTypeIdDefinition.setDisplayName("Secondary Object Type Id");
        propertySecondaryObjectTypeIdDefinition.setQueryName("cmis:secondaryObjectTypeIds");
        propertySecondaryObjectTypeIdDefinition.setLocalName("secondaryObjectTypeIds");
        propertySecondaryObjectTypeIdDefinition.setCardinality(Cardinality.MULTI);
        propertySecondaryObjectTypeIdDefinition.setPropertyType(PropertyType.ID);
        propertySecondaryObjectTypeIdDefinition.setUpdatability(Updatability.READWRITE);
        map.put("cmis:secondaryObjectTypeIds", propertySecondaryObjectTypeIdDefinition);
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
        return map;
    }

    FileableCmisObject createFileableCmisObject(String id, String path, String name, String parent, boolean folder, boolean majorVersion) {
        FileableCmisObject fileableCmisObject;

        ObjectDataImpl objectData = new ObjectDataImpl();
        PropertiesImpl properties = new PropertiesImpl();
        PropertyIdImpl propertyId = new PropertyIdImpl();
        propertyId.setId("cmis:objectId");
        propertyId.setLocalName("objectId");
        propertyId.setQueryName("cmis:objectId");
        propertyId.setDisplayName("Object Id");
        propertyId.setValue(id);
        properties.addProperty(propertyId);
        PropertyStringImpl propertyName = new PropertyStringImpl();
        propertyName.setId("cmis:name");
        propertyName.setLocalName("name");
        propertyName.setQueryName("cmis:name");
        propertyName.setDisplayName("Name");
        propertyName.setValue(name);
        properties.addProperty(propertyName);
        PropertyStringImpl propertyPath = new PropertyStringImpl();
        propertyPath.setId("cmis:path");
        propertyPath.setLocalName("path");
        propertyPath.setQueryName("cmis:path");
        propertyPath.setDisplayName("Path");
        propertyPath.setValue(path);
        properties.addProperty(propertyPath);
        PropertyIdImpl propertyType = new PropertyIdImpl();
        propertyType.setId("cmis:baseTypeId");
        propertyType.setLocalName("baseTypeId");
        propertyType.setQueryName("cmis:baseTypeId");
        propertyType.setDisplayName("Base Type Id");
        propertyType.setValue(folder ? "cmis:folder" : "cmis:document");
        properties.addProperty(propertyType);
        PropertyIdImpl propertyParentId = new PropertyIdImpl();
        propertyParentId.setId("cmis:parentId");
        propertyParentId.setLocalName("parentId");
        propertyParentId.setQueryName("cmis:parentId");
        propertyParentId.setDisplayName("Parent Id");
        propertyParentId.setValue(parent);
        properties.addProperty(propertyParentId);
        PropertyIdImpl propertyObjectType = new PropertyIdImpl();
        propertyObjectType.setId("cmis:objectTypeId");
        propertyObjectType.setLocalName("objectTypeId");
        propertyObjectType.setQueryName("cmis:objectTypeId");
        propertyObjectType.setDisplayName("Object Type Id");
        propertyObjectType.setValue(folder ? "cmis:folder" : "cmis:document");
        properties.addProperty(propertyObjectType);

        if (folder) {
            objectData.setProperties(properties);
            objectType = new FolderTypeImpl(sessionImpl, folderType);

            fileableCmisObject = new FolderImpl(sessionImpl, objectType, objectData, new OperationContextImpl());
        } else {
            PropertyBooleanImpl propertyIsVersionCheckedOut = new PropertyBooleanImpl();
            propertyIsVersionCheckedOut.setId("cmis:isVersionSeriesCheckedOut");
            propertyIsVersionCheckedOut.setLocalName("isVersionSeriesCheckedOut");
            propertyIsVersionCheckedOut.setQueryName("cmis:isVersionSeriesCheckedOut");
            propertyIsVersionCheckedOut.setDisplayName("Is Version Checked Out");
            propertyIsVersionCheckedOut.setValue(false);
            properties.addProperty(propertyIsVersionCheckedOut);
            PropertyBooleanImpl propertyIsPrivateWorkingCopy = new PropertyBooleanImpl();
            propertyIsPrivateWorkingCopy.setId("cmis:isPrivateWorkingCopy");
            propertyIsPrivateWorkingCopy.setLocalName("isPrivateWorkingCopy");
            propertyIsPrivateWorkingCopy.setQueryName("cmis:isPrivateWorkingCopy");
            propertyIsPrivateWorkingCopy.setDisplayName("Is private working copy");
            propertyIsPrivateWorkingCopy.setValue(false);
            properties.addProperty(propertyIsPrivateWorkingCopy);
            PropertyStringImpl propertyVersionLabel = new PropertyStringImpl();
            propertyVersionLabel.setId("cmis:versionLabel");
            propertyVersionLabel.setDisplayName("Version Label");
            propertyVersionLabel.setQueryName("cmis:versionLabel");
            propertyVersionLabel.setLocalName("versionLabel");
            propertyVersionLabel.setValue(majorVersion ? "1.0" : "0.1");
            properties.addProperty(propertyVersionLabel);
            PropertyStringImpl propertyCheckinComment = new PropertyStringImpl();
            propertyCheckinComment.setId("cmis:checkinComment");
            propertyCheckinComment.setDisplayName("Checkin Comment");
            propertyCheckinComment.setQueryName("cmis:checkinComment");
            propertyCheckinComment.setLocalName("checkinComment");
            propertyCheckinComment.setValue("Initial Version");
            properties.addProperty(propertyCheckinComment);
            PropertyIdImpl propertySecondaryObjectTypeIds = new PropertyIdImpl();
            propertySecondaryObjectTypeIds.setId("cmis:secondaryObjectTypeIds");
            propertySecondaryObjectTypeIds.setDisplayName("Secondary Object Type Ids");
            propertySecondaryObjectTypeIds.setQueryName("cmis:secondaryObjectTypeIds");
            propertySecondaryObjectTypeIds.setLocalName("secondaryObjectTypeIds");
            ArrayList<String> secondaryObjectTypeIds = new ArrayList<>();
            secondaryObjectTypeIds.add("P:cm:titled");
            secondaryObjectTypeIds.add("P:sys:localized");
            secondaryObjectTypeIds.add("P:cm:author");
            secondaryObjectTypeIds.add("P:my:idable");
            secondaryObjectTypeIds.add("P:my:amountable");
            secondaryObjectTypeIds.add("P:cm:emailed");
            propertySecondaryObjectTypeIds.setValues(secondaryObjectTypeIds);
            properties.addProperty(propertySecondaryObjectTypeIds);
            PropertyDateTimeImpl propertyCreationDate = new PropertyDateTimeImpl();
            propertyCreationDate.setId("cmis:creationDate");
            propertyCreationDate.setLocalName("creationDate");
            propertyCreationDate.setQueryName("cmis:objectTypeId");
            propertyCreationDate.setDisplayName("Creation Date ");
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
            propertyCreationDate.setValue(cal);
            properties.addProperty(propertyCreationDate);
            objectData.setProperties(properties);
            objectType = new DocumentTypeImpl(sessionImpl, documentType);
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
                FileableCmisObject cmisObject = repository.get(new ObjectIdImpl(objectId));
                repository.delete(cmisObject);
                return null;
            }

        }).when(session).delete(any(ObjectId.class), anyBoolean());
        when(session.createObjectId(any(String.class))).thenAnswer(new Answer<ObjectId>() {

            public ObjectId answer(InvocationOnMock invocation) throws Throwable {
                return new ObjectIdImpl((String) invocation.getArguments()[0]);
            }
        });
        when(session.getObject(any(ObjectId.class))).thenAnswer(new Answer<FileableCmisObject>() {
            public FileableCmisObject answer(InvocationOnMock invocation) throws Throwable {
                return repository.get((ObjectId) invocation.getArguments()[0]);
            }
        });
        when(session.getObject(any(String.class), any(OperationContext.class))).thenAnswer(new Answer<FileableCmisObject>() {
            public FileableCmisObject answer(InvocationOnMock invocation) throws Throwable {
                return repository.get(new ObjectIdImpl((String) invocation.getArguments()[0]));
            }
        });
        when(session.getObject(any(ObjectId.class), any(OperationContext.class))).thenAnswer(new Answer<FileableCmisObject>() {
            public FileableCmisObject answer(InvocationOnMock invocation) throws Throwable {
                return repository.get((ObjectId) invocation.getArguments()[0]);
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
                else if (string.startsWith("P:"))
                    return secondaryTypeStore.get(string);
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
                else if (string.startsWith("P:"))
                    return secondaryTypeStore.get(string);
                return documentType;
            }
        });
        return session;
    }

    private ObjectId createFileableCmisObject(InvocationOnMock invocation, boolean folder) {
        boolean majorVersion = false;
        Object[] args = invocation.getArguments();
        Map<String, Object> props = (Map<String, Object>) args[0];

        if (props.get(PropertyIds.OBJECT_TYPE_ID) == null) {
            throw new IllegalArgumentException();
        }
        if (props.get(PropertyIds.NAME) == null) {
            throw new IllegalArgumentException();
        }
        String name = (String) props.get(PropertyIds.NAME);
        String id = Double.toString(Math.random() * 100 + 1);
        FileableCmisObject cmis = (FileableCmisObject) args[1];
        String path = cmis.getPaths().get(0);
        if (!folder && invocation.getArguments().length > 2 && ((VersioningState) invocation.getArguments()[3]).equals(VersioningState.MAJOR))
            majorVersion = true;
        if (!folder)
            repository.insert(createFileableCmisObject(id, path, name, cmis.getId(), folder, majorVersion), (ContentStream) invocation.getArguments()[2]);
        else
            repository.insert(createFileableCmisObject(id, path, name, cmis.getId(), folder, majorVersion));

        return new ObjectIdImpl(id);
    }


    private CollectionIterable<FileableCmisObject> mockCollectionIterable() {
        CollectionIterable<FileableCmisObject> collectionIterable = mock(CollectionIterable.class);
        // collectionIterable = (CollectionIterable) mockIterable(collectionIterable, pathStore.get("/Datenverzeichnis/Skripte/backup.js.sample"));
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
                        FileableCmisObject cmisObject = repository.get(new ObjectIdImpl(id));
                        for (Property property : cmisObject.getProperties()) {
                            if (property.getId().equalsIgnoreCase("cmis:parentId")) {
                                result = repository.get(new ObjectIdImpl(property.getValueAsString()));
                                break;
                            }
                        }
                        return getObjectDataFromCmisObject(result);
                    }
                });
                when(navigationService.getObjectParents(anyString(), anyString(), anyString(), anyBoolean(), any(IncludeRelationships.class), anyString(), anyBoolean(), any(ExtensionsData.class))).then(new Answer<List<ObjectParentData>>() {
                    public List<ObjectParentData> answer(InvocationOnMock invocation) throws Throwable {
                        List<ObjectParentData> result = new ArrayList<>();
                        FileableCmisObject cmisObject = repository.get(new ObjectIdImpl((String) invocation.getArguments()[1]));
                        for (Property property : cmisObject.getProperties()) {
                            if (property.getId().equalsIgnoreCase("cmis:parentId")) {
                                result.add(getObjectParentDataFromCmisObject(repository.get(new ObjectIdImpl(property.getValueAsString()))));
                                break;
                            }
                        }
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
                    public ObjectList answer(InvocationOnMock invocation) throws Throwable {
                        int typen = 0;
                        ObjectListImpl objectList = new ObjectListImpl();
                        Object[] args = invocation.getArguments();
                        String statement = (String) args[1];
                        BigInteger skip = (BigInteger) args[7];
                        BigInteger maxItems = (BigInteger) args[6];
                        List<ObjectData> list = new ArrayList<>();
                        final String search = statement.substring(statement.indexOf("'") + 1, statement.indexOf("'", statement.indexOf("'") + 1));
                        if (statement.contains("IN_FOLDER")) {
                            if (statement.contains("from my:archivContent"))
                                typen = 1;
                            if (statement.contains("from cmis:folder"))
                                typen = -1;
                            if (repository.contains(new ObjectIdImpl(search))) {
                                for (FileableCmisObject cmisObject : repository.idStore.values()) {
                                    if (typen <= 0 && cmisObject.getType() instanceof FolderType && cmisObject.getProperty("cmis:parentId").getFirstValue().toString().equalsIgnoreCase(search))
                                        list.add(getObjectDataFromCmisObject(cmisObject));
                                    if (typen >= 0 && cmisObject.getType() instanceof DocumentType && cmisObject.getProperty("cmis:parentId").getFirstValue().toString().equalsIgnoreCase(search))
                                        list.add(getObjectDataFromCmisObject(cmisObject));
                                }
                            }

                        } else {
                            if (repository.contains(search))
                                list.add(getObjectDataFromCmisObject(repository.get(search)));
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
                                            ret = order[1].equalsIgnoreCase("ASC") ? -1 : -1 ;
                                        if (ret != 0)
                                            break;
                                    }
                                    return ret;
                                }
                            });
                        }
                        objectList.setObjects(list.subList(skip.intValue(), skip.intValue() + maxItems.intValue() > list.size() ? list.size() :  skip.intValue() + maxItems.intValue()));
                        objectList.setNumItems(BigInteger.valueOf(list.size()));
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
                        Document document = (Document) repository.get(new ObjectIdImpl(holder.getValue()));
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
                        Document document = (Document) repository.get(new ObjectIdImpl(holder.getValue()));
                        if (!document.isPrivateWorkingCopy()) throw new CmisVersioningException();
                        if (properties != null && properties.getProperties() != null && !properties.getProperties().isEmpty()) {
                            document.updateProperties(properties.getProperties());
                        }
                        if (stream != null) {
                            ContentStreamImpl streamCurrent = (ContentStreamImpl) repository.getContent(new ObjectIdImpl(holder.getValue()));
                            streamCurrent.setStream(stream.getStream());
                        }
                        ((PropertyImpl) document.getProperty("cmis:isPrivateWorkingCopy")).setValue(false);
                        if (major)
                            ((PropertyImpl) document.getProperty("cmis:versionLabel")).setValue(new BigDecimal(document.getProperty("cmis:versionLabel").getValueAsString()).add(new BigDecimal("1")).toString());
                        else
                            ((PropertyImpl) document.getProperty("cmis:versionLabel")).setValue(new BigDecimal(document.getProperty("cmis:versionLabel").getValueAsString()).add(new BigDecimal("0.1")).toString());
                        if (checkinComment != null && !checkinComment.isEmpty())
                            ((PropertyImpl) document.getProperty("cmis:checkinComment")).setValue(checkinComment);
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
                return getObjectDataFromCmisObject(repository.get(new ObjectIdImpl(objectId)));
            }
        });
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                FileableCmisObject cmisObjectNew;
                String objectId = (String) ((Holder) invocation.getArguments()[1]).getValue();
                FileableCmisObject cmisObject = repository.get(new ObjectIdImpl(objectId));
                ObjectDataImpl objectData = (ObjectDataImpl) getObjectDataFromCmisObject(cmisObject);
                for (PropertyData property : ((Properties) invocation.getArguments()[3]).getPropertyList()) {
                    PropertiesImpl properties = (PropertiesImpl) objectData.getProperties();
                    properties.addProperty(property);
                }

                if (cmisObject.getType() instanceof FolderType)
                    cmisObjectNew = new FolderImpl(sessionImpl, objectType, objectData, new OperationContextImpl());
                else
                    cmisObjectNew = new DocumentImpl(sessionImpl, objectType, objectData, new OperationContextImpl());
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
                FileableCmisObject cmisObject = repository.get(new ObjectIdImpl(objectId));
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

    private ObjectParentData getObjectParentDataFromCmisObject(FileableCmisObject cmisObject) {
        ObjectParentDataImpl objectData = new ObjectParentDataImpl();
        objectData.setObject(getObjectDataFromCmisObject(cmisObject));
        objectData.setRelativePathSegment(cmisObject.getPropertyValue("cmis:path"));
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
        when(info.getRootFolderId()).thenReturn("99999");
        return info;
    }

}
