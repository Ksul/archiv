package de.ksul.archiv.controller;

import de.ksul.archiv.AlfrescoConnector;
import de.ksul.archiv.configuration.ArchivTestConfiguration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 28.11.16
 * Time: 13:15
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ArchivTestConfiguration.class})
@DirtiesContext
public class ArchivControllerTest extends  ArchivControllerAbstractTest {


    @Autowired
    AlfrescoConnector con;

    @Before
    public void setup() throws Exception{
        setCon(con);
        super.setUp();
        services = new ArchivController(con);
        filePdf = "/src/test/resources/Test.pdf";
        fileTxt = "/src/test/resources/test.txt";
        fileZip = "/src/test/resources/Sample.zip";
    }

    @After
    public void shutDown() throws Exception{
        super.shutDown();
    }

}
