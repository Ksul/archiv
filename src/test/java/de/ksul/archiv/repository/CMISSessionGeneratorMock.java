package de.ksul.archiv.repository;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.ksul.archiv.VerteilungConstants;
import de.ksul.archiv.configuration.ArchivProperties;
import de.ksul.archiv.configuration.ArchivTestProperties;
import de.ksul.archiv.repository.deserializer.ContentStreamDeserializer;
import de.ksul.archiv.repository.deserializer.PropertyDeserializer;
import de.ksul.archiv.repository.serializer.*;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.SessionImpl;
import org.apache.chemistry.opencmis.client.runtime.util.CollectionIterable;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.DateTimeResolution;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 12/4/16
 * Time: 2:47 PM
 */
@Service
public class CMISSessionGeneratorMock implements CMISSessionGenerator {

    private static Logger logger = LoggerFactory.getLogger(CMISSessionGeneratorMock.class.getName());
    private String file;
    private SessionImpl sessionImpl;
    private Repository repository;

    private ArchivProperties archivProperties;


    @Autowired
    public CMISSessionGeneratorMock(ResourceLoader resourceLoader, ArchivProperties archivProperties, ArchivTestProperties archivTestProperties) {

        this.file = archivTestProperties.getTestData();
        this.archivProperties = archivProperties;
        MockUtils.getInstance().setResourceLoader(resourceLoader);

        mockCollectionIterable();


    }


    @Override
    public Session generateSession() {

        return sessionImpl;
    }

    private CollectionIterable<FileableCmisObject> mockCollectionIterable() {
        CollectionIterable<FileableCmisObject> collectionIterable = mock(CollectionIterable.class);
        return collectionIterable;
    }

    @PreDestroy
    public void shutDown() throws IOException {
        if (file != null && !file.isEmpty()) {
            logger.info("Save Data...");
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.configure(SerializationFeature.INDENT_OUTPUT,true);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            SimpleModule module = new SimpleModule();
            module.addSerializer(PropertyType.class, new PropertyTypeSerializer());
            module.addSerializer(Cardinality.class, new CardinalitySerializer());
            module.addSerializer(DateTimeResolution.class, new DateTimeResolutionSerializer());
            module.addSerializer(Updatability.class, new UpdatabilitySerializer());
            module.addSerializer(ContentStream.class, new ContentStreamSerializer());
            mapper.registerModule(module);
            mapper.writeValue(new File(file), repository);
            logger.info("Data saved!");
        }

    }

    @PostConstruct
    public void setup()  {
        if (file != null && !file.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            SimpleModule module = new SimpleModule();
            module.addDeserializer(Property.class, new PropertyDeserializer<FileableCmisObject>());
            module.addDeserializer(ContentStream.class, new ContentStreamDeserializer());
            mapper.registerModule(module);
            File jsonFile = new File(file);
            if (jsonFile.exists()) {
                logger.info("Load Data...");
                try {
                    repository = mapper.readValue(jsonFile, new TypeReference<Repository>() {
                    });
                    Repository.setInstance(repository);
                    sessionImpl = new SessionMock().setRepository(repository).getSession();
                    MockUtils.getInstance().setSession(sessionImpl);
                    logger.info("Data loaded!");
                } catch (Exception e) {
                    logger.error("Data not loaded!", e);
                }
            }
        }
        if (repository == null) {
            repository = Repository.getInstance();
            sessionImpl = new SessionMock().setRepository(repository).getSession();
            MockUtils mockUtils = MockUtils.getInstance();
            mockUtils.setSession(sessionImpl);
            TreeNode node;
            node = repository.insert(null, mockUtils.createFileableCmisObject(repository, null, null, archivProperties.getCompanyHomeName(), mockUtils.getFolderType(), null), false);
            node = repository.insert(node, mockUtils.createFileableCmisObject(repository, null, "/", archivProperties.getDataDictionaryName(), mockUtils.getFolderType(), null), false);
            node = repository.insert(node, mockUtils.createFileableCmisObject(repository, null, "/" + archivProperties.getDataDictionaryName(), archivProperties.getScriptDirectoryName(), mockUtils.getFolderType(), null), false);
            repository.insert(node, mockUtils.createFileableCmisObject(repository, null, "/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), "backup.js.sample", mockUtils.getDocumentType(), "application/x-javascript"), mockUtils.createStream("// ", "application/x-javascript"), "1.0", false);
            repository.insert(node, mockUtils.createFileableCmisObject(repository, null, "/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), "alfresco docs.js.sample", mockUtils.getDocumentType(), "application/x-javascript"), mockUtils.createStream("// ", "application/x-javascript"), "1.0", false);
            repository.insert(node, mockUtils.createFileableCmisObject(repository, null, "/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), "doc.xml", mockUtils.getDocumentType(), "text/xml"), mockUtils.createFileStream("classpath:static/rules/doc.xml", "text/xml"), "1.0", false);
            repository.insert(node, mockUtils.createFileableCmisObject(repository, null, "/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), "doc.xsd", mockUtils.getDocumentType(), "text/xml"), mockUtils.createFileStream("classpath:static/rules/doc.xsd", "text/xml"), "1.0", false);
            repository.insert(node, mockUtils.createFileableCmisObject(repository, null, "/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), "recognition.js", mockUtils.getDocumentType(), "application/x-javascript"), mockUtils.createFileStream("classpath:static/js/recognition.js", "application/x-javascript"), "1.0", false);

            repository.insert(null, mockUtils.createFileableCmisObject(repository, null, null, "categories", mockUtils.getItemType(), null), false);

        }
    }

}
