package de.schulte.archiv;

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
     * @param id            die übergebene Id
     * @return              die konvertirte Id
     */
    public static String normalizeObjectId(String id) {
        if (id.contains(";"))
            id = id.substring(0, id.lastIndexOf(';'));
        if (id.startsWith("workspace://SpacesStore/"))
            id = id.substring(24);
        return id;
    }

    /**
     * liefert die wirkliche Id. Entfernt die Version und andere nicht benötigten Informationen
     * @param id    die ObjectId
     * @return      die modifizierte ObjectId
     */
    public static String getRealId(String id) {
        if (id.contains(";"))
            id = id.substring(0, id.lastIndexOf(';'));
        if (id.startsWith("workspace://SpacesStore/"))
            id = id.substring(24);
        return id;
    }

    public static boolean isEmpty(Object o) {
        if (o == null)  return true;
        if (o instanceof Collection) return ((Collection) o).size() == 0;
        if (o instanceof String) return ((String) o).length() == 0;
        if (o instanceof Long) return ((Long) o).longValue() == 0;
        if (o instanceof Integer) return ((Integer) o).intValue() == 0;
        if (o instanceof Double) return ((Double) o).doubleValue() == 0;
        return false;
    }
}
