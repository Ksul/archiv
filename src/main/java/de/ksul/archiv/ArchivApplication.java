package de.ksul.archiv;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

@SpringBootApplication
public class ArchivApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ArchivApplication.class);
		app.setLogStartupInfo(true);
		app.setBanner(new Banner() {
			@Override
			public void printBanner (Environment environment,
									 Class<?> sourceClass,
									 PrintStream out) {
				out.println("\n" +
						"                   _     _\n" +
						"    /\\            | |   (_)\n" +
						"   /  \\   _ __ ___| |__  ___   __\n" +
						"  / /\\ \\ | '__/ __| '_ \\| \\ \\ / /\n" +
						" / ____ \\| | | (__| | | | |\\ V /\n" +
						"/_/    \\_\\_|  \\___|_| |_|_| \\_/\n" +
						"================================\n" +
						"Version:" +environment.getProperty("info.version") + ":" + environment.getProperty("info.build.date") + "\n" +
						"Spring Boot:" + SpringBootVersion.getVersion());
			}
		});
		app.run( args);
	}
}
