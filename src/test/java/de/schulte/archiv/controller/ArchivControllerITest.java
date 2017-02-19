package de.schulte.archiv.controller;

import de.schulte.archiv.AlfrescoConnector;
import de.schulte.archiv.AlfrescoTest;
import de.schulte.archiv.FileEntry;
import de.schulte.archiv.VerteilungConstants;
import de.schulte.archiv.configuration.ArchivConfiguration;
import de.schulte.archiv.configuration.ArchivProperties;
import de.schulte.archiv.configuration.ArchivTestConfiguration;
import de.schulte.archiv.configuration.ArchivTestProperties;
import de.schulte.archiv.request.ConnectionRequest;
import de.schulte.archiv.request.DataTablesRequest;
import de.schulte.archiv.request.RestRequest;
import de.schulte.archiv.response.DataTablesResponse;
import de.schulte.archiv.response.MoveResponse;
import de.schulte.archiv.response.RestResponse;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.codec.binary.Base64;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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


}
