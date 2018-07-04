package de.ksul.archiv.repository;

import de.ksul.archiv.PDFConnector;
import de.ksul.archiv.configuration.ArchivProperties;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.ObjectServiceImpl;
import org.apache.chemistry.opencmis.client.runtime.*;
import org.apache.chemistry.opencmis.client.runtime.cache.Cache;
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
import java.lang.reflect.Field;
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

    private SessionImpl sessionImpl;
    private Repository repository = new Repository();



    public CMISSessionGeneratorMock(ResourceLoader resourceLoader, ArchivProperties archivProperties) {


        //       cmisBindingHelper = mock(CmisBindingHelper.class);
        //      given(cmisBindingHelper.createBinding(anyMap(), any(AuthenticationProvider.class), any(TypeDefinitionCache.class))).willReturn(binding);
        this.resourceLoader = resourceLoader;

        mockCollectionIterable();

        sessionImpl = new SessionMock(repository).getSession();
        when(sessionImpl.getRepositoryId()).thenReturn("0");

        //sessionFactory = mockSessionFactory();
        //objectService = ObjectServiceMock.getMock(repository, sessionImpl);



        Map<String, Object> properties = new HashMap<>();

        repository.insert(null, MockUtils.getInstance().createFileableCmisObject(repository, sessionImpl, null,  null, "/", MockUtils.getInstance().getFolderType(sessionImpl), null));
        repository.insert("/", MockUtils.getInstance().createFileableCmisObject(repository, sessionImpl, null, "/", archivProperties.getDataDictionaryName(),  MockUtils.getInstance().getFolderType(sessionImpl), null));
        repository.insert("/" + archivProperties.getDataDictionaryName(), MockUtils.getInstance().createFileableCmisObject(repository, sessionImpl, null, "/" + archivProperties.getDataDictionaryName(), archivProperties.getScriptDirectoryName(),  MockUtils.getInstance().getFolderType(sessionImpl),  null));
        repository.insert("/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), MockUtils.getInstance().createFileableCmisObject(repository, sessionImpl, null, "/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), "backup.js.sample", MockUtils.getInstance().getDocumentType(sessionImpl), "application/x-javascript"), createStream("// "), "1.0");
        repository.insert("/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), MockUtils.getInstance().createFileableCmisObject(repository, sessionImpl, null, "/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), "alfresco docs.js.sample",  MockUtils.getInstance().getDocumentType(sessionImpl), "application/x-javascript"), createStream("// "), "1.0");
        repository.insert("/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), MockUtils.getInstance().createFileableCmisObject(repository, sessionImpl, null, "/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), "doc.xml",  MockUtils.getInstance().getDocumentType(sessionImpl), "text/xml"), createFileStream("classpath:static/rules/doc.xml"), "1.0");
        repository.insert("/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), MockUtils.getInstance().createFileableCmisObject(repository, sessionImpl, null, "/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), "doc.xsd",  MockUtils.getInstance().getDocumentType(sessionImpl), "text/xml"), createFileStream("classpath:static/rules/doc.xsd"), "1.0");
        repository.insert("/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), MockUtils.getInstance().createFileableCmisObject(repository, sessionImpl, null, "/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName(), "recognition.js",  MockUtils.getInstance().getDocumentType(sessionImpl), "application/x-javascript"), createFileStream("classpath:static/js/recognition.js"), "1.0");


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







    @Override
    public Session generateSession() {

        return sessionImpl;
    }







    private CollectionIterable<FileableCmisObject> mockCollectionIterable() {
        CollectionIterable<FileableCmisObject> collectionIterable = mock(CollectionIterable.class);
        return collectionIterable;
    }










}
