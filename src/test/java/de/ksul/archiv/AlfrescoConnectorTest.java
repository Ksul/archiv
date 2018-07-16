package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivTestConfiguration;
import org.apache.chemistry.opencmis.client.api.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 09.01.14
 * Time: 13:06
 * To change this template use File | Settings | File Templates.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes={ArchivTestConfiguration.class})
@TestPropertySource(properties={"ksul.archiv.test.testData="})
@DirtiesContext
public class AlfrescoConnectorTest extends AlfrescoConnectorAbstractTest {

    @BeforeEach
    public void setUp(@Autowired AlfrescoConnector connector) throws Exception {

        con = connector;
        Assertions.assertNotNull(con);
        filePdf = "/src/test/resources/Test.pdf";
        fileTxt = "/src/test/resources/test.txt";
        fileZip = "/src/test/resources/Sample.zip";
        super.setUp();
    }

    @AfterEach
    public void shutDown() throws Exception {
        super.shutDown();
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
        // Hier ist das natürlich gleich!!
        assertThat(result.getTotalNumItems(), Matchers.is(totalNumItems));
    }
}
