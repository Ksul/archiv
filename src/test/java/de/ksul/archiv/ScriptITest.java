package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivProperties;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 28.08.18
 * Time: 10:18
 */
@Disabled
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
