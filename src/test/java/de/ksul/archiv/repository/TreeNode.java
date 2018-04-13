package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.commons.data.ContentStream;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 4/12/17
 * Time: 10:51 AM
 */


public class TreeNode<T> implements Iterable<TreeNode<T>> {

    private T data;
    private ContentStream contentStream;
    private String id;
    private String name;
    private String path;
    private TreeNode<T> parent;
    Map<String, TreeNode<T>> children;

    private boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    private List<TreeNode<T>> elementsIndex;

    TreeNode(String id, String name, T data) {
        this.id = id;
        this.name = name;
        this.path ="/";
        this.data = data;
        this.children = new HashMap<>();
        this.elementsIndex = new LinkedList<TreeNode<T>>();
        this.elementsIndex.add(this);
    }

    TreeNode(String id, String name,  T data, ContentStream contentStream) {
        this.id = id;
        this.name = name;
        this.path ="/";
        this.data = data;
        this.contentStream = contentStream;
        this.children = new HashMap<>();
        this.elementsIndex = new LinkedList<TreeNode<T>>();
        this.elementsIndex.add(this);
    }

    public T getData() {
        return data;
    }

    ContentStream getContentStream() {
        return contentStream;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getId() { return id;}

    TreeNode<T> getParent() {
        return parent;
    }
    

    TreeNode<T> addNode(String id, String name, T child, ContentStream contentStream) {
        TreeNode<T> childNode = new TreeNode<T>(id, name, child, contentStream);
        childNode.parent = this;
        this.children.put(name, childNode);
        StringBuilder pfad = new StringBuilder(name);
        TreeNode<T> parentNode = this;
        while (parentNode != null) {
            pfad.insert(0, (parentNode.name.equalsIgnoreCase("/") ? "" : "/"));
            pfad.insert(0, parentNode.name);
            parentNode = parentNode.parent;
        }
        childNode.path = pfad.toString();
        this.registerChild(childNode);
        return childNode;
    }

    void removeNode() {
        if (this.parent!= null)
            this.getParent().children.remove(this.name);
        deRegisterChild(this);
    }

    TreeNode<T> moveNode(TreeNode<T> newParent) {
        this.parent = newParent;
        newParent.children.put(name, this);
        StringBuilder pfad = new StringBuilder(name);
        TreeNode<T> parentNode = newParent;
        while (parentNode != null) {
            pfad.insert(0, (parentNode.name.equalsIgnoreCase("/") ? "" : "/"));
            pfad.insert(0, parentNode.name);
            parentNode = parentNode.parent;
        }
        this.path = pfad.toString();
        deRegisterChild(this);
        newParent.registerChild(this);
        return this;
    }

    void updateNode(T child) {
        this.data = child;
    }

    int getLevel() {
        if (this.isRoot())
            return 0;
        else
            return parent.getLevel() + 1;
    }

    private void registerChild(TreeNode<T> node) {
        elementsIndex.add(node);
        if (parent != null)
            parent.registerChild(node);
    }

    private void deRegisterChild(TreeNode<T> node) {
        elementsIndex.remove(node);
        if (parent != null)
            parent.deRegisterChild(node);
    }

    TreeNode<T> findTreeNodeForId(String id) {
        if (id == null)
            throw new RuntimeException("Id must be set!");
        for (TreeNode<T> element : this.elementsIndex) {
            String objectId = element.id;
            if (id.matches(objectId))
                return element;
        }
        return null;
    }


    TreeNode<T> findTreeNodeForPath (String path) {
        if (path == null)
            throw new RuntimeException("Path must be set!");
        for (TreeNode<T> element : this.elementsIndex) {
            String objectPath = element.path;
            if (objectPath != null && path.matches(objectPath))
                return element;
        }
        return null;
    }

    boolean containsPath( String path){
        for (TreeNode<T> element : this.elementsIndex) {
            if (element.path.matches(path)){
                return true;
            }
        }
        return false;
    }

    boolean containsId( String id){
        for (TreeNode<T> element : this.elementsIndex) {
            if (element.id.matches(id)){
                return true;
            }
        }
        return false;
    }

    private  TreeNode<T> findTreeNode(Comparable<T> cmp) {
        for (TreeNode<T> element : this.elementsIndex) {
            T elData = element.data;
            if (cmp.compareTo(elData) == 0)
                return element;
        }

        return null;
    }

    public TreeNode<T> getByPath(String path) {
        String[] parts;
        TreeNode<T> parentNode = null;
        int i = 1;
        if (path.equalsIgnoreCase("/"))
            parts = new String[]{"/"};
        else {
            parts = path.split("/");

        }
        for (String part : parts) {
            if (part.isEmpty())
                part = part + "/";
            if (this.children.containsKey(part)) {
               TreeNode<T> node = this.children.get(part);
                if (parentNode != null && !node.parent.equals(parentNode))
                    return null;
                parentNode = node;
                if (i == parts.length)
                    return node;
            } else
                return null;
            i++;
        }
        return null;
    }

    @Override
    public String toString() {
        return data != null ? data.toString() : "[data null]";
    }

    @Override
    public Iterator<TreeNode<T>> iterator() {
        return new TreeNodeIter<T>(this);
    }

}