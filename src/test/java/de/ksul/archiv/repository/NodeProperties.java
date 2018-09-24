package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

import java.util.Date;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 07.09.18
 * Time: 15:56
 */
public class NodeProperties extends HashMap<String, Object> {

    @Override
    public Object put(String key, Object value) {
        if (super.containsKey(key))
            return super.put(key, value);
        else
            throw new CmisRuntimeException("Property " + key + " not set!");
    }

    public Object _put(String key, Object value) {
        return super.put(key, value);
    }

}
