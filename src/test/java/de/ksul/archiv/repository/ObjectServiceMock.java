package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.FolderType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.ObjectServiceImpl;
import org.apache.chemistry.opencmis.client.runtime.*;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Date;
import java.util.UUID;

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
 * Time: 09:57
 */
public class ObjectServiceMock {


    private static ObjectServiceImpl objectService;

    public ObjectServiceMock(Repository repository, SessionImpl sessionImpl) {
        if (objectService == null)
            objectService = getMock(repository, sessionImpl);
    }

    public ObjectServiceImpl getObjectService() {
        return objectService;
    }

    private ObjectServiceImpl getMock(Repository repository, SessionImpl sessionImpl) {

        ObjectServiceImpl objectService = mock(ObjectServiceImpl.class);
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Holder<String> idHolder = (Holder) invocation.getArguments()[1];
                String objectId = idHolder.getValue();
                boolean overwrite = invocation.getArgument(2);
                UUID uuid = repository.getContentService().createContent(objectId, ((ContentStream) invocation.getArguments()[4]), overwrite);
                DocumentImpl document = (DocumentImpl) repository.getById(objectId);
                ((PropertyImpl) document.getProperty("cmis:contentStreamId")).setValue(uuid);

                //ContentStreamImpl stream = (ContentStreamImpl) repository.getContent(new ObjectIdImpl(objectId));
                //stream.setStream(((ContentStream) invocation.getArguments()[4]).getStream());
                return null;
            }

        }).when(objectService).setContentStream(anyString(), any(), anyBoolean(), any(), any(ContentStream.class), any());
        when(objectService.getObject(anyString(), anyString(), any(), anyBoolean(), any(IncludeRelationships.class), anyString(), anyBoolean(), anyBoolean(), any())).thenAnswer(new Answer<ObjectData>() {
            public ObjectData answer(InvocationOnMock invocation) throws Throwable {
                String objectId = (String) invocation.getArguments()[1];
                FileableCmisObject cmisObject = repository.getById(objectId);
                if (cmisObject == null)
                    throw new CmisObjectNotFoundException((String) invocation.getArguments()[1] + " not found!");
                return MockUtils.getInstance().getObjectDataFromCmisObject(cmisObject);
            }
        });
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                String objectId = (String) ((Holder) invocation.getArguments()[1]).getValue();

                repository.update(objectId, ((Properties) invocation.getArguments()[3]).getProperties());
                return null;
            }
        }).when(objectService).updateProperties(anyString(), any(Holder.class), any(Holder.class), any(Properties.class), any());
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                String objectId = (String) ((Holder) invocation.getArguments()[1]).getValue();
                String targetFolderId = (String) invocation.getArguments()[2];
                String sourceFolderId = (String) invocation.getArguments()[3];
                FileableCmisObject cmisObject = repository.getById(objectId);
                repository.move(targetFolderId, cmisObject);
                for (Property property : cmisObject.getProperties()) {
                    if (property.getId().equalsIgnoreCase("cmis:parentId")) {
                        ((PropertyImpl) property).setValue(targetFolderId);
                        break;
                    }
                }
                return null;
            }
        }).when(objectService).moveObject(anyString(), any(Holder.class), anyString(), anyString(), any());
/*        when(objectService.createFolder(anyString(), any(Properties.class), anyString(), anyListOf(String.class), any(Acl.class), any(Acl.class), any(ExtensionsData.class))).thenAnswer(new Answer<ObjectId>() {

            public ObjectId answer(InvocationOnMock invocation) throws Throwable {

                return createFileableCmisObject(invocation, true);
            }
        });*/
    return objectService;
    }
}
