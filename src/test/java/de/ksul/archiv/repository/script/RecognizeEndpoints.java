package de.ksul.archiv.repository.script;

import de.ksul.archiv.repository.TreeNode;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 25.07.18
 * Time: 12:52
 */
public class RecognizeEndpoints {

    private static Logger logger = LoggerFactory.getLogger(RecognizeEndpoints.class.getName());
    private static TreeNode<FileableCmisObject> document;
    private static JSRepository script = new JSRepository();


    public static class JSLogger{
        public static void log(String message) {
            logger.info(message);
        }
    }

    public static void setDocument(TreeNode<FileableCmisObject> document) {
        RecognizeEndpoints.document = document;
    }

    public static TreeNode<FileableCmisObject> document() {
        return document;
    }

    public static JSRepository script() {
        return script;
    }
}
