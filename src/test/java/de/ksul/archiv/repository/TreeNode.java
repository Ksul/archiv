package de.ksul.archiv.repository;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.runtime.AbstractCmisObject;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 4/12/17
 * Time: 10:51 AM
 */


@JsonIgnoreProperties({"obj"})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class TreeNode<T> implements Iterable<TreeNode<T>>, Comparable {

    private String id;
    private String name;
    private String path;
    private List<Property<?>> obj;
    private TreeMap<String,  List<Property<?>>> versions = new TreeMap<>(Collections.reverseOrder());
    private TreeNode<T> parent;
    @JsonProperty("childs")
    Map<String, TreeNode<T>> childs;

    public boolean isLeaf() {
        return childs.size() == 0;
    }

    public TreeNode() {
    }

    TreeNode(String id, String name, FileableCmisObject obj) {
        this.id = id;
        this.name = name;
        this.path ="/";
        this.obj = obj.getProperties();
        this.childs = new HashMap<>();
     }

    TreeNode(String id, String name,  FileableCmisObject obj, String version) {
        this.id = id;
        this.name = name;
        this.path ="/";
        this.obj = obj.getProperties();
        this.childs = new HashMap<>();
        if (version != null) {
            this.versions.put(version, obj.getProperties());
        }
    }





    private boolean isRoot() {
        return parent == null;
    }


    public T getObj(){return (T) MockUtils.getInstance().createObject(obj);
    }

    public T getObj(String version) {
        if (obj.stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.VERSION_LABEL)).findFirst().get().getValueAsString().equals(version))
            return (T) MockUtils.getInstance().createObject(obj);
        if (!versions.containsKey(version))
            throw new CmisVersioningException("version not found");
        LinkedHashMap<String, Property<?>> props = new LinkedHashMap<>();
       List<Property<?>> versionProps = versions.get(version);
       return (T) MockUtils.getInstance().createObject(versionProps);
    }

    /*private T createObj(List<Property<?>> properties) {
        ObjectDataImpl objectData = new ObjectDataImpl();
        PropertiesImpl props = (PropertiesImpl) MockUtils.getInstance().convProperties(properties);
        objectData.setProperties(props);
        props.getProperties().get(PropertyIds.OBJECT_TYPE_ID).
        T obj = new FolderImpl()
        LinkedHashMap<String, Property<?>> props = new LinkedHashMap<>();
        Iterator<String> it = properties.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            props.put(key, properties.get(key));}
        }
        try {
            Field propertiesField = AbstractCmisObject.class.getDeclaredField("properties");
            propertiesField.setAccessible(true);
            propertiesField.set(obj, props);
        } catch (Exception e) {
            throw new RuntimeException(("Properties not set!"));
        }
    }*/

    public boolean containsData (String version){ return versions.containsKey(version);}

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
    

    TreeNode<T> addNode(String id, String name, FileableCmisObject child, String version) {
        TreeNode<T> childNode = new TreeNode<T>(id, name, child, version);
        childNode.parent = this;
        this.childs.put(name, childNode);
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
            this.getParent().childs.remove(this.name);
        deRegisterChild(this);
    }

    TreeNode<T> moveNode(TreeNode<T> newParent) {
        this.parent = newParent;
        newParent.childs.put(name, this);
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

    void updateNode(List<Property<?>>  properties) {
        obj = properties;
    }

    int getLevel() {
        if (this.isRoot())
            return 0;
        else
            return parent.getLevel() + 1;
    }

    private void registerChild(TreeNode<T> node) {

        if (parent != null)
            parent.registerChild(node);
    }

    private void deRegisterChild(TreeNode<T> node) {

        if (parent != null)
            parent.deRegisterChild(node);
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
            if (this.childs.containsKey(part)) {
               TreeNode<T> node = this.childs.get(part);
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
         this.versions.put(version,  ((FileableCmisObject) obj).getProperties());
        return this;
    }

    TreeMap<String,  List<Property<?>>> getAllVersions() {
        return versions;
    }

    TreeNode<T> checkout() {
        if (obj.stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.IS_PRIVATE_WORKING_COPY)).findFirst().get().getValue())
            return null;
        ((PropertyImpl) obj.stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.IS_PRIVATE_WORKING_COPY)).findFirst().get()).setValue(true);
        return this;
    }

    @Override
    public Iterator<TreeNode<T>> iterator() {
        return new TreeNodeIter<T>(this);
    }

    @Override
    public int compareTo(Object o) {
        return this.getId().compareTo(((TreeNode<T>) o).getId());
    }
}