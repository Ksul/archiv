package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 25.09.18
 * Time: 12:37
 */
public class CommentService {

    public TreeNode<?> createCommentsFolder(TreeNode<?> node) {
        if (!node.isSubType("cmis:document"))
            throw new CmisRuntimeException("Discusions only for documents");
        MockUtils mockUtils = MockUtils.getInstance();
        Repository repository = Repository.getInstance();
        
        return repository.insert((TreeNode<FileableCmisObject>) node, mockUtils.createFileableCmisObject(repository, null, node.getPath(), node.getName() + " Diskussion", mockUtils.getFolderType("fm:forum"), null), false);
    }
}
