package de.ksul.archiv.repository.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.SecondaryTypeDefinition;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.11.18
 * Time: 14:38
 */
public abstract class SecondaryTypeMixin {

    @JsonCreator
    public SecondaryTypeMixin(@JsonProperty("session") Session sesion,
                              @JsonProperty("typeDefinition") SecondaryTypeDefinition typeDefinition) {
    }
}
