package de.ksul.archiv.configuration;

import de.ksul.archiv.AlfrescoConnector;
import de.ksul.archiv.repository.CMISSessionGenerator;
import de.ksul.archiv.repository.CMISSessionGeneratorMock;
import de.ksul.archiv.repository.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 25.11.16
 * Time: 12:40
 */
@Configuration
@EnableConfigurationProperties({ArchivProperties.class, ArchivTestProperties.class})
@ComponentScan(basePackages = "de.ksul.archiv.repository")
public class ArchivTestConfiguration {

    public ArchivTestConfiguration() {

    }

    @Bean
    @Autowired
    public Session getSession(CMISSessionGeneratorMock cmisSessionGeneratorMock) {
        Session session;
        session = cmisSessionGeneratorMock.generateSession();
        return session;
    }


    @Bean
    @Autowired
    AlfrescoConnector getConnector(Session session, ArchivProperties archivProperties) {
        return  new AlfrescoConnector(session, null, null, null, null, archivProperties.getCompanyHomeName(), archivProperties.getDataDictionaryName(), archivProperties.getScriptDirectoryName());

    }

}
