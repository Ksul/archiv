package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivProperties;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.repository.CMISSessionGeneratorImpl;
import org.apache.chemistry.opencmis.client.api.Session;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 08.01.14
 * Time: 17:18
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ArchivTestConfiguration.class, ArchivConfiguration.class})
public class CMISSessionGeneratorITest {

    @Autowired
    ArchivProperties properties;


    @Test
    public void testGenerateSession() throws Exception {
      CMISSessionGeneratorImpl gen = new CMISSessionGeneratorImpl(properties.getUser(), properties.getPassword(), properties.getBinding(), "Archiv");
      Session ses = gen.generateSession();
      Assert.assertThat(ses, Matchers.notNullValue());
      Assert.assertThat(gen.getRepositoryName(), Matchers.equalTo("Archiv"));
    }
}
