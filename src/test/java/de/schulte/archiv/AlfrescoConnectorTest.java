package de.schulte.archiv;

import de.schulte.archiv.configuration.ArchivConfiguration;
import de.schulte.archiv.configuration.ArchivTestConfiguration;
import de.schulte.archiv.configuration.ArchivTestProperties;
import de.schulte.archiv.model.Column;
import de.schulte.archiv.model.Order;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 09.01.14
 * Time: 13:06
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@EnableConfigurationProperties({ArchivTestProperties.class})
@SpringBootTest(classes = {ArchivConfiguration.class})
public class AlfrescoConnectorTest extends AlfrescoTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Autowired
    ArchivTestProperties testProperties;

    @Autowired
    AlfrescoConnector con;

    @Before
    public void setUp() throws Exception {
        setCon(con);
        super.setUp();
    }

        @Test
    public void testGetTicket() throws Exception {
        String ticket = con.getTicket();
        assertThat(ticket, Matchers.notNullValue());
        assertThat(ticket, Matchers.startsWith("TICKET_"));
    }


    @Test
    public void testListFolder() throws Exception {

        CmisObject folder = buildTestFolder("TestFolder", null);

        buildTestFolder("Folder", folder);
        buildDocument("ADocument", folder);
        CmisObject cmisObject= buildDocument("BDocument", folder);
        buildDocument("CDocument", folder);
        Map<String, Object> properties = new HashMap<>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:my:archivContent");
        properties.put("cm:title", "ATitel");
        con.updateProperties(cmisObject,  properties);
        List<Order> orders = new ArrayList();
        orders.add(new Order(0, "ASC"));
        List<Column> columns = new ArrayList();
        columns.add(new Column(null, "cmis:name", false, false, null, false));
        ItemIterable<CmisObject> list = con.listFolder(folder.getId(), orders, columns, VerteilungConstants.LIST_MODUS_ALL);
        assertThat(list, Matchers.notNullValue());
        int count = 0;
        for (CmisObject obj : list) {
            switch (count) {
                case 0:
                    assertThat(obj.getName(), Matchers.is("ADocument"));
                    break;
                case 1:
                    assertThat(obj.getName(), Matchers.is("BDocument"));
                    break;
                case 2:
                    assertThat(obj.getName(), Matchers.is("CDocument"));
                    break;
                case 3:
                    assertThat(obj.getName(), Matchers.is("Folder"));
                    break;
            }
            count++;
        }
        assertThat(count, Matchers.is(4));
        orders.get(0).setDir("DESC");
        list = con.listFolder(folder.getId(), orders, columns, VerteilungConstants.LIST_MODUS_ALL);
        assertThat(list, Matchers.notNullValue());
        count = 0;
        for (CmisObject obj : list) {
            switch (count) {
                case 0:
                    assertThat(obj.getName(), Matchers.is("Folder"));
                    break;
                case 1:
                    assertThat(obj.getName(), Matchers.is("CDocument"));
                    break;
                case 2:
                    assertThat(obj.getName(), Matchers.is("BDocument"));
                    break;
                case 3:
                    assertThat(obj.getName(), Matchers.is("ADocument"));
                    break;
            }
            count++;
        }
        assertThat(count, Matchers.is(4));
        list = con.listFolder(folder.getId(), orders, columns, VerteilungConstants.LIST_MODUS_DOCUMENTS);
        assertThat(list, Matchers.notNullValue());
        count = 0;
        for (CmisObject obj : list) {
            switch (count) {
                case 0:
                    assertThat(obj.getName(), Matchers.is("CDocument"));
                    break;
                case 1:
                    assertThat(obj.getName(), Matchers.is("BDocument"));
                    break;
                case 2:
                    assertThat(obj.getName(), Matchers.is("ADocument"));
                    break;
            }
            count++;
        }
        assertThat(count, Matchers.is(3));
        columns.get(0).setName("cm:title");
        list = con.listFolder(folder.getId(), orders, columns, VerteilungConstants.LIST_MODUS_DOCUMENTS);
        assertThat(list, Matchers.notNullValue());
        count = 0;
        for (CmisObject obj : list) {
            switch (count) {
                case 0:
                    assertThat(obj.getName(), Matchers.is("BDocument"));
                    break;
                case 1:
                    assertThat(obj.getName(), Matchers.is("ADocument"));
                    break;
                case 2:
                    assertThat(obj.getName(), Matchers.is("CDocument"));
                    break;
            }
            count++;
        }
        assertThat(count, Matchers.is(3));
        columns.get(0).setName("cmis:name");
        list = con.listFolder(folder.getId(), orders, columns, VerteilungConstants.LIST_MODUS_FOLDER);
        assertThat(list, Matchers.notNullValue());
        count = 0;
        for (CmisObject obj : list) {
            switch (count) {
                case 0:
                    assertThat(obj.getName(), Matchers.is("Folder"));
                    break;
            }
            count++;
        }
        assertThat(count, Matchers.is(1));

        ((Folder) folder).deleteTree(true, UnfileObject.DELETE, true);
    }



    @Test
    public void testGetNode() throws Exception {
        CmisObject cmisObject;
        cmisObject = con.getNode("/Sites");
        assertThat(cmisObject, Matchers.notNullValue());
        assertThat(cmisObject, Matchers.instanceOf(Folder.class));
        cmisObject = con.getNode("/Datenverzeichnis");
        assertThat(cmisObject, Matchers.notNullValue());
        assertThat(cmisObject, Matchers.instanceOf(Folder.class));
        cmisObject = con.getNode("/Datenverzeichnis/Skripte/backup.js.sample");
        assertThat(cmisObject, Matchers.notNullValue());
        assertThat(cmisObject, Matchers.instanceOf(Document.class));
    }

    @Test
    public void testCheckout() throws Exception {
        CmisObject cmisObject = con.getNode("/Datenverzeichnis/Skripte/backup.js.sample");
        CmisObject cmisObjectCheckedOut = con.checkOutDocument((Document) cmisObject);
        thrown.expect(CmisVersioningException.class);
        con.checkOutDocument((Document) cmisObject);
        con.cancelCheckout((Document) cmisObject);
    }

    @Test
    public void testFindDocument() throws Exception{
        ItemIterable<CmisObject> erg = con.findDocument("SELECT * from cmis:document where cmis:name='backup.js.sample'", null, null);
        assertThat(erg, Matchers.notNullValue());
        assertThat(erg.getTotalNumItems(), Matchers.is(1L));
        Document doc = (Document) erg.iterator().next();
        assertThat(doc.getName(), Matchers.equalTo("backup.js.sample"));
    }

    @Test
    public void testQuery() throws Exception {
        List<List<PropertyData<?>>> erg = con.query("SELECT cmis:name, cmis:objectId from cmis:document where cmis:name='backup.js.sample'");
        assertThat(erg, Matchers.notNullValue());
        assertThat(erg.size(), Matchers.is(1));
        assertThat(erg.get(0).get(0).getFirstValue(), Matchers.equalTo("backup.js.sample"));
    }

    @Test
    public void testTotalNumItems() throws Exception {
        ItemIterable<CmisObject> result = null;
        CmisObject cmisObject = con.getSession().getObjectByPath("/");
        Folder folder = (Folder) cmisObject;
        OperationContext operationContext = con.getSession().createOperationContext();
        //operationContext.setMaxItemsPerPage(2);
        QueryStatement stmt = con.getSession().createQueryStatement("IN_FOLDER(?)");
        stmt.setString(1, folder.getId());
        result = con.getSession().queryObjects("cmis:folder", stmt.toString(), false, operationContext);
        long totalNumItems = result.getTotalNumItems();
        operationContext.setMaxItemsPerPage(1);
        result = con.getSession().queryObjects("cmis:folder", stmt.toString(), false, operationContext);
        // Fehler, sollte eigentlich gleich sein
        assertThat(result.getTotalNumItems(), Matchers.not(totalNumItems));
    }

    @Test
    public void testGetDocumentContent() throws Exception{
        byte[] content = con.getDocumentContent((Document) con.findDocument("SELECT * from cmis:document where cmis:name='backup.js.sample'", null, null).iterator().next());
        assertThat(content, Matchers.notNullValue());
        assertThat(content.length, Matchers.greaterThan(0));
        String document =  new String(content, Charset.forName("UTF-8"));
        assertThat(document, Matchers.startsWith("//"));
    }

    @Test
    public void testUploadDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        String id = con.uploadDocument(((Folder) folder), new File(System.getProperty("user.dir") + testProperties.getTestPDF()), "application/pdf", VersioningState.MINOR);
        assertThat(id, Matchers.notNullValue());
        CmisObject document = con.getNode("/TestFolder/ArchivControllerTest.pdf");
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf( Document.class));
        document.delete(true);
    }

    @Test
    public void testCreateDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        Map<String, Object> properties = new HashMap<>();
        List<String> aspects = new ArrayList<>();
        aspects.add("P:cm:titled");
        properties.put("cm:description","Testdokument");
        properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects);
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), VerteilungConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR);
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf( Document.class));
        assertThat(document.getName(), Matchers.equalTo("TestDocument.txt"));
        assertThat(IOUtils.toString(document.getContentStream().getStream(), "UTF-8"), Matchers.equalTo("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?"));
        document.delete(true);
    }

    @Test
    public void testCreateArchivDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        Map<String, Object> properties = new HashMap<>();
        List<String> aspects = new ArrayList<>();
        properties.put("cm:description","Testdokument");
        properties.put("my:amount","25.33");
        properties.put("my:person", "Klaus");
        properties.put("my:documentDate", Long.toString(new Date().getTime()));
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:my:archivContent");
        aspects.add("P:cm:titled");
        aspects.add("P:my:amountable");
        properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects);
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), VerteilungConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR);
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf( Document.class));
        assertThat(document.getName(), Matchers.equalTo("TestDocument.txt"));
        assertThat(IOUtils.toString(document.getContentStream().getStream(), "UTF-8"), Matchers.equalTo("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?"));
        document.delete(true);
    }

    @Test
    public void testUpdateProperties() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        Map<String, Object> properties = new HashMap<>();
        List<String> aspects = new ArrayList<>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:my:archivContent");
        properties.put("cm:description","Testdokument");
        properties.put("my:amount","25.33");
        properties.put("my:tax","true");
        aspects.add("P:cm:titled");
        aspects.add("P:my:amountable");
        properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects);
        document = con.updateProperties(document,  properties);
        assertThat(document, Matchers.notNullValue());
        assertThat(((List<String>) document.getPropertyValue(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)).contains("P:cm:titled"), Matchers.is(true));
        assertThat(((BigDecimal) document.getProperty("my:amount").getValue()).doubleValue(), Matchers.equalTo(new BigDecimal(25.33).doubleValue()));
        assertThat(document.getProperty("my:tax").getValue(), Matchers.is(true));
        document.delete(true);
    }

    @Test
    public void testUpdateDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        Document document = (Document) buildDocument("TestDocument", folder);
        Date date = new Date();
        String content = "";
        Map<String, Object> properties = new HashMap<>();
        content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        properties.clear();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:my:archivContent");
        List<String> aspects = new ArrayList<>();
        properties.put("my:amount", 24.33);
        aspects.add("P:my:amountable");
        properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects);
        document = con.updateDocument(document, content.getBytes(), VerteilungConstants.DOCUMENT_TYPE_TEXT,  properties, VersioningState.MINOR, null);
        byte[] cont = con.getDocumentContent(document );
        assertThat(cont, Matchers.notNullValue());
        assertThat(cont, Matchers.instanceOf(byte[].class));
        assertThat(content, Matchers.equalTo(new String(cont)));

        assertThat(document.getVersionLabel(), Matchers.equalTo("0.2"));
        properties.clear();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:my:archivContent");
        properties.put("my:amount", 23.33);

        document = con.updateDocument(document, content.getBytes(), VerteilungConstants.DOCUMENT_TYPE_TEXT,  properties, VersioningState.MINOR, null);
        // wegen einer zusätzlichen Version durch die Aspekte
        assertThat(document.getVersionLabel(), Matchers.equalTo("0.3"));
        assertThat(((BigDecimal) document.getProperty("my:amount").getValue()).doubleValue(), Matchers.equalTo(new BigDecimal(23.33).doubleValue()));
        document.delete(true);

        document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), VerteilungConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MAJOR);
        assertThat(document.getVersionLabel(), Matchers.equalTo("1.0"));
        assertThat(document.getCheckinComment(), Matchers.equalTo("Initial Version"));
        content = "Dies ist ein neuer Inhalt";

        document = con.updateDocument(document, content.getBytes(), VerteilungConstants.DOCUMENT_TYPE_TEXT,  null, VersioningState.MAJOR, "neuer Versionskommentar");
        cont = con.getDocumentContent(document );
        assertThat(cont, Matchers.notNullValue());
        assertThat(cont, Matchers.instanceOf(byte[].class));
        assertThat(content, Matchers.equalTo(new String(cont)));
        assertThat(document.getVersionLabel(), Matchers.equalTo("2.0"));
        assertThat(document.getCheckinComment(), Matchers.equalTo("neuer Versionskommentar"));
        properties.clear();
        aspects.clear();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:my:archivContent");
        properties.put("cm:description","Testdokument");
        properties.put("my:amount", 25.33);

        properties.put("cm:sentdate", date.getTime());
        aspects.add("P:my:amountable");
        aspects.add("P:cm:titled");
        aspects.add("P:cm:emailed");
        properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects);

        document = con.updateDocument(document, null, VerteilungConstants.DOCUMENT_TYPE_TEXT,  properties, VersioningState.MAJOR, "2. Versionskommentar");
        assertThat(document, Matchers.notNullValue());
        assertThat(document.getVersionLabel(), Matchers.equalTo("3.0"));
        assertThat(document.getCheckinComment(), Matchers.equalTo("2. Versionskommentar"));
        assertThat(((List<String>) document.getPropertyValue(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)).contains("P:my:amountable"), Matchers.is(true));
        assertThat(((BigDecimal) document.getProperty("my:amount").getValue()).doubleValue(), Matchers.equalTo(new BigDecimal(25.33).doubleValue()));
        assertThat(((List<String>) document.getPropertyValue(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)).contains("P:cm:emailed"), Matchers.is(true));
        assertThat(((GregorianCalendar) document.getProperty("cm:sentdate").getValue()).getTime().getTime(), Matchers.equalTo(date.getTime()));
        assertThat(document.getProperty("cm:description").getValueAsString(), Matchers.equalTo("Testdokument"));
        cont = con.getDocumentContent(document );
        assertThat(cont, Matchers.notNullValue());
        assertThat(cont, Matchers.instanceOf(byte[].class));
        assertThat(content, Matchers.equalTo(new String(cont)));
        document.delete(true);
    }

    @Test
    public void testMoveNode() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        CmisObject newFolder = buildTestFolder("FolderTest", null);
        assertThat(newFolder, Matchers.notNullValue());
        assertThat(newFolder, Matchers.instanceOf( Folder.class));
        CmisObject cmisObject = con.moveNode((Document) document, (Folder) folder, (Folder) newFolder);
        assertThat(cmisObject, Matchers.notNullValue());
        assertThat(cmisObject, Matchers.instanceOf( Document.class));
        assertThat(cmisObject.getName(), Matchers.equalTo("TestDocument"));
        CmisObject obj = con.getNode("/FolderTest/TestDocument");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj, Matchers.instanceOf( Document.class));
        assertThat(obj.getName(), Matchers.equalTo("TestDocument"));
        cmisObject.delete(true);
    }


    @Test
    public void testGetComments() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        Map<String, Object> abd = con.addComment(document,"Testkommentar");
        assertThat(abd, Matchers.notNullValue());
        Map<String, Object>  result =  con.getComments(document);
        assertThat(((Map) ((ArrayList) result.get("items")).get(0)).get("content"), Matchers.equalTo("Testkommentar"));
        document.delete(true);
    }


    @Test
    public void testCreateFolder() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        ((Folder) folder).deleteTree(true, UnfileObject.DELETE, true);
    }




}
