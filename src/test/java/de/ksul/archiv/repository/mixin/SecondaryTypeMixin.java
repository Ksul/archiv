package de.ksul.archiv.repository.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.objecttype.SecondaryTypeImpl;
import org.apache.chemistry.opencmis.commons.definitions.SecondaryTypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.SecondaryTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.11.18
 * Time: 14:38
 */
public class SecondaryTypeMixin {

    @JsonDeserialize(as = SecondaryType.class)
    @JsonCreator
    public static SecondaryType create(@JsonProperty("session") Session sesion,
                                @JsonProperty("id") String id,
                                @JsonProperty("baseId") String baseId,
                                @JsonProperty("propertyDefinitions")Map<String, PropertyDefinition<?>> propertyDefinitions) {
        SecondaryTypeDefinitionImpl secondaryTypeDefinition = new SecondaryTypeDefinitionImpl();
        secondaryTypeDefinition.setPropertyDefinitions(propertyDefinitions);
        SecondaryTypeImpl secondaryType = new SecondaryTypeImpl(sesion, secondaryTypeDefinition);
        secondaryType.setId(id);
        secondaryType.setBaseTypeId(BaseTypeId.fromValue(baseId));
        return new SecondaryTypeImpl(sesion, secondaryType);
    }
}
