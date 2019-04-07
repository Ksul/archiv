package de.ksul.archiv.request;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.04.19
 * Time: 11:45
 */
public class RuleAvailableRequest {


    @NotNull
    private String folderId;
    @NotEmpty
    private String title;

    public RuleAvailableRequest(@NotNull String folderId, @NotEmpty String title) {
        this.folderId = folderId;
        this.title = title;
    }

    public RuleAvailableRequest() {
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
