package de.ksul.archiv.configuration;

import de.ksul.archiv.AlfrescoConnector;
import de.ksul.archiv.repository.CMISSessionGenerator;
import de.ksul.archiv.repository.CMISSessionGeneratorImpl;
import org.apache.chemistry.opencmis.client.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 18.11.16
 * Time: 11:46
 */
@Configuration
@EnableConfigurationProperties({ArchivProperties.class})
public class ArchivConfiguration {

    private static Logger logger = LoggerFactory.getLogger(ArchivConfiguration.class.getName());


    private ArchivProperties archivProperties;

    @Autowired
    public ArchivConfiguration(ArchivProperties archivProperties) {
        this.archivProperties = archivProperties;
    }

    @Bean
    public Session getSession() {
        Session session;
        try {
            CMISSessionGenerator gen = new CMISSessionGeneratorImpl(archivProperties.getUser(), archivProperties.getPassword(), archivProperties.getBinding(), "Session");
            session = gen.generateSession();
            logger.trace(" Mit den Parametern Server: " + archivProperties.getServer() + " Binding: " + archivProperties.getBinding() + " User: " + archivProperties.getUser() + " Password: " + archivProperties.getPassword() + " konnte eine Cmis Session erfolgreich etabliert werden!");
            return session;
        } catch (Exception e) {
            logger.error(" Mit den Parametern Server: " + archivProperties.getServer() + " Binding: " + archivProperties.getBinding() + " User: " + archivProperties.getUser() + " Password: " + archivProperties.getPassword() + " konnte keine Cmis Session etabliert werden!");
            return null;
        }
    }

    @Bean
    AlfrescoConnector getConnector() {
        return new AlfrescoConnector(getSession(), archivProperties.getServer(), archivProperties.getBinding(), archivProperties.getUser(), archivProperties.getPassword());

    }

}
