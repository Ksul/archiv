package de.ksul.archiv.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = ArchivProperties.PREFIX)
public class ArchivProperties {

    public static final String PREFIX = "ksul.archiv";

    /**
     * der User für den Alfresco Server
     */
    private String user;

    /**
     * das Password für den Alfresco Server
     */
    private String password;

    /**
     * die URL für das Binding
     */
    private String binding;

    /**
     * die Url für den Server
     */
    private String server;

    /**
     * der Name des CompanyHome Verzeichnisses
     */
    private String companyHomeName = "Company Home";

    /**
     * der Name der Data Dictionary Verzeichnisses
     */
    private String dataDictionaryName = "Data Dictionary";

    /**
     * der Name für das Verzeichnis der Skripte
     */
    private String scriptDirectoryName = "Scripts";

    /**
     * Einstellungen zum Testen
     */
    private Testing testing = new Testing();

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getCompanyHomeName() {
        return companyHomeName;
    }

    public void setCompanyHomeName(String companyHomeName) {
        this.companyHomeName = companyHomeName;
    }

    public String getDataDictionaryName() {
        return dataDictionaryName;
    }

    public void setDataDictionaryName(String dataDictionaryName) {
        this.dataDictionaryName = dataDictionaryName;
    }

    public String getScriptDirectoryName() {
        return scriptDirectoryName;
    }

    public void setScriptDirectoryName(String scriptDirectoryName) {
        this.scriptDirectoryName = scriptDirectoryName;
    }

    public Testing getTesting() {
        return testing;
    }

    public void setTesting(Testing testing) {
        this.testing = testing;
    }

    public class Testing {

        /**
         * Pfad zur Test Pdf Datei
         */
        private String testpdf;

        /**
         * Pfad zum Test Zip File
         */
        private String testzip;

        /**
         * Pfad zum Test Text File
         */
        private String testtxt;

        /**
         * Pfad zum Sichern der Testumgebung
         */
        private String testData;

        /**
         * Alfresco Models die genutzt werden sollen
         */
        private String[] models;

        public String getTestpdf() {
            return testpdf;
        }

        public void setTestpdf(String testpdf) {
            this.testpdf = testpdf;
        }

        public String getTestzip() {
            return testzip;
        }

        public void setTestzip(String testzip) {
            this.testzip = testzip;
        }

        public String getTesttxt() {
            return testtxt;
        }

        public void setTesttxt(String testtxt) {
            this.testtxt = testtxt;
        }

        public String getTestData() {
            return testData;
        }

        public void setTestData(String testData) {
            this.testData = testData;
        }

        public String[] getModels() {
            return models;
        }

        public void setModels(String[] models) {
            this.models = models;
        }
    }
}
