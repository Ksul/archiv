package de.ksul.archiv.controller;

import de.ksul.archiv.AlfrescoConnector;
import de.ksul.archiv.ArchivTestApplication;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.request.QueryRequest;
import de.ksul.archiv.response.RestResponse;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 28.11.16
 * Time: 13:15
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.MOCK, classes = {ArchivTestConfiguration.class, ArchivTestApplication.class})
@TestPropertySource(properties={"ksul.archiv.testing.testData="})
@DirtiesContext
public class ArchivControllerTest extends  ArchivControllerAbstractTest {

    @BeforeEach
    public void setup(@Autowired AlfrescoConnector connector) throws Exception{

        con = connector;
        Assertions.assertNotNull(con);
        services = new ArchivController(con);
        filePdf = "/src/test/resources/Test.pdf";
        fileTxt = "/src/test/resources/test.txt";
        fileZip = "/src/test/resources/Sample.zip";
        super.setUp();
    }

    @AfterEach
    public void shutDown() throws Exception{
        super.shutDown();
    }

    @Test
    public void testSearch() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("Test", folder);
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
