package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivProperties;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.repository.CMISSessionGeneratorImpl;
import org.apache.chemistry.opencmis.client.api.Session;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 08.01.14
 * Time: 17:18
 * To change this template use File | Settings | File Templates.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ArchivTestConfiguration.class, ArchivConfiguration.class})
public class CMISSessionGeneratorITest {

    @Autowired
    ArchivProperties properties;


    @Test
    public void testGenerateSession() throws Exception {
      CMISSessionGeneratorImpl gen = new CMISSessionGeneratorImpl(properties.getUser(), properties.getPassword(), properties.getBinding(), "Archiv");
      Session ses = gen.generateSession();
      assertThat(ses, Matchers.notNullValue());
      assertThat(gen.getRepositoryName(), Matchers.equalTo("Archiv"));
    }
}
