package de.schulte.archiv.configuration;

import de.schulte.archiv.AlfrescoConnector;
import de.schulte.archiv.repository.CMISSessionGenerator;
import de.schulte.archiv.repository.CMISSessionGeneratorMockImpl;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 25.11.16
 * Time: 12:40
 */
@Configuration
public class ArchivTestConfiguration {

    @Bean
    public Session getSession() {
        Session session;
        CMISSessionGenerator gen = new CMISSessionGeneratorMockImpl();
        session = gen.generateSession();
        return session;
    }

    @Bean
    AlfrescoConnector getConnector() {
        return  new AlfrescoConnector(getSession(), null, null, null, null);

    }

}
