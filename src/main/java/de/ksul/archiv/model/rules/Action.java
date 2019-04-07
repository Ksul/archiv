package de.ksul.archiv.model.rules;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 03.04.19
 * Time: 17:26
 */
public class Action {

    String id = UUID.randomUUID().toString();
    String actionDefinitionName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String description;
    String title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Map<String, String> parameterValues;
    boolean executeAsync;
    String runAsUser;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<Action> actions;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<Condition> conditions;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Action compensatingAction;
    String url;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isExecuteAsync() {
        return executeAsync;
    }

    public void setExecuteAsync(boolean executeAsync) {
        this.executeAsync = executeAsync;
    }

    public String getRunAsUser() {
        return runAsUser;
    }

    public void setRunAsUser(String runAsUser) {
        this.runAsUser = runAsUser;
    }

    public Map<String, String> getParameterValues() {
        return parameterValues;
    }

    public void setParameterValues(Map<String, String> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public Action getCompensatingAction() {
        return compensatingAction;
    }

    public void setCompensatingAction(Action compensatingAction) {
        this.compensatingAction = compensatingAction;
    }
}
