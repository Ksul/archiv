package de.ksul.archiv.request;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 9/19/17
 * Time: 5:23 PM
 */
public class DocumentRequest {


    @NotNull
    private String documentId;

    @NotNull
    private String fileName;

    @NotNull
    private String content;

    @NotNull
    private String mimeType;

    @NotNull
    private String versionState;

    private String versionComment;

    private Map<String, Object> extraProperties;

    public DocumentRequest() {
    }

    public DocumentRequest(String documentId, String fileName, String content, String mimeType, String versionState, String versionComment, Map<String, Object> extraProperties) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.content = content;
        this.mimeType = mimeType;
        this.versionState = versionState;
        this.versionComment = versionComment;
        this.extraProperties = extraProperties;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getVersionState() {
        return versionState;
    }

    public void setVersionState(String versionState) {
        this.versionState = versionState;
    }

    public String getVersionComment() {
        return versionComment;
    }

    public void setVersionComment(String versionComment) {
        this.versionComment = versionComment;
    }

    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties = extraProperties;
    }
}
