package de.ksul.archiv.repository;

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
        System.out.println("Added " + key + " with " + value);
        return super.put(key, value);
    }
}
