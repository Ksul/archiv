package de.ksul.archiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ksul.archiv.request.DataTablesRequest;
import de.ksul.archiv.request.RestRequest;
import net.minidev.json.JSONArray;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 3/29/17
 * Time: 3:25 PM
 */
public abstract class ArchivApplicationRestControllerAbstractTest extends AlfrescoTest {

    private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    protected MockMvc mockMvc;

    protected ObjectMapper objectMapper;

    protected String filePdf;

    protected String fileTxt;

    protected String fileZip;

    private AlfrescoConnector con;

    public void setVariables(AlfrescoConnector con, MockMvc mockMvc, ObjectMapper objectMapper) {
        this.con = con;
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        super.setCon(con);
    }

    @Test
    public void testIsConnected() throws Exception {
        this.mockMvc.perform(get("/isConnected")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", is(true)));

    }

    @Test
    public void testGetTitles() throws Exception {
        this.mockMvc.perform(get("/getTitles")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(JSONArray.class)));

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

    @Test
    public void testOpenDocument() throws Exception {
        RestRequest restRequest = new RestRequest();
        restRequest.setDocumentId(con.getNode("/Datenverzeichnis/Skripte/backup.js.sample").getId());
        this.mockMvc.perform(post("/openDocument")
                .contentType(APPLICATION_JSON_UTF8)
                .content( objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.name", is("backup.js.sample")))
                .andExpect(jsonPath("$.mimeType", is("application/x-javascript")))
                .andExpect(jsonPath("$.data", startsWith(Base64.encodeBase64String("// ".getBytes()))));
        restRequest.setDocumentId("1");
        this.mockMvc.perform(post("/openDocument")
                .contentType(APPLICATION_JSON_UTF8)
                .content( objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.name", nullValue()))
                .andExpect(jsonPath("$.mimeType", nullValue()));
    }

    @Test
    public void testGetNodeId() throws Exception {
        RestRequest restRequest = new RestRequest();
        restRequest.setFilePath("/");
        this.mockMvc.perform(post("/getNodeId")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", notNullValue()));
        restRequest.setFilePath("/xyz");
        this.mockMvc.perform(post("/getNodeId")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    public void testCreateDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        Map<String, Object> p1 = new HashMap<>();
        p1.put("cmis:name", "Testdocument");
        Map<String, Object> p2 = new HashMap<>();
        p2.put("cm:description", "Document mit Testinhalt");
        Map<String, Object> extraProperties = new HashMap<>();
        extraProperties.put("cmis:document", p1);
        extraProperties.put("P:cm:titled", p2);
        RestRequest restRequest = new RestRequest();
        restRequest.setDocumentId(folder.getId());
        restRequest.setFileName("Testdocument");
        restRequest.setContent(Base64.encodeBase64String("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?".getBytes()));
        restRequest.setMimeType("text");
        restRequest.setVersionState(VersioningState.MINOR.value());
        restRequest.setExtraProperties(extraProperties);
        this.mockMvc.perform(post("/createDocument")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data.objectTypeId", is("cmis:document")))
                .andExpect(jsonPath("$.data.versionLabel", is("0.1")))
                .andExpect(jsonPath("$.data.name", is("Testdocument")))
                .andExpect(jsonPath("$.data.checkinComment", is("Initial Version")));
    }

    @Test
    public void testListFolderWithPagination() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        buildTestFolder("FolderTest", folder);
        buildDocument("ADocument", folder);
        buildDocument("BDocument", folder);
        buildDocument("CDocument", folder);
        DataTablesRequest dataTablesRequest = new DataTablesRequest();
        dataTablesRequest.setFolderId(folder.getId());
        dataTablesRequest.setLength(100);
        dataTablesRequest.setWithFolder(VerteilungConstants.LIST_MODUS_ALL);
        this.mockMvc.perform(post("/listFolderWithPagination")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(dataTablesRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(JSONArray.class)))
                .andExpect(jsonPath("$.data.length()", is(4)))
                .andExpect(jsonPath("$.draw", is(0)))
                .andExpect(jsonPath("$.recordsTotal", is(4)))
                .andExpect(jsonPath("$.recordsFiltered", is(4)))
                .andExpect(jsonPath("$.parent", notNullValue()));
        dataTablesRequest.setWithFolder(VerteilungConstants.LIST_MODUS_DOCUMENTS);
        this.mockMvc.perform(post("/listFolderWithPagination")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(dataTablesRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(JSONArray.class)))
                .andExpect(jsonPath("$.data.length()", is(3)))
                .andExpect(jsonPath("$.draw", is(0)))
                .andExpect(jsonPath("$.recordsTotal", is(3)))
                .andExpect(jsonPath("$.recordsFiltered", is(3)))
                .andExpect(jsonPath("$.parent",notNullValue()));
        dataTablesRequest.setWithFolder(VerteilungConstants.LIST_MODUS_FOLDER);
        this.mockMvc.perform(post("/listFolderWithPagination")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(dataTablesRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(JSONArray.class)))
                .andExpect(jsonPath("$.data.length()", is(1)))
                .andExpect(jsonPath("$.draw", is(0)))
                .andExpect(jsonPath("$.recordsTotal", is(1)))
                .andExpect(jsonPath("$.recordsFiltered", is(1)))
                .andExpect(jsonPath("$.parent",notNullValue()));
        dataTablesRequest.setFolderId("99");
        this.mockMvc.perform(post("/listFolderWithPagination")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(dataTablesRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(JSONArray.class)))
                .andExpect(jsonPath("$.data.length()", is(0)))
                .andExpect(jsonPath("$.draw", is(0)))
                .andExpect(jsonPath("$.recordsTotal", is(0)))
                .andExpect(jsonPath("$.recordsFiltered", is(0)))
                .andExpect(jsonPath("$.parent", is("99")));
    }


    @Test
    public void testGetNode() throws Exception {
        RestRequest restRequest = new RestRequest();
        restRequest.setFilePath("/Datenverzeichnis");
        this.mockMvc.perform(post("/getNode")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.parentId", notNullValue()))
                .andExpect(jsonPath("$.data.path", is("/Datenverzeichnis")))
                .andExpect(jsonPath("$.data.name", is("Datenverzeichnis")));
        restRequest.setFilePath("/Datenverzeichnis/Skripte/backup.js.sample");
        this.mockMvc.perform(post("/getNode")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.name", is("backup.js.sample")))
                .andExpect(jsonPath("$.data.parents", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.parents.size()", is(1)));
        restRequest.setFilePath("/xyz");
        this.mockMvc.perform(post("/getNode")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    public void testGetNodeById() throws Exception {
        RestRequest restRequest = new RestRequest();
        restRequest.setDocumentId(con.getNode("/Datenverzeichnis/Skripte/backup.js.sample").getId());
        this.mockMvc.perform(post("/getNodeById")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.name", is("backup.js.sample")))
                .andExpect(jsonPath("$.data.parents", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.parents.size()", is(1)));
        restRequest.setDocumentId("9999");
        this.mockMvc.perform(post("/getNodeById")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    public void testFindDocument() throws Exception {
        DataTablesRequest dataTablesRequest = new DataTablesRequest();
        dataTablesRequest.setCmisQuery("SELECT * from cmis:document where cmis:name='backup.js.sample'");
        dataTablesRequest.setLength(-1);
        this.mockMvc.perform(post("/findDocument")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(dataTablesRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(JSONArray.class)))
                .andExpect(jsonPath("$.data[0].name", is("backup.js.sample")))
                .andExpect(jsonPath("$.data[0].parents", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data[0].parents.size()", is(1)))
                .andExpect(jsonPath("$.draw", is(0)))
                .andExpect(jsonPath("$.recordsTotal", is(1)))
                .andExpect(jsonPath("$.recordsFiltered", is(1)));
        dataTablesRequest.setCmisQuery("SELECT * from cmis:document where cmis:name='backup.js.samples'");
        this.mockMvc.perform(post("/findDocument")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(dataTablesRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(JSONArray.class)))
                .andExpect(jsonPath("$.data.length()", is(0)))
                .andExpect(jsonPath("$.draw", is(0)))
                .andExpect(jsonPath("$.recordsTotal", is(0)))
                .andExpect(jsonPath("$.recordsFiltered", is(0)));
    }


    @Test
    public void testQuery() throws Exception {
        DataTablesRequest dataTablesRequest = new DataTablesRequest();
        dataTablesRequest.setCmisQuery("SELECT * from cmis:document where cmis:name='backup.js.sample'");
        dataTablesRequest.setLength(-1);
        this.mockMvc.perform(post("/query")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(dataTablesRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(JSONArray.class)))
                .andExpect(jsonPath("$.data.length()", greaterThan(1)));
        dataTablesRequest.setCmisQuery("SELECT * from cmis:document where cmis:name='backup.js.samples'");
        this.mockMvc.perform(post("/query")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(dataTablesRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", nullValue()));
    }


    @Test
    public void testGetDocumentContent() throws Exception {
        RestRequest restRequest = new RestRequest();
        restRequest.setDocumentId(con.getNode("/Datenverzeichnis/Skripte/backup.js.sample").getId());
        restRequest.setExtract(false);
        this.mockMvc.perform(post("/getDocumentContent")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", startsWith("// ")));
        restRequest.setDocumentId("x");
        this.mockMvc.perform(post("/getDocumentContent")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    public void testUploadDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        RestRequest restRequest = new RestRequest();
        restRequest.setDocumentId(folder.getId());
        restRequest.setFileName(System.getProperty("user.dir") + filePdf);
        restRequest.setVersionState(VersioningState.MINOR.value());
        this.mockMvc.perform(post("/uploadDocument")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(String.class)));
        restRequest.setDocumentId("999");
        this.mockMvc.perform(post("/uploadDocument")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(restRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.data", nullValue()));
    }
}
