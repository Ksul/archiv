package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivProperties;
import de.ksul.archiv.model.RuleAction;
import de.ksul.archiv.model.RuleCondition;
import de.ksul.archiv.model.Rule;
import de.ksul.archiv.request.RuleCreateRequest;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URL;
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
public class AlfrescoConnectorITest extends AlfrescoConnectorAbstractTest {

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
    public void testGetTicket() throws Exception {
        String ticket = con.getTicket();
        assertThat(ticket, Matchers.notNullValue());
        assertThat(ticket, Matchers.startsWith("TICKET_"));
    }


    @Test
    public void testGetComments() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        Map<String, Object> abd = con.addComment(document,"Testkommentar");
        assertThat(abd, Matchers.notNullValue());
        Map<String, Object>  result =  con.getComments(document);
        assertThat(((Map) ((ArrayList) result.get("items")).get(0)).get("content"), Matchers.equalTo("Testkommentar"));
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
    public void testHasRule() throws Exception {
        CmisObject cmisObject;
        cmisObject = con.getNode("/Archiv/Inbox");
        assertThat(con.hasRule("Verteilung", cmisObject.getId()), Matchers.is(true));

    }

    @Test
    public void testCreateRule() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        con.createRule(folder.getId(), document.getId(), "Test");

    }
}
