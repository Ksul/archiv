package de.ksul.archiv.model.rules;

import java.util.List;

public class RuleSet {

    List<Rule> rules;
    List<Rule> inheritedRules;
    String linkedToRuleSet;
    List<String> linkedFromRuleSets;
    String url;

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public List<Rule> getInheritedRules() {
        return inheritedRules;
    }

    public void setInheritedRules(List<Rule> inheritedRules) {
        this.inheritedRules = inheritedRules;
    }

    public String getLinkedToRuleSet() {
        return linkedToRuleSet;
    }

    public void setLinkedToRuleSet(String linkedToRuleSet) {
        this.linkedToRuleSet = linkedToRuleSet;
    }

    public List<String> getLinkedFromRuleSets() {
        return linkedFromRuleSets;
    }

    public void setLinkedFromRuleSets(List<String> linkedFromRuleSets) {
        this.linkedFromRuleSets = linkedFromRuleSets;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
