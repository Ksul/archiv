package de.schulte.archiv;

import de.schulte.archiv.configuration.ArchivConfiguration;
import de.schulte.archiv.configuration.ArchivTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ArchivTestConfiguration.class, ArchivConfiguration.class})
public class ArchivApplicationTests {

	@Test
	public void contextLoads() {
	}

}