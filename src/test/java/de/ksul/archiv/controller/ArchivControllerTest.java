package de.ksul.archiv.controller;

import de.ksul.archiv.AlfrescoConnector;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 28.11.16
 * Time: 13:15
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes={ArchivTestConfiguration.class})
@DirtiesContext
public class ArchivControllerTest extends  ArchivControllerAbstractTest {


    @Autowired
    AlfrescoConnector con;

    @BeforeEach
    public void setup() throws Exception{
        setCon(con);
        super.setUp();
        services = new ArchivController(con);
        filePdf = "/src/test/resources/Test.pdf";
        fileTxt = "/src/test/resources/test.txt";
        fileZip = "/src/test/resources/Sample.zip";
    }

    @AfterEach
    public void shutDown() throws Exception{
        super.shutDown();
    }

}
