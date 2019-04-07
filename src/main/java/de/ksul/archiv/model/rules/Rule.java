package de.ksul.archiv.model.rules;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 03.04.19
 * Time: 17:20
 */
public class Rule {

    String id = UUID.randomUUID().toString();
    @NotNull
    String title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<String> ruleType;
    boolean applyToChildren;
    boolean executeAsynchronously;
    boolean disabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Action action ;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    OwningNode owningNode;
    String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRuleType() {
        return ruleType;
    }

    public void setRuleType(List<String> ruleType) {
        this.ruleType = ruleType;
    }

    public boolean isApplyToChildren() {
        return applyToChildren;
    }

    public void setApplyToChildren(boolean applyToChildren) {
        this.applyToChildren = applyToChildren;
    }

    public boolean isExecuteAsynchronously() {
        return executeAsynchronously;
    }

    public void setExecuteAsynchronously(boolean executeAsynchronously) {
        this.executeAsynchronously = executeAsynchronously;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public OwningNode getOwningNode() {
        return owningNode;
    }

    public void setOwningNode(OwningNode owningNode) {
        this.owningNode = owningNode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
