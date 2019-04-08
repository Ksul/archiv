package de.ksul.archiv.model.comments;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 08.04.19
 * Time: 10:14
 */
public class Comment {

    List<String> content;

    public Comment(String content) {
        this.content = Arrays.asList(content);
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }
}
