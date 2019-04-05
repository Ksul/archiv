package de.ksul.archiv.model;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 03.04.19
 * Time: 17:26
 */
public class RuleAction {

    String id;
    String actionDefinitionName;
    boolean executeAsync;
    Map<String, String> parameterValues;
    List<RuleAction> actions;
    String url;
    List<RuleCondition> conditions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActionDefinitionName() {
        return actionDefinitionName;
    }

    public void setActionDefinitionName(String actionDefinitionName) {
        this.actionDefinitionName = actionDefinitionName;
    }

    public boolean isExecuteAsync() {
        return executeAsync;
    }

    public void setExecuteAsync(boolean executeAsync) {
        this.executeAsync = executeAsync;
    }

    public Map<String, String> getParameterValues() {
        return parameterValues;
    }

    public void setParameterValues(Map<String, String> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public List<RuleAction> getActions() {
        return actions;
    }

    public void setActions(List<RuleAction> actions) {
        this.actions = actions;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<RuleCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<RuleCondition> conditions) {
        this.conditions = conditions;
    }
}
