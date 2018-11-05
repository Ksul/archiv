package de.ksul.archiv.repository.script;

import de.ksul.archiv.repository.TreeNode;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 05.10.18
 * Time: 14:46
 */
public interface AlfrescoScriptApi<T> {
    TreeNode<T> childByNamePath(String name);

    TreeNode<T> transformDocument(String mimeType);

    TreeNode<T> createFolder(String name);

    TreeNode<T> createFolder(String name, String type);

    TreeNode<T> createNode(String name, String type, String aspect);

    TreeNode<T> createNode(String name, String type);

    TreeNode<T> createFile(String name);

    TreeNode<T> createFile(String name, String type);

    boolean move(TreeNode<T> destination);
    
    boolean isSubType(String name);

    void specializeType(String key);

    void addAspect(String key);

    boolean hasAspect(String key);

    void addTag(String name);

    void addNode(TreeNode<T> node);

    void createAssociation(Object target, String name);

    List<TreeNode<T>> getRootCategories(String name);

    TreeNode<T> createRootCategory(String aspect, String name);

    TreeNode<T> createSubCategory(String name);

    void save();
}
