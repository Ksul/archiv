package de.ksul.archiv.request;

import javax.validation.constraints.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 9/19/17
 * Time: 5:46 PM
 */
public class ContentRequest {

    @NotNull
    private String content;

    public ContentRequest() {
    }

    public ContentRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
