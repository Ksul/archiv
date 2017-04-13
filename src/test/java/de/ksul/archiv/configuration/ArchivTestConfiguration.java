package de.ksul.archiv.configuration;

import de.ksul.archiv.AlfrescoConnector;
import de.ksul.archiv.repository.CMISSessionGenerator;
import de.ksul.archiv.repository.CMISSessionGeneratorMock;
import org.apache.chemistry.opencmis.client.api.Session;
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
        CMISSessionGenerator gen = new CMISSessionGeneratorMock();
        session = gen.generateSession();
        return session;
    }

    @Bean
    AlfrescoConnector getConnector() {
        return  new AlfrescoConnector(getSession(), null, null, null, null);

    }

}
