package de.schulte.archiv.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 24.11.16
 * Time: 15:00
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = ArchivTestProperties.PREFIX)
public class ArchivTestProperties {

    public static final String PREFIX = "schulte.archiv.test";

    private String testPDF;

    private String testZIP;

    private String testTXT;

    public String getTestPDF() {
        return testPDF;
    }

    public void setTestPDF(String testPDF) {
        this.testPDF = testPDF;
    }

    public String getTestZIP() {
        return testZIP;
    }

    public void setTestZIP(String testZIP) {
        this.testZIP = testZIP;
    }

    public String getTestTXT() {
        return testTXT;
    }

    public void setTestTXT(String testTXT) {
        this.testTXT = testTXT;
    }
}
