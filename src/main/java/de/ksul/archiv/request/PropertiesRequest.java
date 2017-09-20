package de.ksul.archiv.request;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 9/19/17
 * Time: 5:33 PM
 */
public class PropertiesRequest {


    @NotNull
    private String documentId;

    @NotNull
    private Map<String, Object> extraProperties;

    public PropertiesRequest() {
    }

    public PropertiesRequest(String documentId, Map<String, Object> extraProperties) {
        this.documentId = documentId;
        this.extraProperties = extraProperties;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties = extraProperties;
    }
}
