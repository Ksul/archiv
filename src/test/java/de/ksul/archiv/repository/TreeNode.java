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
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 4/12/17
 * Time: 10:51 AM
 */

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class TreeNode<T> implements Iterable<TreeNode<T>>, Comparable {

    private String id;
    private String name;
    private String path;
    private Type obj;
    private TreeMap<String, Type> versions = new TreeMap<>(Collections.reverseOrder());
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
        this.obj = new Type(obj.getProperties());
        this.childs = new HashMap<>();
     }

    TreeNode(String id, String name,  FileableCmisObject obj, String version) {
        this.id = id;
        this.name = name;
        this.path ="/";
        this.obj = new Type(obj.getProperties());
        this.childs = new HashMap<>();
        if (version != null) {
            this.versions.put(version, this.obj);
        }
    }

    private boolean isRoot() {
        return parent == null;
    }


    public T getObj(){return (T) MockUtils.getInstance().createObject(obj);
    }

    public T getObj(String version) {
        if (obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.VERSION_LABEL)).findFirst().get().getValueAsString().equals(version))
            return (T) MockUtils.getInstance().createObject(obj);
        if (!versions.containsKey(version))
            throw new CmisVersioningException("version not found");
        LinkedHashMap<String, Property<?>> props = new LinkedHashMap<>();
        return (T) MockUtils.getInstance().createObject(versions.get(version));
    }
    

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
        obj.setProperties(properties);
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


    TreeMap<String,  Type> getAllVersions() {
        return versions;
    }

    TreeNode<T> checkout() {
        if (obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.IS_PRIVATE_WORKING_COPY)).findFirst().get().getValue())
            return null;
        ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.IS_PRIVATE_WORKING_COPY)).findFirst().get()).setValue(true);
        return this;
    }

    TreeNode checkin(String version, String checkinComment) {

        if (checkinComment != null && !checkinComment.isEmpty())
            changePropertie(PropertyIds.CHECKIN_COMMENT, checkinComment);
        changePropertie(PropertyIds.LAST_MODIFICATION_DATE, MockUtils.getInstance().copyDateTimeValue(new Date().getTime()));
        changePropertie(PropertyIds.VERSION_LABEL, version);
        changePropertie(PropertyIds.IS_PRIVATE_WORKING_COPY, false);
        String versionSeriesId = obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.VERSION_SERIES_ID)).findFirst().get().getValue();

        ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_ID)).findFirst().get()).setValue(versionSeriesId + ";" + version);
        try {
            this.versions.put(version, obj.clone());
        } catch (CloneNotSupportedException ignored) {
        }
        return this;
    }

    private void changePropertie(String id, Object value){
        Map<String, PropertyData<?>> props = MockUtils.getInstance().convProperties(obj.getProperties());
        props.put(id, MockUtils.getInstance().fillProperty(id, value));
        obj.setProperties(MockUtils.getInstance().convPropertyData(props));
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