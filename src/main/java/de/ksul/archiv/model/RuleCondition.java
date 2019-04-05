package de.ksul.archiv.model;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 03.04.19
 * Time: 17:34
 */
public class RuleCondition {

    String id;
    String conditionDefinitionName;
    boolean invertCondition;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConditionDefinitionName() {
        return conditionDefinitionName;
    }

    public void setConditionDefinitionName(String conditionDefinitionName) {
        this.conditionDefinitionName = conditionDefinitionName;
    }

    public boolean isInvertCondition() {
        return invertCondition;
    }

    public void setInvertCondition(boolean invertCondition) {
        this.invertCondition = invertCondition;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    String url;


}
