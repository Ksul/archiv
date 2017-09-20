package de.ksul.archiv.request;

import javax.validation.constraints.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 9/19/17
 * Time: 5:18 PM
 */
public class UploadRequest {

    @NotNull
    private String documentId;

    @NotNull
    private String versionState;

    @NotNull
    private String fileName;

    public UploadRequest() {
    }

    public UploadRequest(String documentId, String versionState, String fileName) {
        this.documentId = documentId;
        this.versionState = versionState;
        this.fileName = fileName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getVersionState() {
        return versionState;
    }

    public void setVersionState(String versionState) {
        this.versionState = versionState;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
