package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivProperties;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.repository.MockUtils;
import de.ksul.archiv.repository.Repository;
import de.ksul.archiv.repository.TreeNode;
import de.ksul.archiv.repository.script.RecognizeEndpoints;
import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.CMISStrictDictionaryService;
import org.alfresco.opencmis.dictionary.DocumentTypeDefinitionWrapper;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.dictionary.*;
import org.alfresco.repo.i18n.MessageServiceImpl;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.DynamicallySizedThreadPoolExecutor;
import org.alfresco.util.TraceableThreadFactory;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 28.08.18
 * Time: 10:18
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ArchivConfiguration.class})
@TestPropertySource(properties = {"ksul.archiv.testing.testData="})
@DirtiesContext
public class ScriptITest {


    private AlfrescoConnector con;

    private ArchivProperties archivProperties;

    @Autowired
    public void setArchivProperties(ArchivProperties archivProperties) {
        this.archivProperties = archivProperties;
    }

    @Autowired
    public void setCon(AlfrescoConnector con) {
        this.con = con;
    }

    @Test
    public void testScript() throws Exception {
        CmisObject folder;
        folder = con.getNode("/Archiv/Inbox");
        con.uploadDocument(((Folder) folder), new File(System.getProperty("user.dir") + "/src/test/resources/Test.pdf"), "application/pdf", VersioningState.MINOR);
    }

    @Test
    public void testUploadScript() throws Exception {
        Document doc;
        doc = (Document) con.getNode("/" + archivProperties.getDataDictionaryName() + "/" + archivProperties.getScriptDirectoryName() + "/recognition.js");
        String content = FileUtils.readFileToString( new File(System.getProperty("user.dir") + "/src/main/resources/static/js/recognition.js"), StandardCharsets.UTF_8);
        con.updateDocument(doc, content.getBytes("UTF-8"), VerteilungConstants.DOCUMENT_TYPE_TEXT,  null, VersioningState.MAJOR, "neuer Versionskommentar");
    }



}
