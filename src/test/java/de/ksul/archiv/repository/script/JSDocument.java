package de.ksul.archiv.repository.script;

import de.ksul.archiv.repository.TreeNode;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 25.07.18
 * Time: 14:59
 */
public class JSDocument {

    private TreeNode<FileableCmisObject> document;

    public void  setDocument(TreeNode<FileableCmisObject> document) {
        this.document = document;
    }
}
