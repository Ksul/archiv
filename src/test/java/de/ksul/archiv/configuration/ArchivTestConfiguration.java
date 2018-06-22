package de.ksul.archiv.configuration;

import de.ksul.archiv.AlfrescoConnector;
import de.ksul.archiv.repository.CMISSessionGenerator;
import de.ksul.archiv.repository.CMISSessionGeneratorMock;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties({ArchivProperties.class})
public class ArchivTestConfiguration {

    private ArchivProperties archivProperties;

    public ArchivProperties getArchivProperties() {
        return archivProperties;
    }

    @Autowired
    public void setArchivProperties(ArchivProperties archivProperties) {
        this.archivProperties = archivProperties;
    }

    public ArchivTestConfiguration() {

    }

    @Bean
    @Autowired
    public Session getSession(ResourceLoader resourceLoader) {
        Session session;
        CMISSessionGenerator gen = new CMISSessionGeneratorMock(resourceLoader, getArchivProperties());
        session = gen.generateSession();
        return session;
    }

    @Bean
    @Autowired
    AlfrescoConnector getConnector(ResourceLoader resourceLoader) {
        return  new AlfrescoConnector(getSession(resourceLoader), null, null, null, null, getArchivProperties().getCompanyHomeName(), getArchivProperties().getDataDictionaryName(), getArchivProperties().getScriptDirectoryName());

    }

}
