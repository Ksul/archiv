package de.ksul.archiv.model.comments;

import java.util.ArrayList;
import java.util.List;

public class PaginationList {

    Pagination pagination;
    List<CommentEntry> entries;

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public List<CommentEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<CommentEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(CommentEntry entry) {
        if (entries == null)
            entries = new ArrayList<>();
        entries.add(entry);
    }
}
