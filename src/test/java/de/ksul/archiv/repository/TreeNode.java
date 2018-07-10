package de.ksul.archiv.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.runtime.AbstractCmisObject;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 4/12/17
 * Time: 10:51 AM
 */


@JsonIgnoreProperties({"obj", "content"})
public class TreeNode<T> implements Iterable<TreeNode<T>> {

    private String id;
    private String name;
    private String path;
    private T obj;
    @JsonProperty("data")
    private TreeMap<String,  LinkedHashMap<String, Property<?>>> data = new TreeMap<>(Collections.reverseOrder());
    private TreeNode<T> parent;
    @JsonProperty("childs")
    Map<String, TreeNode<T>> childs;
    private TreeMap<String, ContentStream> contents = new TreeMap<>();
    private TreeMap<String, String> contentIds = new TreeMap<>();


    private boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return childs.size() == 0;
    }

    private List<TreeNode<T>> elementsIndex;

    TreeNode(String id, String name, T obj) {
        this.id = id;
        this.name = name;
        this.path ="/";
        this.obj = obj;
        this.childs = new HashMap<>();
        this.elementsIndex = new LinkedList<TreeNode<T>>();
        this.elementsIndex.add(this);
    }

    TreeNode(String id, String name,  T obj, String version) {
        this.id = id;
        this.name = name;
        this.path ="/";
        this.obj = obj;
        this.childs = new HashMap<>();
        this.elementsIndex = new LinkedList<TreeNode<T>>();
        this.elementsIndex.add(this);
        if (version != null) {
            LinkedHashMap<String, Property<?>> props = new LinkedHashMap<>();
            for (Property<?> p : ((FileableCmisObject) obj).getProperties()) {
                props.put(p.getId(), new PropertyImpl(p));
            }
            this.data.put(version, props);
        }
    }

    public T getObj() {
        return obj;
    }

    public T getObj(String version) {
        if (((FileableCmisObject) obj).getPropertyValue(PropertyIds.VERSION_LABEL).toString().equals(version))
            return obj;
        if (!data.containsKey(version))
            throw new CmisVersioningException("version not found");
        LinkedHashMap<String, Property<?>> props = new LinkedHashMap<>();
        LinkedHashMap<String, Property<?>> versionProps = data.get(version);
        Iterator<String> it = versionProps.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            props.put(key, versionProps.get(key));
        }
        try {
            Field propertiesField = AbstractCmisObject.class.getDeclaredField("properties");
            propertiesField.setAccessible(true);
            propertiesField.set(obj, props);
        } catch (Exception e) {
            throw new RuntimeException(("Properties not set!"));
        }
        return obj;
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

    void updateNode(LinkedHashMap<String, Property<?>>  properties) {
        try {
            Field propertiesField = AbstractCmisObject.class.getDeclaredField("properties");
            propertiesField.setAccessible(true);
            propertiesField.set(obj, properties);
        } catch (Exception e) {
            throw new RuntimeException(("Properties not set!"));
        }
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
        List<Property<?>> props = ((FileableCmisObject) obj).getProperties();
        LinkedHashMap<String, Property<?>> newProps = new LinkedHashMap<>();
        for (Property<?> p : props) {
            newProps.put(p.getId(), new PropertyImpl(p));
        }

        this.data.put(version,  newProps);
        return this;
    }



    TreeMap<String,  LinkedHashMap<String, Property<?>>> getAllVersions() {
        return data;
    }


    @Override
    public Iterator<TreeNode<T>> iterator() {
        return new TreeNodeIter<T>(this);
    }

    public String createContent(ContentStream content, boolean overwrite) {
        if (contentIds.containsKey(id) && !overwrite)
            throw new CmisContentAlreadyExistsException();
        String uuid = UUID.randomUUID().toString();
        contents.put(uuid, content);
        contentIds.put(this.getId(), uuid);
        return uuid;
    }

    public ContentStream getContent() {
        if ( !contents.containsKey(((FileableCmisObject) obj).getProperty(PropertyIds.CONTENT_STREAM_ID).getValueAsString()))
            return null;
        return contents.get(((FileableCmisObject) obj).getProperty(PropertyIds.CONTENT_STREAM_ID).getValueAsString());
    }



}