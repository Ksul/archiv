package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.QueryStatementImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionImpl;
import org.apache.chemistry.opencmis.client.runtime.cache.Cache;
import org.apache.chemistry.opencmis.client.runtime.repository.ObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 11:04
 */
public class SessionMock {

    private static SessionImpl session;

    public SessionMock(Repository repo) {
        if (session == null)
            session = getMock(repo);
    }

    public SessionImpl getSession() {
        return session;
    }

    private SessionImpl getMock(Repository repository) {

        SessionImpl session = mock(SessionImpl.class);

        ObjectFactory objectFactory = new ObjectFactoryImpl();
        objectFactory.initialize(session, null);
        RepositoryInfo repositoryInfo = new RepositoryInfoMock(repository).getRepositoryInfo();
        Cache cache = new CacheMock().getCache();
        CmisBinding binding = new CmisBindingMock(repository, session).getBinding();
        when(session.getBinding()).thenReturn(binding);
        when(session.getRepositoryInfo()).thenReturn(repositoryInfo);
        when(session.getRepositoryId()).thenReturn("0");
        when(session.getObjectFactory()).thenCallRealMethod();
        when(session.createOperationContext()).thenCallRealMethod();
        when(session.getDefaultContext()).thenReturn(new OperationContextImpl(null, false, true, false,
                IncludeRelationships.NONE, null, true, null, true, 100));

        when(session.createQueryStatement(any(String.class))).then(new Answer<QueryStatement>() {
            public QueryStatement answer(InvocationOnMock invocation) throws Throwable {
                return new QueryStatementImpl(session, (String) invocation.getArguments()[0]);
            }
        });
        when(session.query(anyString(), any(Boolean.class), any(OperationContext.class))).thenCallRealMethod();
        when(session.query(anyString(), any(Boolean.class))).thenCallRealMethod();
        when(session.queryObjects(anyString(), anyString(), anyBoolean(),any(OperationContext.class))).thenCallRealMethod();

        when(session.createDocument(anyMap(), any(ObjectId.class), any(ContentStream.class), any(VersioningState.class))).thenCallRealMethod();
        when(session.createDocument(anyMap(), any(ObjectId.class), any(ContentStream.class), any(VersioningState.class), any(), any(), any())).thenCallRealMethod();
        when(session.createFolder(anyMap(), any(ObjectId.class), any(), any(), any())).thenCallRealMethod();
        when(session.createFolder(anyMap(), any(ObjectId.class))).thenCallRealMethod();
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) throws Throwable {
                String objectId = ((ObjectId) invocation.getArguments()[0]).getId();
                FileableCmisObject cmisObject = repository.getById(objectId);
                repository.delete(cmisObject);
                return null;
            }

        }).when(session).delete(any(ObjectId.class), anyBoolean());
        when(session.deleteTree(any(ObjectId.class), anyBoolean(), any(UnfileObject.class), anyBoolean())).thenCallRealMethod();
        when(session.createObjectId(anyString())).thenCallRealMethod();
        when(session.getObject(any(ObjectId.class))).thenCallRealMethod();
        when(session.getObject(anyString(), any(OperationContext.class))).thenCallRealMethod();
        when(session.getObject(any(ObjectId.class), any(OperationContext.class))).thenCallRealMethod();
        when(session.getObjectByPath(anyString())).thenCallRealMethod();
        when(session.getObjectByPath(any(String.class), any(OperationContext.class))).thenCallRealMethod();
        when(session.getContentStream(any(ObjectId.class), any(), any(), any())).thenAnswer(new Answer<ContentStream>() {
            public ContentStream answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                Document document = (Document) args[0];
                return repository.getContent(document.getId());
            }
        });
        when(session.getTypeDefinition(anyString())).thenAnswer(new Answer<ObjectType>() {
            public ObjectType answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String string = (String) args[0];
                if (string.equalsIgnoreCase("cmis:document"))
                    return MockUtils.getInstance().getDocumentType();
                else if (string.equalsIgnoreCase("cmis:folder"))
                    return MockUtils.getInstance().getFolderType();
                else if (string.contains("my:archivContent"))
                    return MockUtils.getInstance().getArchivType();
                else if (string.startsWith("P:"))
                    return MockUtils.getInstance().getSecondaryTypeStore().get(string);
                else
                    return MockUtils.getInstance().getDocumentType();
            }
        });
        when(session.getTypeDefinition(anyString(), anyBoolean())).thenAnswer(new Answer<ObjectType>() {
            public ObjectType answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String string = (String) args[0];
                if (string.equalsIgnoreCase("cmis:document"))
                    return MockUtils.getInstance().getDocumentType();
                else if (string.equalsIgnoreCase("cmis:folder"))
                    return MockUtils.getInstance().getFolderType();
                else if (string.contains("my:archivContent"))
                    return MockUtils.getInstance().getArchivType();
                else if (string.startsWith("P:"))
                    return MockUtils.getInstance().getSecondaryTypeStore().get(string);
                return MockUtils.getInstance().getDocumentType();
            }
        });

        try {
            Field bindingField = SessionImpl.class.getDeclaredField("binding");
            bindingField.setAccessible(true);
            bindingField.set(session, binding);
            Field cacheField = SessionImpl.class.getDeclaredField("cache");
            cacheField.setAccessible(true);
            cacheField.set(session, cache);
            Field objectFactoryField = SessionImpl.class.getDeclaredField("objectFactory");
            objectFactoryField.setAccessible(true);
            objectFactoryField.set(session, objectFactory);
        } catch (Exception e) {
           throw new RuntimeException(e.getMessage());
        }
        return session;
    }

}
