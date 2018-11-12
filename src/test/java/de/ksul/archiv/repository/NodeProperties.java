package de.ksul.archiv.repository;

import com.fasterxml.jackson.annotation.JsonCreator;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.assertj.core.util.Lists;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 07.09.18
 * Time: 15:56
 */
public class NodeProperties extends HashMap<String, Object> {

    private Type type;


    public NodeProperties(Type type) {
        this.type = type;
    }

    @JsonCreator
    public NodeProperties(Map<? extends String, ?> m) {
        super(m);
    }

    @Override
    public Object put(String key, Object value) {
        key = findNameForyKey(key);
        if (super.containsKey(key))
            return super.put(key, convertIntoJavaObject(value));
        else
            throw new CmisRuntimeException("Property " + key + " not set!");
    }

    public Object _put(String key, Object value) {
        return super.put(key, value);
    }

    private String findNameForyKey(String key) {
        if (this.type != null) {
            Optional<Property<?>> op = this.type.getProperties().stream().filter(e -> e.getLocalName().equals(key)).findFirst();
            if (op.isPresent())
                return op.get().getId();
        }
    return key;
    }

    private Object convertIntoJavaObject(Object scriptObj) {
        if (scriptObj instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) scriptObj;
            if (scriptObjectMirror.isArray()) {
                List<Object> list = Lists.newArrayList();
                for (Map.Entry<String, Object> entry : scriptObjectMirror.entrySet()) {
                    list.add(convertIntoJavaObject(entry.getValue()));
                }
                return list;
            } else {
                if (scriptObjectMirror.getClassName().equalsIgnoreCase("Date")) {
                    return new Date(scriptObjectMirror.to(Long.class));
                } else {
                    return scriptObj;
                }
            }

        }
        if (scriptObj.getClass().equals(TreeNode.class))
            return ((TreeNode) scriptObj).getId();
        return scriptObj;
    }

}
