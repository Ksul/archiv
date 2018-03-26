package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.controller.ArchivController;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

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
		SpringApplication app = new SpringApplication(ArchivTestApplication.class);
		app.setLogStartupInfo(true);
		app.setBanner(new Banner() {
			@Override
			public void printBanner (Environment environment,
									 Class<?> sourceClass,
									 PrintStream out) {
				out.println("                                                        \n" +
						"                   _     _         _______        _        \n" +
						"    /\\            | |   (_)       |__   __|      | |       \n" +
						"   /  \\   _ __ ___| |__  ___   __    | | ___  ___| |_      \n" +
						"  / /\\ \\ | '__/ __| '_ \\| \\ \\ / /    | |/ _ \\/ __| __|\n" +
						" / ____ \\| | | (__| | | | |\\ V /     | |  __/\\__ \\ |_   \n" +
						"/_/    \\_\\_|  \\___|_| |_|_| \\_/      |_|\\___||___/\\__|\n" +
						"============================================================\n" +
						"Version:" +environment.getProperty("info.version") + ":" + environment.getProperty("info.build.date") + "\n" +
						"Spring Boot:" + SpringBootVersion.getVersion());
			}
		});
		app.run( args);
	}
}
