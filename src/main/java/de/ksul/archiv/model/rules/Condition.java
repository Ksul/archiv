package de.ksul.archiv.model.rules;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 03.04.19
 * Time: 17:34
 */
public class Condition {

    String id = UUID.randomUUID().toString();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String description;
    String title;
    String conditionDefinitionName;
    boolean invertCondition;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Map<String, String> parameterValues;
    String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Map<String, String> getParameterValues() {
        return parameterValues;
    }

    public void setParameterValues(Map<String, String> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }



}
