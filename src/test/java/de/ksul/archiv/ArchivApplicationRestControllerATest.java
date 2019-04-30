package de.ksul.archiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivProperties;
import de.ksul.archiv.controller.ArchivController;
import de.ksul.archiv.request.CommentRequest;
import de.ksul.archiv.request.ObjectByIdRequest;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 4/10/17
 * Time: 4:56 PM
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest({ArchivConfiguration.class, ArchivController.class})
@DirtiesContext
public class ArchivApplicationRestControllerATest extends ArchivApplicationRestControllerAbstractTest {

    @BeforeEach
    public void setUp(@Autowired AlfrescoConnector connector,
                      @Autowired ArchivProperties archivProperties,
                      @Autowired MockMvc mockMvc,
                      @Autowired ObjectMapper objectMapper) throws Exception {
        con = connector;
        Assertions.assertNotNull(con);
        this.mockMvc = mockMvc;
        Assertions.assertNotNull(mockMvc);
        this.objectMapper = objectMapper;
        Assertions.assertNotNull(objectMapper);
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
    public void testGetConnection() throws Exception {
        this.mockMvc.perform(get("/getConnection")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data.server", is("http://localhost:80/alfresco/")))
                .andExpect(jsonPath("$.data.binding", is("http://localhost:80/alfresco/api/-default-/public/cmis/versions/1.1/atom")))
                .andExpect(jsonPath("$.data.user", is("admin")))
                .andExpect(jsonPath("$.data.password", is("admin")));

    }

    @Test
    public void testGetComments() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setDocumentId(document.getId());
        commentRequest.setComment("Testkommentar");
        this.mockMvc.perform(post("/addComment")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(commentRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()));
        this.mockMvc.perform(post("/getComments")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(new ObjectByIdRequest(document.getId())))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data.list.entries.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data.list.entries[0].entry.content", is("Testkommentar")));
        document.delete();
        folder.delete();
    }


}
