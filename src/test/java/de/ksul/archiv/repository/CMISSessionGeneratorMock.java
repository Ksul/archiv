package de.ksul.archiv.repository;

import de.ksul.archiv.configuration.ArchivProperties;
import de.ksul.archiv.configuration.ArchivTestProperties;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.SessionImpl;
import org.apache.chemistry.opencmis.client.runtime.util.CollectionIterable;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
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
    private ResourceLoader resourceLoader;

    private SessionImpl sessionImpl;
    private Repository repository;

    private ArchivTestProperties archivTestProperties;



    public CMISSessionGeneratorMock(Repository repository, ResourceLoader resourceLoader, ArchivProperties archivProperties, ArchivTestProperties archivTestProperties) {

        this.repository = repository;
        this.archivTestProperties = archivTestProperties;
        this.resourceLoader = resourceLoader;

        mockCollectionIterable();

        sessionImpl = new SessionMock(repository).getSession();
        when(sessionImpl.getRepositoryId()).thenReturn("0");


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
