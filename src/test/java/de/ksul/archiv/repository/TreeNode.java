package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.runtime.AbstractCmisObject;
import org.apache.chemistry.opencmis.client.runtime.DocumentImpl;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 4/12/17
 * Time: 10:51 AM
 */


public class TreeNode<T> implements Iterable<TreeNode<T>> {

    private String id;
    private String name;
    private String path;
    private TreeMap<String, T> data = new TreeMap<>(Collections.reverseOrder());
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
        this.data.put("" ,data);
        this.children = new HashMap<>();
        this.elementsIndex = new LinkedList<TreeNode<T>>();
        this.elementsIndex.add(this);
    }

    TreeNode(String id, String name,  T data, String version) {
        this.id = id;
        this.name = name;
        this.path ="/";
        this.data.put(version == null ? "" : version, data);
        this.children = new HashMap<>();
        this.elementsIndex = new LinkedList<TreeNode<T>>();
        this.elementsIndex.add(this);
    }

    public T getData() {
        return data.get(data.firstKey());
    }

    public T getData(String version) {
        return data.get(version);
    }

    public boolean containsData (String version){ return data.containsKey(version);}

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
    

    TreeNode<T> addNode(String id, String name, T child, String version) {
        TreeNode<T> childNode = new TreeNode<T>(id, name, child, version);
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
        this.data.put("", child);
    }

    void updateNode(T child, String version) {
        this.data.put(version, child);
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
        String[] parts = id.split(";");
        for (TreeNode<T> element : this.elementsIndex) {
            String objectId = element.id;
            if (parts[0].matches(objectId))
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

    TreeNode<T> makeNewVersion(String version) {
    T data = this.getData();
    if (data instanceof Folder)
        throw new RuntimeException("no version for folder");
        List<Property<?>> props = ((Document) data).getProperties();
        LinkedHashMap<String, Property<?>> newProps = new LinkedHashMap<>();
        for (Property<?> p : props) {
            newProps.put(p.getId(), new PropertyImpl(p));
        }
        try {
            Field propertiesField = AbstractCmisObject.class.getDeclaredField("properties");
            propertiesField.setAccessible(true);
            propertiesField.set(data, newProps);
        } catch (Exception e) {
            throw new RuntimeException(("Properties not set!"));
        }
        this.data.put(version, data);
        return this;
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