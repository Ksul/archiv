package de.ksul.archiv.request;

import javax.validation.constraints.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 9/19/17
 * Time: 1:17 PM
 */
public class ObjectByIdRequest {


    @NotNull
    private String documentId;

    public ObjectByIdRequest() {
    }

    public ObjectByIdRequest(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
