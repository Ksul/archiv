package de.ksul.archiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ksul.archiv.request.*;
import net.minidev.json.JSONArray;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.Charset;
import java.util.Date;
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
    public void testOpenDocument() throws Exception {
        ObjectByIdRequest request = new ObjectByIdRequest();
        request.setDocumentId(con.getNode("/Datenverzeichnis/Skripte/backup.js.sample").getId());
        this.mockMvc.perform(post("/openDocument")
                .contentType(APPLICATION_JSON_UTF8)
                .content( objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.name", is("backup.js.sample")))
                .andExpect(jsonPath("$.mimeType", is("application/x-javascript")))
                .andExpect(jsonPath("$.data", startsWith(Base64.encodeBase64String("// ".getBytes()))));
        request.setDocumentId("1");
        this.mockMvc.perform(post("/openDocument")
                .contentType(APPLICATION_JSON_UTF8)
                .content( objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @Test
    public void testGetNodeId() throws Exception {
        ObjectByPathRequest request = new ObjectByPathRequest();
        request.setFilePath("/");
        this.mockMvc.perform(post("/getNodeId")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", notNullValue()));
        request.setFilePath("/xyz");
        this.mockMvc.perform(post("/getNodeId")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", nullValue()))
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
        DocumentRequest request = new DocumentRequest();
        request.setDocumentId(folder.getId());
        request.setFileName("Testdocument");
        request.setContent(Base64.encodeBase64String("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?".getBytes()));
        request.setMimeType("text");
        request.setVersionState(VersioningState.MINOR.value());
        request.setExtraProperties(extraProperties);
        this.mockMvc.perform(post("/createDocument")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
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
        ObjectByPathRequest request = new ObjectByPathRequest();
        request.setFilePath("/Datenverzeichnis");
        this.mockMvc.perform(post("/getNode")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.parentId", notNullValue()))
                .andExpect(jsonPath("$.data.path", is("/Datenverzeichnis")))
                .andExpect(jsonPath("$.data.name", is("Datenverzeichnis")));
        request.setFilePath("/Datenverzeichnis/Skripte/backup.js.sample");
        this.mockMvc.perform(post("/getNode")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.name", is("backup.js.sample")))
                .andExpect(jsonPath("$.data.parents", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.parents.size()", is(1)));
        request.setFilePath("/xyz");
        this.mockMvc.perform(post("/getNode")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    public void testGetNodeById() throws Exception {
        ObjectByIdRequest request = new ObjectByIdRequest();
        request.setDocumentId(con.getNode("/Datenverzeichnis/Skripte/backup.js.sample").getId());
        this.mockMvc.perform(post("/getNodeById")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.name", is("backup.js.sample")))
                .andExpect(jsonPath("$.data.parents", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.parents.size()", is(1)));
        request.setDocumentId("9999");
        this.mockMvc.perform(post("/getNodeById")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is5xxServerError())
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
        ObjectByIdRequest request = new ObjectByIdRequest();
        request.setDocumentId(con.getNode("/Datenverzeichnis/Skripte/backup.js.sample").getId());
        this.mockMvc.perform(post("/getDocumentContent")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", startsWith("// ")));
        request.setDocumentId("x");
        this.mockMvc.perform(post("/getDocumentContent")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    public void testUploadDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        UploadRequest request = new UploadRequest();
        request.setDocumentId(folder.getId());
        request.setFileName(System.getProperty("user.dir") + filePdf);
        request.setVersionState(VersioningState.MINOR.value());
        this.mockMvc.perform(post("/uploadDocument")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(String.class)));
        request.setDocumentId("999");
        this.mockMvc.perform(post("/uploadDocument")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    public void testUpdateProperties() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        long time = new Date().getTime();
        Map<String, Object> p1 = new HashMap<>();
        p1.put("cm:description", "Testdokument");
        p1.put("cm:title", "Testdokument");
        Map<String, Object> p2 = new HashMap<>();
        p2.put("cm:sentdate", time);
        Map<String, Object> p3 = new HashMap<>();
        p3.put("my:amount", 25.33);
        p3.put("my:tax", true);
        Map<String, Object> p4 = new HashMap<>();
        p4.put("my:person", "Katja");
        p4.put("my:documentDate", time);
        Map<String, Object> p5 = new HashMap<>();
        p5.put("my:idvalue", null);
        Map<String, Object> extraProperties = new HashMap<>();
        extraProperties.put("P:cm:titled", p1);
        extraProperties.put("P:cm:emailed", p2);
        extraProperties.put("P:my:amountable", p3);
        extraProperties.put("D:my:archivContent", p4);
        extraProperties.put("P:my:idable", p5);
        PropertiesRequest request = new PropertiesRequest();
        request.setDocumentId(document.getId());
        request.setExtraProperties(extraProperties);
        this.mockMvc.perform(post("/updateProperties")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.amount", is(25.33)))
                .andExpect(jsonPath("$.data.person", is("Katja")))
                .andExpect(jsonPath("$.data.versionLabel", is("0.1")))
                .andExpect(jsonPath("$.data.name", is("TestDocument")))
                .andExpect(jsonPath("$.data.tax", is(true)));
        extraProperties.clear();
        p3.put("my:amount", 25.34);
        p5.put("my:idvalue", "123");
        extraProperties.put("P:cm:titled", p1);
        extraProperties.put("P:cm:emailed", p2);
        extraProperties.put("P:my:amountable", p3);
        extraProperties.put("D:my:archivContent", p4);
        extraProperties.put("P:my:idable", p5);
        request.setDocumentId(document.getId());
        request.setExtraProperties(extraProperties);
        this.mockMvc.perform(post("/updateProperties")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.amount", is(25.34)))
                .andExpect(jsonPath("$.data.person", is("Katja")))
                .andExpect(jsonPath("$.data.versionLabel", is("0.1")))
                .andExpect(jsonPath("$.data.name", is("TestDocument")))
                .andExpect(jsonPath("$.data.tax", is(true)))
                .andExpect(jsonPath("$.data.idvalue", is("123")));
        request.setDocumentId("x");
        this.mockMvc.perform(post("/updateProperties")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    public void testMoveNode() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        CmisObject newFolder = buildTestFolder("FolderTest", null);
        MoveRequest request = new MoveRequest();
        request.setDocumentId(document.getId());
        request.setCurrentLocationId(folder.getId());
        request.setDestinationId(newFolder.getId());
        this.mockMvc.perform(post("/moveNode")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.data", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.parents", isA(LinkedHashMap.class)))
                .andExpect(jsonPath("$.data.parents.0.name", is("FolderTest")));
    }
}
