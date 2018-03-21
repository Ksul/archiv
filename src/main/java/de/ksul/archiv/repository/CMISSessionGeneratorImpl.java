package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * Klasse erzeugt eine CMIS Session
 * User: Klaus Schulte (m500288)
 * Date: 08.01.14
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public class CMISSessionGeneratorImpl implements CMISSessionGenerator {

    private static Logger logger = LoggerFactory.getLogger(CMISSessionGeneratorImpl.class.getName());
    private String user;
    private String password;
    private String atomPubURL;
    private String repositoryName;

    public CMISSessionGeneratorImpl(String user, String password, String atomPubURL, String repositoryName) {
        this.user = user;
        this.password = password;
        this.atomPubURL = atomPubURL;
        this.repositoryName = repositoryName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String getAtomPubURL() {
        return atomPubURL;
    }

    @SuppressWarnings("unused")
    public void setAtomPubURL(String atomPubURL) {
        this.atomPubURL = atomPubURL;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    @SuppressWarnings("unused")
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    /**
     * baut die Session auf
     *
     * @return die Session
     */
    @Override
    public Session generateSession() {

        Session session;

        try {

            // From: http://chemistry.apache.org/java/examples/example-create-session.html
            // default factory implementation
            SessionFactory factory = SessionFactoryImpl.newInstance();
            Map<String, String> parameter = new HashMap<>();

            // user credentials
            parameter.put(SessionParameter.USER, getUser());
            parameter.put(SessionParameter.PASSWORD, getPassword());

            // connection settings
            parameter.put(SessionParameter.ATOMPUB_URL, getAtomPubURL());
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
//		parameter.put(SessionParameter.REPOSITORY_ID, this.repositoryName);
//		Session session = factory.createSession(parameter);

            // Set the alfresco object factory
            //parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

            // create session

            List<Repository> repositories = factory.getRepositories(parameter);
            session = repositories.get(0).createSession();

        } catch (Exception e) {
            logger.error("Session konnte nicht aufgebaut werden: " + e.getMessage());
            throw e;
        }
        return session;
    }

}

