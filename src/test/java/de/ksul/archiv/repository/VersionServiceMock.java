package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.VersioningService;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 10:12
 */
public class VersionServiceMock {

    private static VersioningService versioningService;

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
                ((PropertyImpl) document.getProperty(PropertyIds.IS_PRIVATE_WORKING_COPY)).setValue(true);
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
                    version = new BigDecimal(document.getProperty(PropertyIds.VERSION_LABEL).getValueAsString()).add(new BigDecimal("1")).toString();
                else
                    version = new BigDecimal(document.getProperty(PropertyIds.VERSION_LABEL).getValueAsString()).add(new BigDecimal("0.1")).toString();
                if (checkinComment != null && !checkinComment.isEmpty())
                    ((PropertyImpl) document.getProperty(PropertyIds.CHECKIN_COMMENT)).setValue(checkinComment);
                ((PropertyImpl) document.getProperty(PropertyIds.LAST_MODIFICATION_DATE)).setValue(MockUtils.getInstance().copyDateTimeValue(new Date().getTime()));


                ((PropertyImpl) document.getProperty(PropertyIds.VERSION_LABEL)).setValue(version);
                ((PropertyImpl) document.getProperty(PropertyIds.OBJECT_ID)).setValue(document.getProperty(PropertyIds.VERSION_SERIES_ID).getValueAsString() + ";" + version);
                ((PropertyImpl) document.getProperty(PropertyIds.IS_PRIVATE_WORKING_COPY)).setValue(false);

                if (stream != null) {
                    repository.changeContent(document, stream);
                }

                document = (Document) repository.makeNewVersion(parts[0], version);
                if (properties != null && properties.getProperties() != null && !properties.getProperties().isEmpty()) {
                    document.updateProperties(properties.getProperties());
                }

                holder.setValue(parts[0] + ";" + version);


                return null;
            }
        }).when(versioningService).checkIn(anyString(), any(Holder.class), anyBoolean(), any(Properties.class), any(), any(), any(), any(), any(), any());

        when(versioningService.getAllVersions(anyString(), anyString(), anyString(), any(), anyBoolean(), any())).then(new Answer<List<ObjectData>>() {
            @Override
            public List<ObjectData> answer(InvocationOnMock invocation) throws Throwable {
                String objectId = (String) invocation.getArgument(1);
                return repository.getAllVersions(objectId);
            }
        });

        return versioningService;
    }
}
