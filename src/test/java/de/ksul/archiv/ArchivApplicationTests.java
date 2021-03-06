package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true"})
@SpringBootTest(classes = {ArchivTestConfiguration.class, ArchivConfiguration.class})
public class ArchivApplicationTests {
	@Test
	public void contextLoads() {
	}

}
