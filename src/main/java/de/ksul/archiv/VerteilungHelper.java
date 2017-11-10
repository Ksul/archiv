package de.ksul.archiv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 21.02.14
 * Time: 14:17
 * To change this template use File | Settings | File Templates.
 */
public class VerteilungHelper {

    private static Logger logger = LoggerFactory.getLogger(VerteilungHelper.class.getName());

    /**
     * konvertiert die ObjectId in ein verwertbares Format
     * das bedeudet, dass die Versionsinformation und die Store Information abgeschnitten wird
     * damit sie zum Beispiel im DOM Tree als Id benutzt werden kann.
     * @param id    die ObjectId
     * @return      die modifizierte ObjectId
     */
    public static String normalizeObjectId(String id) {
        // Versionsinformationen entfernen
        if (id.contains(";"))
            id = id.substring(0, id.lastIndexOf(';'));
        // 
        if (id.startsWith("workspace://SpacesStore/"))
            id = id.substring(24);
        return id;
    }

    /**
     * prüft, ob ein Object leer ist
     * @param o das zu prüfende Object
     * @return true or false
     */
    public static boolean isEmpty(Object o) {
        if (o == null)  return true;
        if (o instanceof Collection) return ((Collection) o).size() == 0;
        if (o instanceof String) return ((String) o).length() == 0;
        if (o instanceof Long) return (Long) o == 0;
        if (o instanceof Integer) return (Integer) o == 0;
        if (o instanceof Double) return (Double) o == 0;
        return false;
    }
}
