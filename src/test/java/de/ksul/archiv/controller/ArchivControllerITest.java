package de.ksul.archiv.controller;

import de.ksul.archiv.AlfrescoConnector;
import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivProperties;
import de.ksul.archiv.configuration.ArchivTestProperties;
import de.ksul.archiv.request.CommentRequest;
import de.ksul.archiv.request.ObjectByIdRequest;
import de.ksul.archiv.request.QueryRequest;
import de.ksul.archiv.response.RestResponse;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.01.14
 * Time: 09:54
 * To change this template use File | Settings | File Templates.
 */
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@EnableConfigurationProperties({ArchivTestProperties.class})
@SpringBootTest(classes = {ArchivConfiguration.class})
public class ArchivControllerITest extends  ArchivControllerAbstractTest  {

    ArchivProperties properties;

    @BeforeEach
    public void setUp(@Autowired AlfrescoConnector connector,
                      @Autowired ArchivProperties properties,
                      @Autowired ArchivTestProperties testProperties) throws Exception {
        con = connector;
        Assertions.assertNotNull(con);
        services = new ArchivController(con);
        Assertions.assertNotNull(services);
        Assertions.assertNotNull(properties);
        this.properties = properties;
        Assertions.assertNotNull(testProperties);
        filePdf = testProperties.getTestPDF();
        fileTxt = testProperties.getTestTXT();
        fileZip = testProperties.getTestZIP();
        super.setUp();
    }

    @AfterEach
    public void shutDown() throws Exception {
        super.shutDown();
    }

    @Test
    public void testGetConnection() throws Exception {
        RestResponse obj = services.getConnection();
        assertThat(obj.getData(), Matchers.notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        Map<String, Object> connection = (Map) obj.getData();
        assertThat(connection.get("server"), Matchers.equalTo(properties.getServer()));
        assertThat(connection.get("binding"), Matchers.equalTo(properties.getBinding()));
        assertThat(connection.get("user"), Matchers.equalTo(properties.getUser()));
        assertThat(connection.get("password"), Matchers.equalTo(properties.getPassword()));
    }


    @Test
    public void testGetComments() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setDocumentId(document.getId());
        commentRequest.setComment("Testkommentar");
        RestResponse obj = services.addComment(commentRequest);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());

        obj = services.getComments(new ObjectByIdRequest(document.getId()));
        Map<String, Object> comment = (Map) obj.getData();
        assertThat(((Map) ((ArrayList) comment.get("items")).get(0)).get("content"), is("Testkommentar"));

        document.delete();
        folder.delete(true);
    }

    @Test
    public void testSearch() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("Test", folder);
        // der Server braucht einige Zeit um das neu angelegte Dokument zu indexieren.
        Thread.sleep(15000);
        String searchText ="Test";
        QueryRequest request = new QueryRequest();
        request.setCmisQuery( "select d.cmis:objectId, d.cmis:name, d.cmis:creationDate, d.my:documentDate, o.cm:title  from my:archivContent as d " +
                "join cm:titled as o on d.cmis:objectId = o.cmis:objectId " +
                "WHERE IN_TREE(d, '" + folder.getId() + "') AND ( CONTAINS(d, 'cmis:name:*" + searchText + "* OR TEXT:" + searchText + "') OR CONTAINS(o, 'cm:title:*" + searchText + "*'))");
        RestResponse obj = services.query(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(((ArrayList) obj.getData()).size(), Matchers.greaterThan(0));
        searchText ="T1est";
        request.setCmisQuery( "select d.cmis:objectId, d.cmis:name, d.cmis:creationDate, d.my:documentDate, o.cm:title  from my:archivContent as d " +
                "join cm:titled as o on d.cmis:objectId = o.cmis:objectId " +
                "WHERE IN_TREE(d, '" + folder.getId() + "') AND ( CONTAINS(d, 'cmis:name:*" + searchText + "* OR TEXT:" + searchText + "') OR CONTAINS(o, 'cm:title:*" + searchText + "*'))");
        obj = services.query(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());
        assertThat(((ArrayList) obj.getData()).size(), Matchers.equalTo(0));
    }


}
