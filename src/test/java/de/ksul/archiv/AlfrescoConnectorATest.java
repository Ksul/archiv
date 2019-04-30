package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivProperties;
import de.ksul.archiv.model.comments.CommentPaging;
import org.apache.chemistry.opencmis.client.api.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 09.01.14
 * Time: 13:06
 * To change this template use File | Settings | File Templates.
 */
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ArchivConfiguration.class})
@DirtiesContext
public class AlfrescoConnectorATest extends AlfrescoConnectorAbstractTest {

    @BeforeEach
    public void setUp(@Autowired AlfrescoConnector connector, @Autowired ArchivProperties archivProperties) throws Exception {
        con = connector;
        Assertions.assertNotNull(con);
        Assertions.assertNotNull(archivProperties);
        filePdf = archivProperties.getTesting().getTestpdf();
        fileTxt = archivProperties.getTesting().getTesttxt();
        fileZip = archivProperties.getTesting().getTestzip();
        super.setUp();
    }

    @AfterEach
    public void shutDown() throws Exception {
        super.shutDown();
    }

    @Test
    public void testGetComments() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        Map<String, Object> abd = con.addComment(document,"Testkommentar");
        assertThat(abd, Matchers.notNullValue());
        CommentPaging result =  con.getComments(document);
        assertThat(result.getList().getEntries().get(0).getEntry().getContent(), Matchers.equalTo("Testkommentar"));
        document.delete(true);
    }

    @Test
    public void testTotalNumItems() throws Exception {
        ItemIterable<CmisObject> result = null;
        CmisObject cmisObject = con.getSession().getObjectByPath("/");
        Folder folder = (Folder) cmisObject;
        OperationContext operationContext = con.getSession().createOperationContext();
        //operationContext.setMaxItemsPerPage(2);
        QueryStatement stmt = con.getSession().createQueryStatement("IN_FOLDER(?)");
        stmt.setString(1, folder.getId());
        result = con.getSession().queryObjects("cmis:folder", stmt.toString(), false, operationContext);
        long totalNumItems = result.getTotalNumItems();
        operationContext.setMaxItemsPerPage(1);
        result = con.getSession().queryObjects("cmis:folder", stmt.toString(), false, operationContext);
        // Fehler, sollte eigentlich gleich sein
        assertThat(result.getTotalNumItems(), Matchers.not(totalNumItems));
    }

    @Test
    public void testCreateRule() throws Exception {
       CmisObject folder = buildTestFolder("TestFolder", null);
       CmisObject document = buildDocument("TestDocument", folder);
        assertThat(con.hasRule("Test", folder.getId()), Matchers.is(false));
       con.createRule(folder.getId(), document.getId(), "Test", "");
       assertThat(con.hasRule("Test", folder.getId()), Matchers.is(true));
    }
}
