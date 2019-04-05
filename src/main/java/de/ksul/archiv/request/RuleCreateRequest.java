package de.ksul.archiv.request;

import de.ksul.archiv.model.Rule;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.04.19
 * Time: 18:24
 */
public class RuleCreateRequest {

    Rule data;

    public Rule getData() {
        return data;
    }

    public void setData(Rule data) {
        this.data = data;
    }

}
