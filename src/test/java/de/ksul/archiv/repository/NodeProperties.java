package de.ksul.archiv.repository;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.objects.NativeDate;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            return super.put(key, convertIntoJavaObject(value));
        else
            throw new CmisRuntimeException("Property " + key + " not set!");
    }


    public Object _put(String key, Object value) {
        return super.put(key, value);
    }

    private static Object convertIntoJavaObject(Object scriptObj) {
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
                    NativeDate nativeDate = scriptObjectMirror.to(NativeDate.class);
                    return new Date(scriptObjectMirror.to(Long.class));
                } else {
                    return scriptObj;
                }
            }

        }
        return scriptObj;
    }

}
