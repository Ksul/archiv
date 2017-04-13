package de.ksul.archiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.configuration.ArchivTestProperties;
import de.ksul.archiv.controller.ArchivController;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 4/10/17
 * Time: 4:56 PM
 */
@RunWith(SpringRunner.class)
@WebMvcTest({ArchivConfiguration.class, ArchivController.class})
@EnableConfigurationProperties({ArchivTestProperties.class})
public class ArchivApplicationRestControllerITest extends ArchivApplicationRestControllerAbstractTest {

    @Autowired
    AlfrescoConnector con;

    @Autowired
    ArchivTestProperties testProperties;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Before
    public void setUp() throws Exception {
        setVariables(con, mockMvc, objectMapper);
        filePdf = testProperties.getTestPDF();
        fileTxt = testProperties.getTestTXT();
        fileZip = testProperties.getTestZIP();
        super.setUp();
    }

}
