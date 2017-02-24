package de.ksul.archiv.controller;

import de.ksul.archiv.AlfrescoConnector;
import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivProperties;
import de.ksul.archiv.configuration.ArchivTestProperties;
import de.ksul.archiv.request.RestRequest;
import de.ksul.archiv.response.RestResponse;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
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
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@EnableConfigurationProperties({ArchivTestProperties.class})
@SpringBootTest(classes = {ArchivConfiguration.class})
public class ArchivControllerITest extends  ArchivControllerAbstractTest  {

    @Autowired
    ArchivTestProperties testProperties;

    @Autowired
    ArchivProperties properties;

    @Autowired
    AlfrescoConnector con;

    @Before
    public void setUp() throws Exception {
        setCon(con);
        super.setUp();
        services = new ArchivController(con);
        assertThat(services, Matchers.notNullValue());
        filePdf = testProperties.getTestPDF();
        fileTxt = testProperties.getTestTXT();
        fileZip = testProperties.getTestZIP();
    }

    @After
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
        RestRequest request = new RestRequest();
        request.setDocumentId(document.getId());
        request.setComment("Testkommentar");
        RestResponse obj = services.addComment(request);
        assertThat(obj, notNullValue());
        assertThat(obj.getData() + (obj.hasError() ? obj.getError().getMessage() : ""), obj.isSuccess(), Matchers.is(true));
        assertThat(obj.getData(), notNullValue());

        obj = services.getComments(request);
        Map<String, Object> comment = (Map) obj.getData();
        assertThat(((Map) ((ArrayList) comment.get("items")).get(0)).get("content"), is("Testkommentar"));

        document.delete();
    }


}
