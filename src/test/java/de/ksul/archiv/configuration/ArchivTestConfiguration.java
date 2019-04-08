package de.ksul.archiv.configuration;

import de.ksul.archiv.AlfrescoTestConnector;
import de.ksul.archiv.repository.CMISSessionGeneratorMock;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 25.11.16
 * Time: 12:40
 */
@Configuration
@EnableConfigurationProperties({ArchivProperties.class})
@ComponentScan(basePackages = "de.ksul.archiv.repository")
public class ArchivTestConfiguration {

    ArchivProperties archivProperties;
    ResourceLoader resourceLoader;
    CMISSessionGeneratorMock cmisSessionGeneratorMock;

    public ArchivTestConfiguration() {

    }


    @Autowired
    public void setArchivProperties(ArchivProperties archivProperties) {
        this.archivProperties = archivProperties;
    }

    @Autowired
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Autowired
    public void setCmisSessionGeneratorMock(CMISSessionGeneratorMock cmisSessionGeneratorMock) {
        this.cmisSessionGeneratorMock = cmisSessionGeneratorMock;
    }

    @Bean
    public Session getSession() {
        Session session;
        cmisSessionGeneratorMock.init(resourceLoader, archivProperties);
        session = cmisSessionGeneratorMock.generateSession();
        return session;
    }


    @Bean
    @Autowired
    AlfrescoTestConnector getConnector(Session session, ArchivProperties archivProperties) {
        return  new AlfrescoTestConnector(session, null, null, null, null, archivProperties.getCompanyHomeName(), archivProperties.getDataDictionaryName(), archivProperties.getScriptDirectoryName());

    }

    @Bean
    RestTemplate getRestTemplate() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(1));
        restTemplateBuilder.setReadTimeout(Duration.ofSeconds(2));
        return restTemplateBuilder.build();
    }

}
