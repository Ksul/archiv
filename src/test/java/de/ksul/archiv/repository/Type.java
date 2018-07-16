package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.client.runtime.objecttype.SecondaryTypeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 16.07.18
 * Time: 15:24
 */
public class Type implements Cloneable{

    private List<Property<?>> properties;


    private Type() {
    }

    public Type(List<Property<?>> properties) {
        this.properties = properties;
    }

    public List<Property<?>> getProperties() {
        return properties;
    }

    public void setProperties(List<Property<?>> properties) {
        this.properties = properties;
    }



    @Override
    protected Type clone() throws CloneNotSupportedException {

        Type type = new Type();
        List<Property<?>> newProps = new ArrayList<>();
        for (Property<?> p : getProperties()) {
            newProps.add(new PropertyImpl(p));
        }
        type.setProperties(newProps);
        return type;
    }
}
