package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.QueryStatementImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionImpl;
import org.apache.chemistry.opencmis.client.runtime.cache.Cache;
import org.apache.chemistry.opencmis.client.runtime.repository.ObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 11:04
 */
public class SessionMock {

    private SessionImpl session;
    private CmisBindingMock cmisBindingMock;
    private RepositoryInfoMock repositoryInfoMock;
    private CacheMock cacheMock;

    public SessionMock() {
        cmisBindingMock = new CmisBindingMock();
        repositoryInfoMock = new RepositoryInfoMock();
        cacheMock = new CacheMock();
    }

    public SessionImpl getSession() {
        if (session == null)
            session = getMock();
        return session;
    }

    public SessionMock setRepository(Repository repository) {
        cmisBindingMock.setRepository(repository);
        repositoryInfoMock.setRepository(repository);
        return this;
    }

    private SessionImpl getMock() {

        SessionImpl session = mock(SessionImpl.class);

        ObjectFactory objectFactory = new ObjectFactoryImpl();
        objectFactory.initialize(session, null);
        RepositoryInfo repositoryInfo = repositoryInfoMock.getRepositoryInfo();
        Cache cache = cacheMock.getCache();
        CmisBinding binding = cmisBindingMock.getBinding();
        when(session.getBinding()).thenReturn(binding);
        when(session.getRepositoryInfo()).thenReturn(repositoryInfo);
        when(session.getRepositoryId()).thenReturn("0");
        when(session.getObjectFactory()).thenCallRealMethod();
        when(session.createOperationContext()).thenCallRealMethod();
        when(session.getDefaultContext()).thenReturn(new OperationContextImpl(null, false, true, false,
                IncludeRelationships.NONE, null, true, null, true, 100));

        when(session.createQueryStatement(any(String.class))).thenCallRealMethod();
        when(session.query(anyString(), any(Boolean.class), any(OperationContext.class))).thenCallRealMethod();
        when(session.query(anyString(), any(Boolean.class))).thenCallRealMethod();
        when(session.queryObjects(anyString(), anyString(), anyBoolean(), any(OperationContext.class))).thenCallRealMethod();

        when(session.createDocument(anyMap(), any(ObjectId.class), any(ContentStream.class), any(VersioningState.class))).thenCallRealMethod();
        when(session.createDocument(anyMap(), any(ObjectId.class), any(ContentStream.class), any(VersioningState.class), any(), any(), any())).thenCallRealMethod();
        when(session.createFolder(anyMap(), any(ObjectId.class), any(), any(), any())).thenCallRealMethod();
        when(session.createFolder(anyMap(), any(ObjectId.class))).thenCallRealMethod();
        doCallRealMethod().when(session).delete(any(ObjectId.class), anyBoolean());
        when(session.deleteTree(any(ObjectId.class), anyBoolean(), any(UnfileObject.class), anyBoolean())).thenCallRealMethod();
        when(session.createObjectId(anyString())).thenCallRealMethod();
        when(session.getObject(any(ObjectId.class))).thenCallRealMethod();
        when(session.getObject(anyString(), any(OperationContext.class))).thenCallRealMethod();
        when(session.getObject(any(ObjectId.class), any(OperationContext.class))).thenCallRealMethod();
        when(session.getObjectByPath(anyString())).thenCallRealMethod();
        when(session.getObjectByPath(any(String.class), any(OperationContext.class))).thenCallRealMethod();
        when(session.getContentStream(any(ObjectId.class), any(), any(), any())).thenCallRealMethod();
        when(session.getTypeDefinition(anyString())).thenCallRealMethod();
        when(session.getTypeDefinition(anyString(), anyBoolean())).thenCallRealMethod();

        Map<String, String> parameter = new HashMap<>();
        parameter.put(SessionParameter.CACHE_SIZE_OBJECTS, "5");
        parameter.put(SessionParameter.CACHE_SIZE_LINKS, "5");
        parameter.put(SessionParameter.CACHE_SIZE_REPOSITORIES, "2");


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
            Field lockField = SessionImpl.class.getDeclaredField("lock");
            lockField.setAccessible(true);
            lockField.set(session, new ReentrantReadWriteLock());
            Field parametersField = SessionImpl.class.getDeclaredField("parameters");
            parametersField.setAccessible(true);
            parametersField.set(session, parameter);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return session;
    }

}
