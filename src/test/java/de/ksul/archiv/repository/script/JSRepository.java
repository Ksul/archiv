package de.ksul.archiv.repository.script;

import de.ksul.archiv.repository.Repository;
import de.ksul.archiv.repository.TreeNode;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 25.07.18
 * Time: 15:00
 */
public class JSRepository {

    private static Repository repository;


    public static void setRepository(Repository repository) {
        JSRepository.repository = repository;
    }

    public TreeNode<FileableCmisObject> childByNamePath(String path) {
      return repository.findTreeNodeForPath(path);
    }

}
