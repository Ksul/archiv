package de.ksul.archiv.repository;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.ksul.archiv.VerteilungConstants;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.ItemType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.joda.time.LocalTime;

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
    public String name;
    private String path;
    public String displayPath = "";
    private Type obj;
    private TreeMap<String, Type> versions = new TreeMap<>(Collections.reverseOrder());
    public TreeNode<T> parent;
    public String content;
    @JsonProperty("childs")
    Map<String, TreeNode<T>> childs;
    public NodeProperties properties = new NodeProperties();
    public NodeProperties childAssocs  = new NodeProperties();
    public boolean isLeaf() {
        return childs.size() == 0;
    }

    public TreeNode() {
    }

    TreeNode( FileableCmisObject obj) {
        if (! obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_ID)).findFirst().isPresent())
            throw new CmisRuntimeException("objectId not found");
        this.id = ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_ID)).findFirst().get()).getValueAsString();
        if (! obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.NAME)).findFirst().isPresent())
            throw new CmisRuntimeException("object name not found");
        this.name = ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.NAME)).findFirst().get()).getValueAsString();
        this.path ="";
        this.obj = new Type(obj.getProperties(), obj.getType(), obj.getSecondaryTypes());
        this.childs = new HashMap<>();
        copyValues();
     }

    TreeNode( FileableCmisObject obj, String version) {
        if (! obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_ID)).findFirst().isPresent())
            throw new CmisRuntimeException("objectId not found");
        this.id = ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_ID)).findFirst().get()).getValueAsString();
        if (! obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.NAME)).findFirst().isPresent())
            throw new CmisRuntimeException("object name not found");
        this.name = ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.NAME)).findFirst().get()).getValueAsString();
        this.path ="";
        this.obj = new Type(obj.getProperties(), obj.getType(), obj.getSecondaryTypes());
        this.childs = new HashMap<>();
        if (version != null) {
            changePropertie(PropertyIds.LAST_MODIFICATION_DATE, MockUtils.getInstance().copyDateTimeValue(new Date().getTime()));
            changePropertie(PropertyIds.VERSION_LABEL, version);
            changePropertie(PropertyIds.IS_PRIVATE_WORKING_COPY, false);
            String versionSeriesId = obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.VERSION_SERIES_ID)).findFirst().get().getValue();
            ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_ID)).findFirst().get()).setValue(versionSeriesId + ";" + version);
            this.versions.put(version, this.obj);
        }
        copyValues();
    }

    private boolean isRoot() {
        return parent == null;
    }

    public Type getType() {
        return obj;
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

    protected void setPath(String path) {
        this.path = path;
    }

    public String getId() { return id;}

    TreeNode<T> getParent() {
        return parent;
    }
    

    TreeNode<T> addNode(FileableCmisObject child, String version) {
        TreeNode<T> childNode = new TreeNode<T>( child, version);
        childNode.parent = this;
        String name = child.getName();
        this.childs.put(name, childNode);
        childNode.path = this.path + (this.path.equals("/") ? "" : "/") + name;
        childNode.displayPath = this.displayPath + "/" + this.getName();
//        if (version != null) {
//             childNode.versions.put(version, childNode.obj);
//           // childNode.checkin(version, null);
//        }
        this.registerChild(childNode);
        return childNode;
    }

    void removeNode() {
        if (this.parent!= null)
            this.getParent().childs.remove(this.getName());
        deRegisterChild(this);
    }

    public boolean move(TreeNode<T> destination) {
        try {
            moveNode(destination);
            if (destination.getName().equalsIgnoreCase("Inbox")) {
                Repository.getInstance().executeScript(this);
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    TreeNode<T> moveNode(TreeNode<T> newParent) {
        this.parent = newParent;
        newParent.childs.put(getName(), this);
        this.path = newParent.path + (newParent.path.equals("/") ? "" : "/") + name;
        this.displayPath = newParent.displayPath + "/" + newParent.getName();
        deRegisterChild(this);
        newParent.registerChild(this);
        updateProperty(PropertyIds.PARENT_ID, newParent.getId());
        return this;
    }

    void updateNode(List<Property<?>>  properties) {
        obj.setProperties(properties);
        copyValues();
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

    public TreeNode<T> childByNamePath(String name) {
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        if (name.startsWith("/")) {
            name = name.substring(1, name.length());
        }
        if (obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_TYPE_ID)).findFirst().get().getValue().equals(BaseTypeId.CMIS_DOCUMENT.value()))
            throw new CmisNotSupportedException("not a folder");

        String[] parts = name.split("/");
        TreeNode<T> n = this;
        for (String part : parts) {
            Iterator<String> it = n.childs.keySet().iterator();
            boolean found = false;
            while (it.hasNext()) {
                String id = it.next();
                if (n.childs.get(id).getName().equals(part)) {
                    found = true;
                    n = n.childs.get(id);
                    break;
                }
            }
            if ( !found)
                return null;
        }
        return n;
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
        ((PropertyImpl) props.get(id)).setValue(value);
        obj.setProperties(MockUtils.getInstance().convPropertyData(props));
    }

    public TreeNode<T> transformDocument(String mimeType) {
        if (mimeType.equals("text/plain")) {
            String newName = getName().substring(0, getName().lastIndexOf(".") + 1) + "txt";
            String newPath = path.substring(0, path.lastIndexOf("/")) ;
            FileableCmisObject newObject = MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), MockUtils.getInstance().convProperties(obj.getProperties()), newPath, newName, this.obj.getObjectType(), "text/plain");
            TreeNode<T> transform = (TreeNode<T>) Repository.getInstance().insert((TreeNode<FileableCmisObject>) this.parent, newObject, false);
//            if (obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.CONTENT_STREAM_MIME_TYPE)).findFirst().get().getValue().equals(VerteilungConstants.DOCUMENT_TYPE_PDF)){
//                ((PropertyImpl) ((FileableCmisObject) transform.getObj()).getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.CONTENT_STREAM_MIME_TYPE)).findFirst().get()).setValue(VerteilungConstants.DOCUMENT_TYPE_PDF);
//                Repository.getInstance().createContent(transform.getId(), , true);
//            }
            transform.setContent(this.content);
            return transform;
        }
        return null;
    }

    public boolean remove(){
        try {
            Repository.getInstance().delete(getId());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public TreeNode<T> createFolder(String name) {
        FileableCmisObject newFolder = MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, this.getPath(), name, MockUtils.getInstance().getFolderType(), null);
        return ( TreeNode<T>) Repository.getInstance().insert((TreeNode<FileableCmisObject>) this, newFolder, true);
        
    }

    public boolean isSubType(String name) {
        String typeName = obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_TYPE_ID)).findFirst().get().getValue();
        return MockUtils.getInstance().getPropertyDefinitionBuilder().isSubtypeOf(typeName, name);
    }

    public void specializeType(String key) {
        if (obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_TYPE_ID)).findFirst().get().getValue().equals(BaseTypeId.CMIS_DOCUMENT.value())) {
            if (!key.startsWith("D:"))
                key = "D:" + key;
        }
        if (obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_TYPE_ID)).findFirst().get().getValue().equals(BaseTypeId.CMIS_FOLDER.value())) {
            if (!key.startsWith("F:"))
                key = "F:" + key;
        }
        Map<String, PropertyDefinition<?>> definitionMap = MockUtils.getInstance().getPropertyDefinitionBuilder().getPropertyDefinitionMap(key);
        obj.getObjectType().getPropertyDefinitions().putAll(definitionMap);
        for (PropertyDefinition propertyDefinition: definitionMap.values()) {
            if (  ! obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(propertyDefinition.getId())).findFirst().isPresent()) {
                obj.getProperties().add(new PropertyImpl<>(propertyDefinition, new ArrayList<>()));
                properties._put(propertyDefinition.getId(), null);
            }
        }
    }

    public void addAspect(String key) {
        if (!key.startsWith("P:"))
            key = "P:" + key;
        ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)).findFirst().get()).getValues().add(key);
        Map<String, PropertyDefinition<?>> definitionMap = MockUtils.getInstance().getPropertyDefinitionBuilder().getPropertyDefinitionMap(key);
        obj.getObjectType().getPropertyDefinitions().putAll(definitionMap);
        for (PropertyDefinition propertyDefinition: definitionMap.values()) {
            if (  ! obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(propertyDefinition.getId())).findFirst().isPresent()) {
                obj.getProperties().add(new PropertyImpl<>(propertyDefinition, new ArrayList<>()));
                properties._put(propertyDefinition.getId(), null);
            }
        }

    }

    public boolean hasAspect(String key) {
        return ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)).findFirst().get()).getValues().contains(key);
    }

    public void addTag(String name) {
        //TODO Implementierung
    }

    public void createAssociation( Object target, String name){
        childAssocs.put(name, target);
    }

    public List<TreeNode<T>> getRootCategories(String name) {
        if (!(((FileableCmisObject) getObj()).getBaseType() instanceof ItemType))
            throw new CmisRuntimeException("no category node");
        return null;
    }

    public TreeNode<T> createRootCategory(String aspect, String name) {
        if (!(((FileableCmisObject) getObj()).getBaseType() instanceof ItemType))
            throw new CmisRuntimeException("no category node");
        Repository repository =  Repository.getInstance();
        MockUtils mockUtils = MockUtils.getInstance();
        return (TreeNode<T>) repository.insert(null, mockUtils.createFileableCmisObject(repository, null, repository.getCategoryroot().getPath(), name, mockUtils.getItemType(), null), false);
    }

    public TreeNode<T> createSubCategory(String name) {
        if (!(((FileableCmisObject) getObj()).getBaseType() instanceof ItemType))
            throw new CmisRuntimeException("no category node");
        Repository repository =  Repository.getInstance();
        MockUtils mockUtils = MockUtils.getInstance();
        return (TreeNode<T>) repository.insert(null, mockUtils.createFileableCmisObject(repository, null, repository.getCategoryroot().getPath(), name, mockUtils.getItemType(), null), false);
    }

    public void save() {
        for (String key: properties.keySet())
            MockUtils.getInstance().fillProperty(key, properties.get(key));
    }


    @Override
    public Iterator<TreeNode<T>> iterator() {
        return new TreeNodeIter<T>(this);
    }

    @Override
    public int compareTo(Object o) {
        return this.getId().compareTo(((TreeNode<T>) o).getId());
    }

    public void setContent(String content) {
        this.content = content;
    }

    void updateProperty(String name, Object value) {
        ((PropertyImpl) ((FileableCmisObject) getObj()).getProperty(PropertyIds.CONTENT_STREAM_ID)).setValue(value);
        properties._put(name,value);
    }

    private void copyValues() {
        for (Property property: obj.getProperties()){
            properties._put(property.getId(), property.getValue());
        }
    }

}