package de.ksul.archiv.configuration;

import de.ksul.archiv.AlfrescoConnector;
import de.ksul.archiv.repository.CMISSessionGenerator;
import de.ksul.archiv.repository.CMISSessionGeneratorMock;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 25.11.16
 * Time: 12:40
 */
@Configuration
public class ArchivTestConfiguration {

    @Bean
    @Autowired
    public Session getSession(ResourceLoader resourceLoader) {
        Session session;
        CMISSessionGenerator gen = new CMISSessionGeneratorMock(resourceLoader);
        session = gen.generateSession();
        return session;
    }

    @Bean
    @Autowired
    AlfrescoConnector getConnector(ResourceLoader resourceLoader) {
        return  new AlfrescoConnector(getSession(resourceLoader), null, null, null, null);

    }

}
