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
    private String companyHomeName;

    /**
     * der Name der Data Dictionary Verzeichnisses
     */
    private String dataDictionaryName;

    /**
     * der Name für das Verzeichnis der Skripte
     */
    private String scriptDirectoryName;

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
}
