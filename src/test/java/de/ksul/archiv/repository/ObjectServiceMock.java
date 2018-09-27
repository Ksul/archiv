package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.ObjectServiceImpl;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 09:57
 */
public class ObjectServiceMock {


    private ObjectServiceImpl objectService;
    private Repository repository;

    public ObjectServiceMock() {
    }

    public ObjectServiceMock setRepository(Repository repository) {
        this.repository = repository;
        return this;
    }

    public ObjectServiceImpl getObjectService() {
        if (objectService == null)
            objectService = getMock();
        return objectService;
    }

    private ObjectServiceImpl getMock() {

        objectService = mock(ObjectServiceImpl.class);
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Holder<String> idHolder = (Holder) invocation.getArguments()[1];
                String objectId = idHolder.getValue();
                boolean overwrite = invocation.getArgument(2);
                repository.createContent(objectId, ((ContentStream) invocation.getArguments()[4]), overwrite);
                return null;
            }

        }).when(objectService).setContentStream(anyString(), any(), anyBoolean(), any(), any(ContentStream.class), any());
        when(objectService.getObject(anyString(), anyString(), any(), anyBoolean(), any(IncludeRelationships.class), anyString(), anyBoolean(), anyBoolean(), any())).thenAnswer(new Answer<ObjectData>() {
            public ObjectData answer(InvocationOnMock invocation) throws Throwable {
                String objectId = (String) invocation.getArguments()[1];
                FileableCmisObject cmisObject = repository.getById(objectId);
                if (cmisObject == null)
                    throw new CmisObjectNotFoundException((String) invocation.getArguments()[1] + " not found!");
                return MockUtils.getInstance().getObjectDataFromProperties(cmisObject.getProperties());
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

                return null;
            }
        }).when(objectService).moveObject(anyString(), any(Holder.class), anyString(), anyString(), any());
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                repository.delete((String) invocation.getArguments()[1]);
                return null;
            }
        }).when(objectService).deleteObject(anyString(), anyString(), anyBoolean(), any());
        when(objectService.getObjectByPath(anyString(), anyString(), any(), anyBoolean(), any(IncludeRelationships.class), anyString(), anyBoolean(), anyBoolean(), any())).then(new Answer<ObjectData>() {
            @Override
            public ObjectData answer(InvocationOnMock invocation) {
                FileableCmisObject cmisObject = repository.getByPath((String) invocation.getArguments()[1]);
                if (cmisObject == null)
                    throw new CmisObjectNotFoundException((String) invocation.getArguments()[0] + " not found!");
                return MockUtils.getInstance().getObjectDataFromProperties(cmisObject.getProperties());
            }
        });
        when(objectService.deleteTree(anyString(), anyString(), anyBoolean(), any(UnfileObject.class), anyBoolean(), any())).then(new Answer<FailedToDeleteData>() {
            @Override
            public FailedToDeleteData answer(InvocationOnMock invocation) throws Throwable {
                repository.deleteTree((String) invocation.getArguments()[1]);
                return new FailedToDeleteDataImpl();
            }
        });
        when(objectService.createDocument(anyString(), any(Properties.class), anyString(), any(), any(VersioningState.class), any(), any(), any(), any())).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {

                ObjectType objectType;
                PropertiesImpl props = (PropertiesImpl) invocation.getArguments()[1];
                objectType = MockUtils.getInstance().getDocumentType(((String) props.getProperties().get(PropertyIds.OBJECT_TYPE_ID).getFirstValue()));
                String name = (String) props.getProperties().get(PropertyIds.NAME).getFirstValue();
                TreeNode parent = repository.findTreeNodeForId((String) invocation.getArguments()[2]);
                String path = parent.getPath();
                FileableCmisObject newObject = MockUtils.getInstance().createFileableCmisObject(repository, props.getProperties(), path, name, objectType, ((ContentStream) invocation.getArguments()[3]).getMimeType());
                repository.insert(parent, newObject, (ContentStream) invocation.getArguments()[3], ((VersioningState) invocation.getArguments()[4]).equals(VersioningState.MAJOR) ? "1.0" : "0.1", true);

                return newObject.getId();
            }
        });
        when(objectService.createFolder(anyString(), any(Properties.class), anyString(), any(), any(), any(), any())).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {

                PropertiesImpl props = (PropertiesImpl) invocation.getArguments()[1];

                String name = (String) props.getProperties().get(PropertyIds.NAME).getFirstValue();
                TreeNode parent = repository.findTreeNodeForId((String) invocation.getArguments()[2]);
                String path = parent.getPath();
                FileableCmisObject newObject;
                newObject = MockUtils.getInstance().createFileableCmisObject(repository, props.getProperties(), path, name, MockUtils.getInstance().getFolderType("cmis:folder"), null);
                repository.insert(parent, newObject, true);

                return newObject.getId();
            }
        });
        when(objectService.getContentStream(anyString(), anyString(), any(), any(), any(), any())).then(new Answer<ContentStream>() {
            @Override
            public ContentStream answer(InvocationOnMock invocation) throws Throwable {
                return repository.getContent((String) invocation.getArguments()[1]);
            }
        });


        return objectService;
    }
}
