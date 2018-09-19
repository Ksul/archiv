package de.ksul.archiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.controller.ArchivController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
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
@TestPropertySource(properties={"ksul.archiv.testing.testData="})
@DirtiesContext
public class ArchivApplicationRestControllerTest extends ArchivApplicationRestControllerAbstractTest {

    @BeforeEach
    public void setUp(@Autowired AlfrescoConnector connector,
                      @Autowired MockMvc mockMvc,
                      @Autowired ObjectMapper objectMapper) throws Exception {

        con = connector;
        Assertions.assertNotNull(con);
        this.mockMvc = mockMvc;
        Assertions.assertNotNull(mockMvc);
        this.objectMapper = objectMapper;
        Assertions.assertNotNull(objectMapper);
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
