package de.schulte.archiv.response;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 24.11.16
 * Time: 13:58
 */
public class MoveResponse extends RestResponse {

    private Map<String, Object> source;

    private Map<String, Object> target;

    public Map<String, Object> getSource() {
        return source;
    }

    public void setSource(Map<String, Object> source) {
        this.source = source;
    }

    public Map<String, Object> getTarget() {
        return target;
    }

    public void setTarget(Map<String, Object> target) {
        this.target = target;
    }
}
