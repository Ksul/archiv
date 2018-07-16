package de.ksul.archiv.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 4/12/17
 * Time: 11:24 AM
 */
public class Repository {

    private static Logger logger = LoggerFactory.getLogger(Repository.class.getName());
    private String rootId ;
    private TreeNode<FileableCmisObject> root;
    @JsonProperty("nodes")
    private TreeMap<String, TreeNode<FileableCmisObject>> nodes = new TreeMap<>() ;
    @JsonProperty("contents")
    private TreeMap<String, ContentStream> contents = new TreeMap<>();
    @JsonProperty("contentIds")
    private TreeMap<String, String> contentIds = new TreeMap<>();

    public Repository() {
    }

    String UUId() {
        return UUID.randomUUID().toString();
    }

    String getRootId() {
        return rootId;
    }

    List<FileableCmisObject> query(String query) {
        if (query == null)
            throw new RuntimeException("query must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        List<FileableCmisObject> list = new ArrayList<>();
        query = query.replace(".", "\\.");
        query = query.replace("?", ".");
        query = query.replace("%", ".*");
        for (TreeNode<FileableCmisObject> node : root){
            if (node.getName().matches(query))
                list.add(node.getObj());
        }
        return list;
    }

    List<FileableCmisObject> getChildren(String id) {
        return getChildren(id, 0, Integer.MAX_VALUE);
    }

    List<FileableCmisObject> getChildren(String id, int skip, int maxItems) {
        if (id == null)
            throw new RuntimeException("id must be set!");
        int i = 0;
        List<FileableCmisObject> ret = new ArrayList<>();
        if (root == null)
            throw new RuntimeException("no Root Node!");
        TreeNode<FileableCmisObject> startNode = findTreeNodeForId(id);
        if (startNode != null) {
            for (TreeNode<FileableCmisObject> node : startNode) {
                if (node.getParent() != null && node.getParent().equals(startNode)) {
                    skip--;
                    if (skip <= 0)
                        ret.add(node.getObj());
                    if (i >= maxItems)
                        break;
                    i++;
                }
            }
        }
        return ret;
    }

    List<FileableCmisObject> getChildrenForAllLevels(String id) {
        if (id == null)
            throw new RuntimeException("id must be set!");
        List<FileableCmisObject> ret = new ArrayList<>();
        if (root == null)
            throw new RuntimeException("no Root Node!");
        TreeNode<FileableCmisObject> startNode = findTreeNodeForId(id);
        if (startNode != null) {
            for (TreeNode<FileableCmisObject> node : startNode) {
                if (node.getParent() != null && node.getParent().equals(startNode)) {
                        ret.add(node.getObj());
                        ret.addAll(getChildrenForAllLevels(node.getId()));
                }
            }
        }
        return ret;
    }

    FileableCmisObject getParent(String id){
        if (id == null)
            throw new RuntimeException("id must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(id);
        if (node == null)
            return null;
        else {
            if (node.getParent() == null)
                return null;
            else
                return node.getParent().getObj();
        }
    }

    void update(String id , Map<String, PropertyData<?>> properties) {
        if (id == null)
            throw new RuntimeException("id must be set!");
        if (properties == null)
            throw new RuntimeException("properties must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(id);
        if (node == null)
            throw new RuntimeException("Node with Id " + id + " not found!");
        List<Property<?>> props = node.getObj().getProperties();
        List<Property<?>> newProps = new ArrayList<>();
        for (Property<?> p : props) {
            newProps.add( new PropertyImpl(p));
        }
        Iterator<String> it = properties.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            newProps.add( new PropertyImpl(MockUtils.getInstance().getAllPropertyDefinitionMap().get(key), properties.get(key).getValues()));
        }
        node.updateNode(newProps);

    }

    List<String> deleteTree(FileableCmisObject cmisObject) {
        if (cmisObject == null)
            throw new RuntimeException("cmisObject must be set!");
        delete(cmisObject);
        return new ArrayList<>();
    }

    void delete(FileableCmisObject cmisObject) {
        if (cmisObject == null)
            throw new RuntimeException("cmisObject must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        logger.info(cmisObject.getName() + " [ID: " + cmisObject.getId() + "] [Path: " + cmisObject.getPaths().get(0) + "] deleted from repository!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(cmisObject.getId());
        node.removeNode();
        nodes.remove(node.getId());
    }

    FileableCmisObject move(String parentId, FileableCmisObject cmisObject){
        if (cmisObject == null)
            throw new RuntimeException("cmisObject not set!");
        if (parentId == null)
            throw new RuntimeException("parentId must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(cmisObject.getId());
        TreeNode<FileableCmisObject> parent = findTreeNodeForId(parentId);
        if (node == null)
            throw new RuntimeException("Node with Id " + cmisObject.getId() + " not found!");
        if (parent == null)
            throw new RuntimeException("Node with Id " + parentId + " not found!");
        TreeNode<FileableCmisObject> sourceFolder = node.getParent();
        TreeNode<FileableCmisObject> movedNode = node.moveNode(parent);
        logger.info(node.getName() + " [ID: " + cmisObject.getId() + "] from Path: " + sourceFolder.getName() + " [ID: " + sourceFolder.getId() + "] to Path: " + parent.getName() + " [ID: " + parent.getId() + "] moved!");
        return movedNode.getObj();
    }

    void insert(String parent, FileableCmisObject cmisObject) {
        insert(parent, cmisObject, null, null);
    }

    void insert(String parentPath, FileableCmisObject cmisObject, ContentStream contentStream, String version) {
        if (cmisObject == null)
            throw new RuntimeException("cmisObject must be set!");
        String name = cmisObject.getName();
        String id = cmisObject instanceof Folder ? cmisObject.getId() : ((Document) cmisObject).getVersionSeriesId();
        if (parentPath == null) {
            root = new TreeNode<>(id, name, cmisObject);
            nodes.put(root.getId(), root);
            rootId = id;
        } else {
            if (root == null)
                throw new RuntimeException("no Root Node!");
            TreeNode<FileableCmisObject> node = findTreeNodeForPath(parentPath);
            if (node != null) {
                TreeNode<FileableCmisObject> newNode = node.addNode(id, name, cmisObject, version);
                nodes.put(newNode.getId(), newNode);
                if (contentStream != null) {
                    ((Document) cmisObject).setContentStream(contentStream, true);
                }
            }
            else
                throw new RuntimeException("Parent " + parentPath + " not found!");
        }
        logger.info(name + " [ID: " + cmisObject.getId() + "] [Path: " + cmisObject.getPaths().get(0) + "] inserted into repository!");
    }

    TreeNode<FileableCmisObject> findTreeNodeForId(String id) {
        if (id == null)
            throw new RuntimeException("Id must be set!");
        String[] parts = id.split(";");
        if (nodes.containsKey(parts[0]))
            return nodes.get(parts[0]);
        return null;
    }

    TreeNode<FileableCmisObject> findTreeNodeForPath (String path) {
        if (path == null)
            throw new RuntimeException("Path must be set!");
        for (TreeNode<FileableCmisObject> element : nodes.values()) {
            String objectPath = element.getPath();
            if (objectPath != null && path.matches(objectPath))
                return element;
        }
        return null;
    }


    boolean containsPath(String path) {
        if (root == null)
            throw new RuntimeException("no Root Node!");
        if (path == null)
            throw new RuntimeException("path must be set!");
        path = path.replace(".", "\\.");
        path = path.replace("?", ".");
        path = path.replace("%", ".*");
        for (TreeNode<?> element : nodes.values()) {
            if (element.getPath().matches(path)){
                return true;
            }
        }
        return false;
    }

    FileableCmisObject getByPath(String path) {
        if (root == null)
            throw new RuntimeException("no Root Node!");
        if (path == null)
            throw new RuntimeException("path must be set!");
        if (path.length() > 1 && path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        TreeNode<FileableCmisObject> node = findTreeNodeForPath(path);
        if (node != null)
            return node.getObj();
        else
            return null;
    }

    FileableCmisObject getById(String id) {
        if (root == null)
            throw new RuntimeException("no Root Node!");
        if (id == null)
            throw new RuntimeException("id must be set!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(id);
        if (node != null) {
            String[] parts = id.split(";");
            if (parts.length == 1)
                return node.getObj();
            else if (node.containsData(parts[1]))
                return node.getObj(parts[1]);
            else
                throw new CmisObjectNotFoundException(("version not found!"));
        }
        else
            return null;
    }

    FileableCmisObject checkout(String id) {
        if (root == null)
            throw new RuntimeException("no Root Node!");
        if (id == null)
            throw new RuntimeException("id must be set!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(id);
        if (node.checkout() == null)
            throw new CmisVersioningException("is PWC");
        return node.getObj();

    }

    FileableCmisObject checkin(String id, String version, String checkinComment) {
        if (root == null)
            throw new RuntimeException("no Root Node!");
        if (id == null)
            throw new RuntimeException("id must be set!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(id);
        node.checkin(version, checkinComment);
        return node.getObj();
    }

    List<ObjectData> getAllVersions(String id) {
        if (root == null)
            throw new RuntimeException("no Root Node!");
        if (id == null)
            throw new RuntimeException("id must be set!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(id);
        TreeMap<String, Type> allVersions = node.getAllVersions();
        List<ObjectData> versions = new ArrayList<>();
        Iterator<Type> it = allVersions.values().iterator();
        while (it.hasNext()) {
            ObjectData objectData = MockUtils.getInstance().getObjectDataFromProperties(it.next().getProperties());
            versions.add(objectData);
        }
        return versions;
    }

    ContentStream getContent(String id) {
        if (id == null)
            throw new RuntimeException("id must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(id);
        if ( !contents.containsKey(node.getObj().getProperty(PropertyIds.CONTENT_STREAM_ID).getValueAsString()))
            return null;
        return contents.get(node.getObj().getProperty(PropertyIds.CONTENT_STREAM_ID).getValueAsString());
    }

    void createContent(String objectId, ContentStream content, boolean overwrite) {
        if (objectId == null)
            throw new RuntimeException("id must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(objectId);
        if (contentIds.containsKey(node.getId()) && !overwrite)
            throw new CmisContentAlreadyExistsException();
        String uuid = UUID.randomUUID().toString();
        contents.put(uuid, content);
        contentIds.put(node.getId(), uuid);
        ((PropertyImpl) node.getObj().getProperty(PropertyIds.CONTENT_STREAM_ID)).setValue(uuid);
    }

    void changeContent(String objectId, ContentStream newContent) {
        if (objectId == null)
        throw new RuntimeException("id must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        ContentStreamImpl streamCurrent = (ContentStreamImpl) getContent(objectId);
        streamCurrent.setStream(newContent.getStream());
        logger.info("[ID: " + objectId + "] changed content!");
    }



}
