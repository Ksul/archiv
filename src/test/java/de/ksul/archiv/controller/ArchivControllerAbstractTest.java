package de.ksul.archiv.controller;

import de.ksul.archiv.AlfrescoTest;
import de.ksul.archiv.FileEntry;
import de.ksul.archiv.VerteilungConstants;
import de.ksul.archiv.request.*;
import de.ksul.archiv.response.DataTablesResponse;
import de.ksul.archiv.response.MoveResponse;
import de.ksul.archiv.response.RestResponse;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.codec.binary.Base64;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.01.14
 * Time: 09:54
 * To change this template use File | Settings | File Templates.
 */

public abstract class ArchivControllerAbstractTest extends AlfrescoTest {


    protected ArchivController services;

    protected String filePdf;

    protected String fileTxt;

    protected String fileZip;


    @Test
    public void testListFolderWithPagination() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        buildDocument("ADocument", folder);
        buildDocument("BDocument", folder);
        buildDocument("CDocument", folder);
        buildDocument("DDocument", folder);
        DataTablesRequest dataTablesRequest = new DataTablesRequest();
        dataTablesRequest.setFolderId(folder.getId());
        dataTablesRequest.setWithFolder(VerteilungConstants.LIST_MODUS_DOCUMENTS);
        dataTablesRequest.setLength(2);
        dataTablesRequest.setStart(0);
        DataTablesResponse obj = services.listFolderWithPagination(dataTablesRequest);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(obj.getData(), instanceOf(ArrayList.class));
        assertThat(((ArrayList) obj.getData()).size(), is(2));
        assertThat(((Map) ((ArrayList) obj.getData()).get(0)).get("name"), is("DDocument"));
        assertThat(((Map) ((ArrayList) obj.getData()).get(1)).get("name"), is("CDocument"));
        dataTablesRequest.setStart(2);
        obj = services.listFolderWithPagination(dataTablesRequest);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(obj.getData(), instanceOf(ArrayList.class));
        assertThat(((ArrayList) obj.getData()).size(), is(2));
        assertThat(((Map) ((ArrayList) obj.getData()).get(0)).get("name"), is("BDocument"));
        assertThat(((Map) ((ArrayList) obj.getData()).get(1)).get("name"), is("ADocument"));
        ((Folder) folder).deleteTree(true, UnfileObject.DELETE, true);
    }

    @Test
    public void testListFolder() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);

        buildDocument("TestDocument", folder);
        buildDocument("TestDocument1", folder);
        buildDocument("TestDocument2", folder);
        buildTestFolder("FolderTest", folder);
        DataTablesRequest dataTablesRequest = new DataTablesRequest();
        dataTablesRequest.setFolderId(folder.getId());
        dataTablesRequest.setWithFolder(VerteilungConstants.LIST_MODUS_FOLDER);
        dataTablesRequest.setLength(-1);
        DataTablesResponse obj = services.listFolder(dataTablesRequest);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(obj.getData(), instanceOf(ArrayList.class));
        assertThat(((ArrayList) obj.getData()).size(), is(1));
        assertThat(((Map) ((ArrayList) obj.getData()).get(0)).get("name"), is("FolderTest"));
        assertThat(((Map) ((ArrayList) obj.getData()).get(0)).get("hasChildFolder"), is(false));
        assertThat(((Map) ((ArrayList) obj.getData()).get(0)).get("objectId"), Matchers.notNullValue());
        dataTablesRequest.setWithFolder(VerteilungConstants.LIST_MODUS_ALL);
        obj = services.listFolder(dataTablesRequest);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(obj.getData(), instanceOf(ArrayList.class));
        assertThat(((ArrayList) obj.getData()).size(), is(4));
        for (int i = 0; i < ((ArrayList) obj.getData()).size(); i++) {
            assertThat(((Map) ((ArrayList) obj.getData()).get(0)).get("name"), anyOf(is("TestDocument"), is("TestDocument1"), is("TestDocument2"), is("FolderTest")));
            assertThat(((Map) ((ArrayList) obj.getData()).get(0)).get("objectId"), Matchers.notNullValue());
        }
        dataTablesRequest.setWithFolder(VerteilungConstants.LIST_MODUS_DOCUMENTS);
        obj = services.listFolder(dataTablesRequest);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(obj.getData(), instanceOf(ArrayList.class));
        assertThat(((ArrayList) obj.getData()).size(), is(3));
        for (int i = 0; i < ((ArrayList) obj.getData()).size(); i++) {
            assertThat(((Map) ((ArrayList) obj.getData()).get(0)).get("name"), anyOf(is("TestDocument"), is("TestDocument1"), is("TestDocument2")));
            assertThat(((Map) ((ArrayList) obj.getData()).get(0)).get("objectId"), Matchers.notNullValue());
        }
    }


    @Test
    public void testGetNodeID() throws Exception {
        ObjectByPathRequest request = new ObjectByPathRequest();
        request.setFilePath("/");
        RestResponse obj = services.getNodeId(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        request.setFilePath("/Datenverzeichnis/Skripte/");
        obj = services.getNodeId(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        request.setFilePath("/Datenverzeichnis/Skripte/backup.js.sample");
        obj = services.getNodeId(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
    }


    @Test
    public void testGetNode() throws Exception {
        ObjectByPathRequest request = new ObjectByPathRequest();
        request.setFilePath("/");
        RestResponse obj = services.getNode(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        request.setFilePath("/Datenverzeichnis/Skripte/");
        obj = services.getNode(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        request.setFilePath("/Datenverzeichnis/Skripte/backup.js.sample");
        obj = services.getNode(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
    }

    @Test
    public void testGetNodeByID() throws Exception {
        RestResponse obj = services.getNodeId(new ObjectByPathRequest("/"));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        obj = services.getNodeById(new ObjectByIdRequest((String) obj.getData()));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        obj = services.getNodeId(new ObjectByPathRequest("/Datenverzeichnis/Skripte/backup.js.sample"));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        obj = services.getNodeById(new ObjectByIdRequest((String) obj.getData()));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
    }


    @Test
    public void testFindDocument() throws Exception {
        DataTablesRequest dataTablesRequest = new DataTablesRequest();
        dataTablesRequest.setCmisQuery("SELECT * from cmis:document where cmis:name='backup.js.sample'");
        dataTablesRequest.setLength(-1);
        DataTablesResponse obj = services.findDocument(dataTablesRequest);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(((ArrayList) obj.getData()).size(), Matchers.greaterThanOrEqualTo(1));
        Map data = (Map) ((ArrayList) obj.getData()).get(0);
        assertThat(data, Matchers.notNullValue());
        assertThat((String) data.get("name"), Matchers.equalToIgnoringCase("backup.js.sample"));
    }


    @Test
    public void testQuery() throws Exception {
        QueryRequest request = new QueryRequest();
        request.setCmisQuery("SELECT cmis:objectId, cmis:name from cmis:document where cmis:name='backup.js.sample'");
        RestResponse obj = services.query(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
    }


    @Test
    public void testGetDocumentContent() throws Exception {
        RestResponse obj = services.getDocumentContent(new ObjectByIdRequest((String) services.getNodeId(new ObjectByPathRequest("/Datenverzeichnis/Skripte/backup.js.sample")).getData()));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        String document = (String) obj.getData();
        assertThat(document.startsWith("//"), is(true));
    }

    @Test
    public void testUploadDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        UploadRequest request = new UploadRequest();
        request.setDocumentId(folder.getId());
        request.setFileName(System.getProperty("user.dir") + filePdf);
        request.setVersionState(VersioningState.MINOR.value());
        RestResponse obj = services.uploadDocument(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        obj = services.deleteDocument(new ObjectByIdRequest((String) services.getNodeId(new ObjectByPathRequest("/TestFolder/Test.pdf")).getData()));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
    }

    @Test
    public void testCreateDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        Map<String, Object> p1 = new HashMap<>();
        p1.put("cm:description", "Testdokument");
        Map<String, Object> extraProperties = new HashMap<>();
        extraProperties.put("P:cm:titled", p1);
        DocumentRequest request = new DocumentRequest();
        request.setDocumentId(folder.getId());
        request.setFileName("TestDocument.txt");
        request.setContent(Base64.encodeBase64String(content.getBytes("UTF-8")));
        request.setMimeType(VerteilungConstants.DOCUMENT_TYPE_TEXT);
        request.setExtraProperties(extraProperties);
        request.setVersionState(VersioningState.MINOR.value());
        RestResponse obj = services.createDocument(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        Map<String, Object> data = (Map) obj.getData();
        assertThat(data, Matchers.notNullValue());
        assertThat(((String) data.get("name")).equalsIgnoreCase("TestDocument.txt"), is(true));
        obj = services.getDocumentContent(new ObjectByIdRequest((String) data.get("objectId")));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        String document = (String) obj.getData();
        assertThat(document, is(content));
        obj = services.deleteDocument(new ObjectByIdRequest((String)  services.getNodeId(new ObjectByPathRequest("/TestFolder/TestDocument.txt")).getData()));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
    }

    @Test
    public void testCreateDocumentWithCustomModel() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        Map<String, Object> p1 = new HashMap<>();
        p1.put("cm:description", "Testdokument");
        Map<String, Object> p2 = new HashMap<>();
        p2.put("cm:sentdate", new Date().getTime());
        Map<String, Object> p3 = new HashMap<>();
        p3.put("my:amount", 25.33);
        Map<String, Object> p4 = new HashMap<>();
        p4.put("my:person", "Katja");
        p4.put("my:documentDate", new Date().getTime());
        Map<String, Object> extraProperties = new HashMap<>();
        extraProperties.put("P:cm:titled", p1);
        extraProperties.put("P:cm:emailed", p2);
        extraProperties.put("P:my:amountable", p3);
        extraProperties.put("D:my:archivContent", p4);
        DocumentRequest request = new DocumentRequest();
        request.setDocumentId(folder.getId());
        request.setFileName("TestDocument.txt");
        request.setContent(Base64.encodeBase64String(content.getBytes("UTF-8")));
        request.setMimeType(VerteilungConstants.DOCUMENT_TYPE_TEXT);
        request.setExtraProperties(extraProperties);
        request.setVersionState(VersioningState.MINOR.value());
        RestResponse obj = services.createDocument(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        Map<String, Object> data = (Map) obj.getData();
        assertThat(data, Matchers.notNullValue());
        assertThat((String) data.get("name"), Matchers.equalToIgnoringCase("TestDocument.txt"));
        obj = services.getDocumentContent(new ObjectByIdRequest((String) data.get("objectId")));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        String document = (String) obj.getData();
        assertThat(document, Matchers.equalTo(content));
        obj = services.deleteDocument(new ObjectByIdRequest((String) services.getNodeId(new ObjectByPathRequest("/TestFolder/TestDocument.txt")).getData()));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
    }

    @Test
    public void testCreateFolder() throws Exception {
        RestResponse obj = services.getNodeId(new ObjectByPathRequest("/"));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        Map<String, Object> p1 = new HashMap<>();
        p1.put("cm:title", "Testtitel");
        p1.put("cm:description", "Dies ist ein ArchivControllerTest Folder");
        Map<String, Object> p2 = new HashMap<>();
        p2.put("cmis:name", "Testfolder");
        Map<String, Object> extraProperties = new HashMap<>();
        extraProperties.put("cmis:folder", p2);
        extraProperties.put("P:cm:titled", p1);
        obj = services.createFolder(new PropertiesRequest((String) obj.getData(), extraProperties));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        Map<String, Object> data = (Map) obj.getData();
        assertThat(data, notNullValue());
        assertThat((String) data.get("name"), Matchers.equalToIgnoringCase("TestFolder"));
        obj = services.deleteFolder(new ObjectByIdRequest((String) data.get("objectId")));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
    }


    @Test
    public void testUpdateDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        Document document = (Document) buildDocument("TestDocument", folder);


        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        DocumentRequest request = new DocumentRequest();
        request.setDocumentId(document.getId());
        request.setContent(Base64.encodeBase64String(content.getBytes("UTF-8")));
        request.setMimeType(VerteilungConstants.DOCUMENT_TYPE_TEXT);
        request.setVersionState(VersioningState.MINOR.value());

        RestResponse obj = services.updateDocument(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        Map<String, Object> doc = (Map) obj.getData();
        obj = services.getDocumentContent(new ObjectByIdRequest((String) doc.get("objectId")));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(obj.getData(), Matchers.is(content));
        obj = services.deleteDocument(new ObjectByIdRequest((String) doc.get("objectId")));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        Map<String, Object> p4 = new HashMap<>();
        p4.put("my:person", "Katja");
        p4.put("my:documentDate", new Date().getTime());
        Map<String, Object> extraProperties = new HashMap<>();
        extraProperties.put("D:my:archivContent", p4);
        request.setDocumentId(folder.getId());
        request.setFileName("TestDocument.txt");
        request.setContent(Base64.encodeBase64String(content.getBytes("UTF-8")));
        request.setMimeType(VerteilungConstants.DOCUMENT_TYPE_TEXT);
        request.setExtraProperties(extraProperties);
        request.setVersionState(VersioningState.MAJOR.value());
        obj = services.createDocument(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        doc = (Map) obj.getData();
        assertThat(doc, Matchers.notNullValue());
        assertThat(doc.get("versionLabel"), Matchers.equalTo("1.0"));
        assertThat(doc.get("checkinComment"), Matchers.equalTo("Initial Version"));
        content = "Dies ist ein neuer Inhalt";
        request.setDocumentId((String) doc.get("objectId"));
        request.setContent(Base64.encodeBase64String(content.getBytes("UTF-8")));
        request.setExtraProperties(null);
        request.setVersionComment("neuer Versionskommentar");
        obj = services.updateDocument(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        Map<String, Object> data = (Map) obj.getData();
        assertThat(data, Matchers.notNullValue());
        assertThat(data.get("versionLabel"), equalTo("2.0"));
        assertThat(data.get("checkinComment"), equalTo("neuer Versionskommentar"));
        extraProperties.clear();
        Map<String, Object> p1 = new HashMap<>();
        p1.put("cm:description", "Testdokument");
        Map<String, Object> p2 = new HashMap<>();
        p2.put("cm:sentdate", new Date().getTime());
        Map<String, Object> p3 = new HashMap<>();
        p3.put("my:amount", 25.33);
        p3.put("my:tax", true);
        p4.put("my:person", "Katja");
        p4.put("my:documentDate", new Date().getTime());
        extraProperties.put("P:cm:titled", p1);
        extraProperties.put("P:cm:emailed", p2);
        extraProperties.put("P:my:amountable", p3);
        extraProperties.put("D:my:archivContent", p4);
        request.setDocumentId((String) data.get("objectId"));
        request.setContent(null);
        request.setExtraProperties(extraProperties);
        request.setVersionComment("2. Versionskommentar");
        obj = services.updateDocument(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        data = (Map) obj.getData();
        assertThat(data, Matchers.notNullValue());
        assertThat(data.get("versionLabel"), Matchers.equalTo("3.0"));
        assertThat(data.get("checkinComment"), Matchers.equalTo("2. Versionskommentar"));
        assertThat(((BigDecimal) data.get("amount")).doubleValue(), Matchers.equalTo(25.33));
        assertThat(data.get("tax"), Matchers.is(true));
        
        obj = services.deleteDocument(new ObjectByIdRequest((String) doc.get("objectId")));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
    }


    @Test
    public void testChangeFolder() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
       Map<String, Object> p1 = new HashMap<>();
        p1.put("cm:title", "Titel");
        p1.put("cm:description", "Beschreibung");
        Map<String, Object> p2 = new HashMap<>();
        p2.put("cmis:objectTypeId", "cmis:folder");
        p2.put("cmis:name", "FolderTest");
        Map<String, Object> extraProperties = new HashMap<>();
        extraProperties.put("cmis:folder", p2);
        extraProperties.put("P:cm:titled", p1);
        PropertiesRequest request = new PropertiesRequest();
        request.setDocumentId(folder.getId());
        request.setExtraProperties(extraProperties);
        RestResponse obj = services.updateProperties(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        folder.refresh();
        assertThat(folder.getName(), is(("FolderTest")));
        extraProperties.clear();
        p1.put("cm:title", "");
        p1.put("cm:description", "");
        p2.put("cmis:objectTypeId", "cmis:folder");
        p2.put("cmis:name", "Testfolder");
        extraProperties.put("cmis:folder", p2);
        extraProperties.put("P:cm:titled", p1);
        request.setDocumentId(folder.getId());
        request.setExtraProperties(extraProperties);
        obj = services.updateProperties(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());

        folder.refresh();
        //assertThat(folder.getName(), is(("TestFolder")));
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
        RestResponse obj = services.updateProperties(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());

        Map<String, Object> data = (Map) obj.getData();
        assertThat(data, Matchers.notNullValue());
        assertThat(data.get("title"), is("Testdokument"));
        assertThat(((BigDecimal) data.get("amount")).doubleValue(), is(25.33d));
        assertThat(data.get("tax"), is(true));
        assertThat(data.get("person"), is("Katja"));
        assertThat(data.get("documentDate"), is(time));
        assertThat(data.get("sentdate"), is(time));

        document.refresh();
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
        obj = services.updateProperties(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        data = (Map) obj.getData();
        assertThat(data, Matchers.notNullValue());
        assertThat(data.get("title"), is("Testdokument"));
        assertThat(((BigDecimal) data.get("amount")).doubleValue(), is(25.34d));
        assertThat(data.get("tax"), is(true));
        assertThat(data.get("idvalue"), is("123"));

        document.refresh();
        extraProperties.clear();
        p3.put("my:amount", null);
        p3.put("my:tax", null);
        extraProperties.put("P:cm:titled", p1);
        extraProperties.put("P:cm:emailed", p2);
        extraProperties.put("P:my:amountable", p3);
        extraProperties.put("D:my:archivContent", p4);
        request.setDocumentId(document.getId());
        request.setExtraProperties(extraProperties);
        obj = services.updateProperties(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        data = (Map) obj.getData();
        assertThat(data, Matchers.notNullValue());

        document.refresh();
        obj = services.deleteDocument(new ObjectByIdRequest(document.getId()));
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
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
        MoveResponse obj = services.moveNode(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        request.setDocumentId((String) services.getNodeId(new ObjectByPathRequest("/FolderTest/TestDocument")).getData());
        RestResponse resp = services.deleteDocument(new ObjectByIdRequest((String) services.getNodeId(new ObjectByPathRequest("/FolderTest/TestDocument")).getData()));
        assertThat(resp, notNullValue());
        assertThat(resp.getData() + (resp.hasError() ? resp.getError().getMessage() : ""), resp.isSuccess(), Matchers.is(true));
        assertThat(resp.getData(), notNullValue());
    }


    @Test
    public void testExtractPDFToInternalStorage() throws Exception {

        assertThat(filePdf, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + filePdf);
        assertThat(content.length, Matchers.greaterThan(0));
        ExtractRequest request = new ExtractRequest();
        request.setContent(Base64.encodeBase64String(content));
        request.setFileName(filePdf);
        RestResponse obj = services.extractPDFToInternalStorage(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(obj.getData(), is(1));
        assertThat(services.getEntries().size(), is(1));
        FileEntry entry = services.getEntries().iterator().next();
        assertThat(entry, Matchers.notNullValue());
        assertThat(entry.getName(), Matchers.equalTo(filePdf));
        assertThat(entry.getData().length, Matchers.greaterThan(0));
        assertThat(entry.getExtractedData().length(), Matchers.greaterThan(0));
        assertThat(entry.getExtractedData(), startsWith(getPDFCompareString()));
    }

    @Test
    public void testExtractPDFContent() throws Exception {
        assertThat(filePdf, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + filePdf);
        assertThat(content.length, Matchers.greaterThan(0));
        ContentRequest request = new ContentRequest();
        request.setContent(Base64.encodeBase64String(content));
        RestResponse obj = services.extractPDFContent(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat((String) obj.getData(), startsWith(getPDFCompareString()));
    }

    @Test
    public void testExtractPDFFile() throws Exception {
        assertThat(filePdf, Matchers.notNullValue());
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + filePdf;
        ObjectByPathRequest request = new ObjectByPathRequest();
        request.setFilePath(fullPath);
        RestResponse obj = services.extractPDFFile(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat((String) obj.getData(), startsWith(getPDFCompareString()));
    }

    @Test
    public void testExtractZIP() throws Exception {
        assertThat(fileZip, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileZip);
        assertThat(content.length, Matchers.greaterThan(0));
        ContentRequest request = new ContentRequest();
        request.setContent(Base64.encodeBase64String(content));
        RestResponse obj = services.extractZIP(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        ArrayList<String> erg = (ArrayList) obj.getData();
        assertThat(erg, Matchers.notNullValue());
        assertThat(erg.size(), is(2));
        String str = erg.get(0);
        assertThat(str.length(), Matchers.greaterThan(0));
        str = erg.get(1);
        assertThat(str.length(), Matchers.greaterThan(0));
    }

    @Test
    public void testExtractZIPToInternalStorage() throws Exception {
        assertThat(fileZip, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileZip);
        assertThat(content.length, Matchers.greaterThan(0));
        ContentRequest request = new ContentRequest();
        request.setContent(Base64.encodeBase64String(content));
        RestResponse obj = services.extractZIPToInternalStorage(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(obj.getData(), is(2));
        assertThat(services.getEntries().size(), is(2));
        for (FileEntry entry : services.getEntries()) {
            assertThat(entry.getName().isEmpty(), is(false));
            assertThat(entry.getData().length, Matchers.greaterThan(0));
            assertThat(entry.getExtractedData() == null || entry.getExtractedData().isEmpty(), Matchers.is(true));
        }
    }

    @Test
    public void testExtractZipAndExtractPDFToInternalStorage() throws Exception {
        assertThat(fileZip, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileZip);
        assertThat(content.length, Matchers.greaterThan(0));
        ContentRequest request = new ContentRequest();
        request.setContent(Base64.encodeBase64String(content));
        RestResponse obj = services.extractZIPAndExtractPDFToInternalStorage(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(obj.getData(), is(2));
        assertThat(services.getEntries().size(), is(2));
        for (FileEntry entry : services.getEntries()) {
            assertThat(entry.getName().isEmpty(), is(false));
            assertThat(entry.getData().length, Matchers.greaterThan(0));
            assertThat(entry.getExtractedData().isEmpty(), is(false));
        }
    }

    @Test
    public void testGetDataFromInternalStorage() throws Exception {
        services.getEntries().clear();
        RestResponse obj = services.getCompleteDataFromInternalStorage();
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.getData(), Matchers.notNullValue());
        assertThat(obj.isSuccess(), is(false));
        services.getEntries().add(new FileEntry("Test1", new byte[]{0, 1, 2}, "ArchivControllerTest Inhalt 1"));
        services.getEntries().add(new FileEntry("Test2", new byte[]{2, 3, 4}, "ArchivControllerTest Inhalt 2"));
        obj = services.getCompleteDataFromInternalStorage();
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        Map<String, Object> data = (Map) obj.getData();
        assertThat(data, Matchers.notNullValue());
        assertThat(data.size(), is(2));
        assertThat(data.containsKey("Test1"), is(true));
        assertThat(data.containsKey("Test2"), is(true));
        assertThat(data.get("Test1"), instanceOf(Map.class));
        assertThat(data.get("Test2"), instanceOf(Map.class));
        Map<String, Object> entry = (Map<String, Object>) data.get("Test1");
        assertThat(new String(Base64.decodeBase64((String) entry.get("data"))), equalTo((new String(new byte[]{0, 1, 2}))));
        assertThat(entry.get("extractedData"), equalTo("ArchivControllerTest Inhalt 1"));
        entry = (Map) data.get("Test2");
        assertThat(new String(Base64.decodeBase64((String) entry.get("data"))), equalTo(new String(new byte[]{2, 3, 4})));
        assertThat(entry.get("extractedData"), equalTo("ArchivControllerTest Inhalt 2"));
        FileNameRequest request = new FileNameRequest();
        request.setFileName("Test2");
        obj = services.getDataFromInternalStorage(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        data = (Map) obj.getData();
        assertThat(data, Matchers.notNullValue());
        assertThat(data.size(), is(1));
        assertThat(data.containsKey("Test2"), is(true));
        assertThat(data.get("Test2"), instanceOf(Map.class));
        entry = (Map) data.get("Test2");
        assertThat(new String(Base64.decodeBase64((String) entry.get("data"))), equalTo(new String(new byte[]{2, 3, 4})));
        assertThat(entry.get("extractedData"), equalTo("ArchivControllerTest Inhalt 2"));
        request.setFileName("Test3");
        obj = services.getDataFromInternalStorage(request);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.getData(), Matchers.notNullValue());
        assertThat(obj.isSuccess(), is(false));
        services.getEntries().clear();
    }

    @Test
    public void testClearInternalStoreage() throws Exception {
        services.getEntries().add(new FileEntry("Test1", new byte[]{0, 1, 2}, "ArchivControllerTest Inhalt 1"));
        RestResponse obj = services.clearInternalStorage();
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(services.getEntries().isEmpty(), is(true));
    }

    @Test
    public void testOpenFilePdf() throws Exception {
        assertThat(filePdf, Matchers.notNullValue());
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + filePdf;
        ObjectByPathRequest request = new ObjectByPathRequest();
        request.setFilePath(fullPath);
        RestResponse obj = services.openFile(request);
        obj.setSuccess(false);
        byte[] content = readFile(System.getProperty("user.dir") + filePdf);
        byte[] contentRead = Base64.decodeBase64((String) obj.getData());
        assertThat("Unterschiedliche Länge gelesen!", content.length == contentRead.length, Matchers.is(true));
        for (int i = 0; i < content.length; i++) {
            assertThat("Unterschiedlicher Inhalt gelesen Position: " + i + " !", content[i] == contentRead[i], Matchers.is(true));
        }
    }

    @Test
    public void testOpenFileTxt() throws Exception {
        assertThat(fileTxt, Matchers.notNullValue());
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileTxt;
        ObjectByPathRequest request = new ObjectByPathRequest();
        request.setFilePath(fullPath);
        RestResponse obj = services.openFile(request);
        obj.setSuccess(false);
        byte[] content = readFile(System.getProperty("user.dir") + fileTxt);
        byte[] contentRead = Base64.decodeBase64((String) obj.getData());
        assertThat("Unterschiedliche Länge gelesen!", content.length == contentRead.length, Matchers.is(true));
        for (int i = 0; i < content.length; i++) {
            assertThat("Unterschiedlicher Inhalt gelesen Position: " + i + " !", content[i] == contentRead[i], Matchers.is(true));
        }
    }

    @Test
    public void testIsURLAvailable() throws Exception {

        ConnectionRequest request = new ConnectionRequest();
        request.setServer("http://www.spiegel.de");
        request.setTimeout(5000);
        RestResponse obj = services.isURLAvailable(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        request.setServer("http://www.spiegel.dumm");
        obj = services.isURLAvailable(request);
        assertThat(obj, notNullValue());
        assertThat(obj.isSuccess(), Matchers.is(false));
    }


}
