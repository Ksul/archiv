package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.controller.ArchivController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 27.03.17
 * Time: 13:06
 * Startet die Anwendung mit der Testkonfiguration
 */
@Configuration
@EnableAutoConfiguration
@Import({ArchivTestConfiguration.class, ArchivController.class})
public class ArchivTestApplication {


	public static void main(String[] args) {
		SpringApplication.run(ArchivTestApplication.class, args);
	}
}
