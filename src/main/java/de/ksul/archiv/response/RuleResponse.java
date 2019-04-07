package de.ksul.archiv.response;

import de.ksul.archiv.model.rules.Rule;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 05.04.19
 * Time: 16:12
 */
public class RuleResponse {

    private List<Rule> data;

    public List<Rule> getData() {
        return data;
    }

    public void setData(List<Rule> data) {
        this.data = data;
    }

    public boolean hasRule(String name) {
        boolean ret = false;
        if (!data.isEmpty()) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getTitle().equalsIgnoreCase(name)) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }
}
