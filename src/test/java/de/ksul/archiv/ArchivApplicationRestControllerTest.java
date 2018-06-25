package de.ksul.archiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.controller.ArchivController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 4/10/17
 * Time: 4:56 PM
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest({ArchivTestConfiguration.class, ArchivController.class})
public class ArchivApplicationRestControllerTest extends ArchivApplicationRestControllerAbstractTest {

    @Autowired
    AlfrescoConnector con;


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    public void setUp() throws Exception {
        setVariables(con, mockMvc, objectMapper);
        filePdf = "/src/test/resources/Test.pdf";
        fileTxt = "/src/test/resources/test.txt";
        fileZip = "/src/test/resources/Sample.zip";
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
                .andExpect(jsonPath("$.data.server", nullValue()))
                .andExpect(jsonPath("$.data.binding", nullValue()))
                .andExpect(jsonPath("$.data.user", nullValue()))
                .andExpect(jsonPath("$.data.password", nullValue()));

    }

}
