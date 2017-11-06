package de.ksul.archiv.request;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 11/6/17
 * Time: 3:14 PM
 */
public class DocumentCreateRequest extends DocumentRequest {

    @NotNull
    private String fileName;


    public DocumentCreateRequest() {
    }

    public DocumentCreateRequest(String documentId, String fileName, String content, String mimeType, String versionState, String versionComment, Map<String, Object> extraProperties) {
        super(documentId, content, mimeType, versionState, versionComment, extraProperties);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
