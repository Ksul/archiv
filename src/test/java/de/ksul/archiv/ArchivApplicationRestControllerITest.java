package de.ksul.archiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.configuration.ArchivTestProperties;
import de.ksul.archiv.controller.ArchivController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    public void testGetConnection() throws Exception {
        this.mockMvc.perform(get("/getConnection")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data.server", is("http://localhost:9080/alfresco/")))
                .andExpect(jsonPath("$.data.binding", is("http://localhost:9080/alfresco/api/-default-/public/cmis/versions/1.1/atom")))
                .andExpect(jsonPath("$.data.user", is("admin")))
                .andExpect(jsonPath("$.data.password", is("admin")));

    }

}
