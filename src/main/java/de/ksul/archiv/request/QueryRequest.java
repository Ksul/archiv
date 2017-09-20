package de.ksul.archiv.request;

import javax.validation.constraints.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 9/19/17
 * Time: 5:52 PM
 */
public class QueryRequest {

    @NotNull
    private String cmisQuery;

    public QueryRequest() {
    }

    public QueryRequest(String cmisQuery) {
        this.cmisQuery = cmisQuery;
    }

    public String getCmisQuery() {
        return cmisQuery;
    }

    public void setCmisQuery(String cmisQuery) {
        this.cmisQuery = cmisQuery;
    }
}
