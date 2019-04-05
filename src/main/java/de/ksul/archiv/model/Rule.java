package de.ksul.archiv.model;

import de.ksul.archiv.model.RuleAction;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 03.04.19
 * Time: 17:20
 */
public class Rule {

    String id;
    @NotNull
    String title;
    String description;
    List<String> ruleType;
    boolean applyToChildren;
    boolean executeAsynchronously;
    boolean disabled;
    List<RuleAction> action;

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

    public List<RuleAction> getAction() {
        return action;
    }

    public void setAction(List<RuleAction> action) {
        this.action = action;
    }
}
