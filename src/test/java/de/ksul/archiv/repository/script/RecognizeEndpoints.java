package de.ksul.archiv.repository.script;

import de.ksul.archiv.repository.CommentService;
import de.ksul.archiv.repository.Repository;
import de.ksul.archiv.repository.SearchService;
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
    public static TreeNode<?> document;
    public static TreeNode<?> script;
    public static TreeNode<?> companyhome;
    public static TreeNode<?> categoryhome;
    public static CommentService commentService;
    public static SearchService searchService;

    public static void setRepository(Repository repository) {
        RecognizeEndpoints.repository = repository;
        companyhome = repository.getRoot();
        categoryhome = repository.getCategoryroot();
        RecognizeEndpoints.commentService = new CommentService();
        RecognizeEndpoints.searchService = new SearchService();
    }

    public static void setScript(String path) {
        RecognizeEndpoints.script = repository.findTreeNodeForPath(path);
    }

    public static class JSLogger {
        public static void log(String message) {
            logger.info(message);
        }
    }

    public static void setDocument(TreeNode<?> document) {
        RecognizeEndpoints.document = document;
    }


}
