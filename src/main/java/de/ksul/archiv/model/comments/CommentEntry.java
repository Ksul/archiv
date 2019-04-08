package de.ksul.archiv.model.comments;

import com.fasterxml.jackson.annotation.JsonInclude;

public class CommentEntry {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Comment entry;

    public CommentEntry() {
    }

    public CommentEntry(Comment entry) {
        this.entry = entry;
    }

    public Comment getEntry() {
        return entry;
    }

    public void setEntry(Comment entry) {
        this.entry = entry;
    }
}
