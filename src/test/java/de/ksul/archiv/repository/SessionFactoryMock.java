package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionImpl;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 14:44
 */
public class SessionFactoryMock {

    private static SessionFactory sessionFactory;

    public SessionFactoryMock(SessionImpl session) {
        if (sessionFactory == null)
            sessionFactory = getMock(session);
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private SessionFactory getMock(SessionImpl sessionImpl) {

        SessionFactory sessionFactory = mock(SessionFactory.class);
        when(sessionFactory.createSession(null)).thenThrow(new NullPointerException("Parameters null"));
        when(sessionFactory.createSession(new HashMap<>())).thenReturn(sessionImpl);
        when(sessionFactory.getRepositories(null)).thenThrow(new NullPointerException("Parameters null"));
        return sessionFactory;

    }
}
