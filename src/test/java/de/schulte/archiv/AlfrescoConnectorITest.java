package de.schulte.archiv;

import de.schulte.archiv.configuration.ArchivConfiguration;
import de.schulte.archiv.configuration.ArchivTestProperties;
import de.schulte.archiv.model.Column;
import de.schulte.archiv.model.Order;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 09.01.14
 * Time: 13:06
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@EnableConfigurationProperties({ArchivTestProperties.class})
@SpringBootTest(classes = {ArchivConfiguration.class})
public class AlfrescoConnectorITest extends AlfrescoConnectorAbstractTest {

    @Autowired
    ArchivTestProperties testProperties;

    @Autowired
    AlfrescoConnector con;

    @Before
    public void setUp() throws Exception {
        setCon(con);
        filePdf = testProperties.getTestPDF();
        fileTxt = testProperties.getTestTXT();
        fileZip = testProperties.getTestZIP();
        super.setUp();
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
}
