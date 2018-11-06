package de.ksul.archiv.repository;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.ksul.archiv.repository.script.AlfrescoScriptApi;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
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
public class TreeNode<T> implements Iterable<TreeNode<T>>, Comparable, AlfrescoScriptApi<T> {

    @JsonIgnore
    private static Logger logger = LoggerFactory.getLogger(TreeNode.class);
    private String id;
    public String name;
    private String path;
    public String displayPath = "";
    private Type obj;
    private TreeMap<String, Type> versions = new TreeMap<>(Collections.reverseOrder());
    public List<TreeNode<T>> parents = new ArrayList<>();
    public String content;
    public String mimetype;
    @JsonProperty("childs")
    Map<String, TreeNode<T>> childs;
    public NodeProperties properties;
    public NodeProperties childAssocs;

    public boolean isLeaf() {
        return childs.size() == 0;
    }

    public TreeNode() {
    }

    TreeNode(FileableCmisObject obj) {
        if (!obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_ID)).findFirst().isPresent())
            throw new CmisRuntimeException("objectId not found");
        this.id = ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_ID)).findFirst().get()).getValueAsString();
        if (!obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.NAME)).findFirst().isPresent())
            throw new CmisRuntimeException("object name not found");
        this.name = ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.NAME)).findFirst().get()).getValueAsString();
        this.path = "";
        this.obj = new Type(obj.getProperties(), obj.getType(), obj.getSecondaryTypes());
        this.childs = new HashMap<>();
        this.properties = new NodeProperties(this.obj);
        this.childAssocs = new NodeProperties(this.obj);
        copyValues();
    }

    TreeNode(FileableCmisObject obj, String version) {
        if (!obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_ID)).findFirst().isPresent())
            throw new CmisRuntimeException("objectId not found");
        this.id = ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_ID)).findFirst().get()).getValueAsString();
        if (!obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.NAME)).findFirst().isPresent())
            throw new CmisRuntimeException("object name not found");
        this.name = ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.NAME)).findFirst().get()).getValueAsString();
        this.path = "";
        this.obj = new Type(obj.getProperties(), obj.getType(), obj.getSecondaryTypes());
        this.childs = new HashMap<>();
        this.properties = new NodeProperties(this.obj);
        this.childAssocs = new NodeProperties(this.obj);
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
        return this.parents.isEmpty();
    }

    public Type getType() {
        return obj;
    }

    public T getObj() {
        return (T) MockUtils.getInstance().createObject(obj);
    }

    public T getObj(String version) {
        if (obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.VERSION_LABEL)).findFirst().get().getValueAsString().equals(version))
            return (T) MockUtils.getInstance().createObject(obj);
        if (!versions.containsKey(version))
            throw new CmisVersioningException("version not found");
        LinkedHashMap<String, Property<?>> props = new LinkedHashMap<>();
        return (T) MockUtils.getInstance().createObject(versions.get(version));
    }


    public boolean containsData(String version) {
        return versions.containsKey(version);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    protected void setPath(String path) {
        this.path = path;
    }

    public String getId() {
        return id;
    }

    TreeNode<T> getParent() {
        return parents.isEmpty() ? null :parents.get(0);
    }

    private void registerChild(TreeNode<T> node) {
        node.parents.add(this);
        node.path = this.path + (this.path.equals("/") ? "" : "/") + node.getName();
        node.displayPath = this.displayPath + "/" + this.getName();
        this.childs.put(node.getName(), node);
    }

    private void deRegisterChild(TreeNode<T> node) {

        if (this.childs.containsKey(node.name)) {
            this.childs.remove(node.name);
        }
    }

    @Override
    public void addNode(TreeNode<T> node) {
        this.registerChild(node);
    }

    @Override
    public boolean move(TreeNode<T> newParent) {
        try {
            if (!this.parents.isEmpty())
                this.parents.get(0).deRegisterChild(this);
            newParent.registerChild(this);
            updateProperty(PropertyIds.PARENT_ID, newParent.getId());
            if (newParent.getName().equalsIgnoreCase("Inbox")) {
                Repository.getInstance().executeScript(this);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    void updateNode(List<Property<?>> properties) {
        obj.setProperties(properties);
        copyValues();
    }

    int getLevel() {
        if (this.isRoot())
            return 0;
        else
            return getParent().getLevel() + 1;
    }

    GregorianCalendar getCreationDate() {
        if (!obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.CREATION_DATE)).findFirst().isPresent())
            return null;
        return obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.CREATION_DATE)).findFirst().get().getValue();
    }

    GregorianCalendar getModifiedDate() {
        if (!obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.LAST_MODIFICATION_DATE)).findFirst().isPresent())
            return null;
        return obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.LAST_MODIFICATION_DATE)).findFirst().get().getValue();
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
                if (parentNode != null && !node.getParent().equals(parentNode))
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
            if (!found)
                return null;
        }
        return n;
    }


    TreeMap<String, Type> getAllVersions() {
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

    private void changePropertie(String id, Object value) {
        Map<String, PropertyData<?>> props = MockUtils.getInstance().convProperties(obj.getProperties());
        ((PropertyImpl) props.get(id)).setValue(value);
        obj.setProperties(MockUtils.getInstance().convPropertyData(props));
    }

    @Override
    public TreeNode<T> transformDocument(String mimeType) {
        if (mimeType.equals("text/plain")) {
            String newName = getName().substring(0, getName().lastIndexOf(".") + 1) + "txt";
            String newPath = path.substring(0, path.lastIndexOf("/"));
            FileableCmisObject newObject = MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), MockUtils.getInstance().convProperties(obj.getProperties()), newPath, newName, this.obj.getObjectType(), "text/plain");
            TreeNode<T> transform = (TreeNode<T>) Repository.getInstance().insert((TreeNode<FileableCmisObject>) this.getParent(), newObject, false);
            transform.setContent(this.content);
            return transform;
        }
        return null;
    }

    @Override
    public boolean remove() {
        try {
            for (TreeNode<T> parent : this.parents)
                parent.deRegisterChild(this);
            logger.info(this.name + " [ID: " + this.id + "] [Path: " + this.path + "] deleted from repository!");
            Repository.getInstance().getNodes().remove(this.id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public TreeNode<T> createFolder(String name, String type) {
        if (type.equals("cm:folder"))
            type = "cmis:folder";
        FileableCmisObject newFolder = MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, this.getPath(), name, MockUtils.getInstance().getFolderType(type), null);
        TreeNode<T> newNode = (TreeNode<T>) Repository.getInstance().insert((TreeNode<FileableCmisObject>) this, newFolder, false);
        newNode.specializeType(type);
        return newNode;
    }

    @Override
    public TreeNode<T> createFolder(String name) {
        return createFolder(name, "cm:folder");
    }

    @Override
    public TreeNode<T> createNode(String name, String type, String aspect) {
        if (!type.startsWith("F:"))
            type = "F:" + type;
        FileableCmisObject newComment = MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, this.getPath(), name, MockUtils.getInstance().getFolderType(type), null);
        TreeNode<T> node = (TreeNode<T>) Repository.getInstance().insert((TreeNode<FileableCmisObject>) this, newComment, true);
        //node.addAspect(aspect);
        return node;
    }

    @Override
    public TreeNode<T> createNode(String name, String type) {
        return createNode(name, type, null);
    }


    @Override
    public TreeNode<T> createFile(String name) {
        return createFile(name, "cm:content");
    }

    @Override
    public TreeNode<T> createFile(String name, String type) {
        if (type.equals("cm:content"))
            type = "cmis:document";
        FileableCmisObject newNode = MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, this.getPath(), name, MockUtils.getInstance().getDocumentType(type), null);
        TreeNode<T> node = (TreeNode<T>) Repository.getInstance().insert((TreeNode<FileableCmisObject>) this, newNode, false);

        return node;
    }

    @Override
    public boolean isSubType(String name) {
        String typeName = obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.OBJECT_TYPE_ID)).findFirst().get().getValue();
        return MockUtils.getInstance().getPropertyDefinitionBuilder().isSubtypeOf(typeName, name);
    }

    @Override
    public void specializeType(String key) {

        Map<String, PropertyDefinition<?>> definitionMap = MockUtils.getInstance().getPropertyDefinitionBuilder().getPropertyDefinitionMap(key);
        obj.getObjectType().getPropertyDefinitions().putAll(definitionMap);
        for (PropertyDefinition propertyDefinition : definitionMap.values()) {
            if (!obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(propertyDefinition.getId())).findFirst().isPresent()) {
                obj.getProperties().add(new PropertyImpl<>(propertyDefinition, new ArrayList<>()));
                properties._put(propertyDefinition.getId(), null);
            }
        }
    }

    @Override
    public void addAspect(String k) {
        final String key = k.startsWith("P:") ? k : "P:" + k;
        if (!((List) properties.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)).contains(key))
            ((List) properties.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)).add(key);
        Map<String, PropertyDefinition<?>> definitionMap = MockUtils.getInstance().getPropertyDefinitionBuilder().getPropertyDefinitionMap(key);
        obj.getObjectType().getPropertyDefinitions().putAll(definitionMap);
        for (PropertyDefinition propertyDefinition : definitionMap.values()) {
            if (!obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(propertyDefinition.getId())).findFirst().isPresent()) {
                obj.getProperties().add(new PropertyImpl<>(propertyDefinition, new ArrayList<>()));
                if (!obj.getObjectType().getPropertyDefinitions().containsKey(propertyDefinition.getId()))
                    obj.getObjectType().getPropertyDefinitions().put(propertyDefinition.getId(), propertyDefinition);
                properties._put(propertyDefinition.getId(), null);
            }
        }
    }

    @Override
    public boolean hasAspect(String key) {
        return ((PropertyImpl) obj.getProperties().stream().filter(e -> e.getId().equalsIgnoreCase(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)).findFirst().get()).getValues().contains(key);
    }

    @Override
    public void addTag(String name) {
        //TODO Implementierung
    }

    @Override
    public void createAssociation(Object target, String name) {
        childAssocs.put(name, target);
    }

    @Override
    public List<TreeNode<T>> getRootCategories(String name) {
        if (!(((FileableCmisObject) getObj()).getBaseType() instanceof ItemType))
            throw new CmisRuntimeException("no category node");
        return null;
    }

    @Override
    public TreeNode<T> createRootCategory(String aspect, String name) {
        if (!(((FileableCmisObject) getObj()).getBaseType() instanceof ItemType))
            throw new CmisRuntimeException("no category node");
        Repository repository = Repository.getInstance();
        MockUtils mockUtils = MockUtils.getInstance();
        return (TreeNode<T>) repository.insert(null, mockUtils.createFileableCmisObject(repository, null, repository.getCategoryroot().getPath(), name, mockUtils.getItemType(), null), false);
    }

    @Override
    public TreeNode<T> createSubCategory(String name) {
        if (!(((FileableCmisObject) getObj()).getBaseType() instanceof ItemType))
            throw new CmisRuntimeException("no category node");
        Repository repository = Repository.getInstance();
        MockUtils mockUtils = MockUtils.getInstance();
        return (TreeNode<T>) repository.insert(null, mockUtils.createFileableCmisObject(repository, null, repository.getCategoryroot().getPath(), name, mockUtils.getItemType(), null), false);
    }

    @Override
    public void save() {

        if (mimetype != null)
            updateProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE, mimetype);
        if (content != null) {

            try {
                InputStream stream = new ByteArrayInputStream(content.getBytes("UTF-8"));
                ContentStream contentStream = new ContentStreamImpl(null, BigInteger.valueOf(content.length()), mimetype, stream);
                Repository.getInstance().createContent(getId(), contentStream, true);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        MockUtils mockUtils = MockUtils.getInstance();

        HashMap<String, PropertyData<?>> map = new HashMap<>();
        for (String key : properties.keySet())
            map.put(key, mockUtils.fillProperty(key, properties.get(key)));

        List<Property<?>> propertyList = MockUtils.getInstance().convPropertyData(map);

        for (Property<?> property : propertyList) {
            Optional<Property<?>> p = this.getType().getProperties().stream().filter(e -> e.getQueryName().equals(property.getQueryName())).findFirst();
            if (p.isPresent())
                ((PropertyImpl) p.get()).setValues(property.getValues());
            else
                this.getType().getProperties().add(property);
        }

        for (String key : (Iterable<String>) ((ArrayList) properties.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS))) {
            Optional<SecondaryType> opt1 = obj.getSecondaryTypes().stream().filter(e -> e.getId().equalsIgnoreCase(key)).findFirst();
            if (!opt1.isPresent()) {
                ObjectType type = Repository.getSession().getTypeDefinition(key);
                if (type instanceof SecondaryType)
                    obj.getSecondaryTypes().add((SecondaryType) type);
            }
        }
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

    public String getContent() {
        return content;
    }

    void updateProperty(String name, Object value) {
        Optional<Property<?>> p = ((FileableCmisObject) getObj()).getProperties().stream().filter(e -> e.getQueryName().equals(name)).findFirst();
        if (p.isPresent()) {
            ((PropertyImpl) p.get()).setValue(value);
            properties._put(name, value);
        }
    }

    private void copyValues() {
        for (Property property : obj.getProperties()) {
            properties._put(property.getId(), property.getValue());
            if (property.getId().equalsIgnoreCase(PropertyIds.CONTENT_STREAM_MIME_TYPE))
                mimetype = property.getValueAsString();
        }
    }

}