package de.ksul.archiv.repository.script;

import de.ksul.archiv.repository.Repository;
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

    private static Repository repository;
    private static Logger logger = LoggerFactory.getLogger(RecognizeEndpoints.class.getName());
    public static TreeNode<FileableCmisObject> document;
    public static TreeNode<FileableCmisObject> script;
    public static TreeNode<FileableCmisObject> companyhome;

    public static void setRepository(Repository repository) {
        RecognizeEndpoints.repository = repository;
        companyhome = repository.findTreeNodeForPath("/");
    }

    public static void setScript(String path) {
        RecognizeEndpoints.script = repository.findTreeNodeForPath(path);
    }

    public static class JSLogger {
        public static void log(String message) {
            logger.info(message);
        }
    }

    public static void setDocument(TreeNode<FileableCmisObject> document) {
        RecognizeEndpoints.document = document;
    }


}
