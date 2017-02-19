package de.schulte.archiv.controller;

import de.schulte.archiv.AlfrescoConnector;
import de.schulte.archiv.ArchivException;
import de.schulte.archiv.VerteilungConstants;
import de.schulte.archiv.configuration.ArchivConfiguration;
import de.schulte.archiv.configuration.ArchivTestConfiguration;
import de.schulte.archiv.configuration.ArchivTestProperties;
import de.schulte.archiv.request.DataTablesRequest;
import de.schulte.archiv.request.RestRequest;
import de.schulte.archiv.response.DataTablesResponse;
import de.schulte.archiv.response.RestResponse;
import org.apache.chemistry.opencmis.client.api.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.apache.commons.codec.binary.*;
import org.apache.commons.codec.binary.Base64;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 28.11.16
 * Time: 13:15
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ArchivTestConfiguration.class})
@DirtiesContext
public class ArchivControllerTest extends  ArchivControllerAbstractTest {


    @Autowired
    AlfrescoConnector con;

    @Before
    public void setup() throws Exception{
        setCon(con);
        super.setUp();
        services = new ArchivController(con);
        filePdf = "/src/test/resources/Test.pdf";
        fileTxt = "/src/test/resources/test.txt";
        fileZip = "/src/test/resources/Sample.zip";
    }

    @After
    public void shutDown() throws Exception{
        super.shutDown();
    }

}
