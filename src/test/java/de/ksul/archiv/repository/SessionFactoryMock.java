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

    private SessionFactory sessionFactory;
    private SessionImpl session;

    public SessionFactoryMock(SessionImpl session) {

    }

    public void setSession(SessionImpl session) {
        this.session = session;
    }

    public SessionFactory getSessionFactory() {
        if (sessionFactory == null)
            sessionFactory = getMock();
        return sessionFactory;
    }

    private SessionFactory getMock() {

        SessionFactory sessionFactory = mock(SessionFactory.class);
        when(sessionFactory.createSession(null)).thenThrow(new NullPointerException("Parameters null"));
        when(sessionFactory.createSession(new HashMap<>())).thenReturn(session);
        when(sessionFactory.getRepositories(null)).thenThrow(new NullPointerException("Parameters null"));
        return sessionFactory;

    }
}
