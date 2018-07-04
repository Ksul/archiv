package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionImpl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.VersioningService;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 10:12
 */
public class VersionServiceMock {

    private VersioningService versioningService;

    public VersionServiceMock(Repository repository) {
        if (versioningService == null)
            versioningService = getMock(repository);
    }

    public VersioningService getVersioningService() {
        return versioningService;
    }

    private VersioningService getMock(Repository repository) {
        VersioningService versioningService = mock(VersioningService.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Holder<String> holder = (Holder) invocation.getArguments()[1];
                String[] parts = holder.getValue().split(";");
                Document document = (Document) repository.getById(parts[0]);
                if (document.isPrivateWorkingCopy()) throw new CmisVersioningException();
                ((PropertyImpl) document.getProperty("cmis:isPrivateWorkingCopy")).setValue(true);
                return null;
            }
        }).when(versioningService).checkOut(anyString(), any(Holder.class), any(), any());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Holder<String> holder = (Holder) invocation.getArguments()[1];
                boolean major = (boolean) invocation.getArguments()[2];
                Properties properties = (Properties) invocation.getArguments()[3];
                ContentStream stream = (ContentStream) invocation.getArguments()[4];
                String checkinComment = (String) invocation.getArguments()[5];
                String[] parts = holder.getValue().split(";");
                Document document = (Document) repository.getById(parts[0]);
                if (!document.isPrivateWorkingCopy()) throw new CmisVersioningException();
                String version;
                if (major)
                    version = new BigDecimal(document.getProperty("cmis:versionLabel").getValueAsString()).add(new BigDecimal("1")).toString();
                else
                    version = new BigDecimal(document.getProperty("cmis:versionLabel").getValueAsString()).add(new BigDecimal("0.1")).toString();
                document = (Document) repository.makeNewVersion(parts[0], version);
                if (properties != null && properties.getProperties() != null && !properties.getProperties().isEmpty()) {
                    document.updateProperties(properties.getProperties());
                }
                if (stream != null) {
                    repository.changeContent(document, stream);
                }
                holder.setValue(parts[0] + ";" + version);
                if (checkinComment != null && !checkinComment.isEmpty())
                    ((PropertyImpl) document.getProperty("cmis:checkinComment")).setValue(checkinComment);
                ((PropertyImpl) document.getProperty("cmis:lastModificationDate")).setValue(MockUtils.getInstance().copyDateTimeValue(new Date().getTime()));

                return null;
            }
        }).when(versioningService).checkIn(anyString(), any(Holder.class), anyBoolean(), any(Properties.class), any(), any(), any(), any(), any(), any());
        return versioningService;
    }
}
