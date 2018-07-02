package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 4/12/17
 * Time: 11:24 AM
 */
public class Repository {

    private static Logger logger = LoggerFactory.getLogger(Repository.class.getName());

    TreeNode<FileableCmisObject> root;


    private String rootId;

    String getId() {
        return UUID.randomUUID().toString();
    }

    String getRootId() {
        return rootId;
    }

    void setRootId(String id) {
        this.rootId = UUID.randomUUID().toString();
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
                list.add(node.getData());
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
        TreeNode<FileableCmisObject> startNode = root.findTreeNodeForId(id);
        if (startNode != null) {
            for (TreeNode<FileableCmisObject> node : startNode) {
                if (node.getParent() != null && node.getParent().equals(startNode)) {
                    skip--;
                    if (skip <= 0)
                        ret.add(node.getData());
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
        TreeNode<FileableCmisObject> startNode = root.findTreeNodeForId(id);
        if (startNode != null) {
            for (TreeNode<FileableCmisObject> node : startNode) {
                if (node.getParent() != null && node.getParent().equals(startNode)) {
                        ret.add(node.getData());
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
        TreeNode<FileableCmisObject> node = root.findTreeNodeForId(id);
        if (node == null)
            return null;
        else {
            if (node.getParent() == null)
                return null;
            else
                return node.getParent().getData();
        }
    }

    void update(FileableCmisObject cmisObject, FileableCmisObject cmisObjectNew, String version) {
        if (cmisObject == null)
            throw new RuntimeException("cmisObject must be set!");
        if (cmisObjectNew == null)
            throw new RuntimeException("cmisObjectNew must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        TreeNode<FileableCmisObject> node = root.findTreeNodeForId(cmisObject.getId());
        if (node == null)
            throw new RuntimeException("Node with Id " + cmisObject.getId() + " not found!");
        else
        node.updateNode(cmisObjectNew, version);
        logger.info(cmisObject.getName() + " [ID: " + cmisObject.getId() + "][Path: " + cmisObject.getPaths().get(0) + "] changed in repository!");
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
        root.findTreeNodeForId(cmisObject.getId()).removeNode();
    }

    FileableCmisObject move(String parentId, FileableCmisObject cmisObject){
        if (cmisObject == null)
            throw new RuntimeException("cmisObject not set!");
        if (parentId == null)
            throw new RuntimeException("parentId must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        TreeNode<FileableCmisObject> node = root.findTreeNodeForId(cmisObject.getId());
        TreeNode<FileableCmisObject> parent = root.findTreeNodeForId(parentId);
        if (node == null)
            throw new RuntimeException("Node with Id " + cmisObject.getId() + " not found!");
        if (parent == null)
            throw new RuntimeException("Node with Id " + parentId + " not found!");
        TreeNode<FileableCmisObject> sourceFolder = node.getParent();
        TreeNode<FileableCmisObject> movedNode = node.moveNode(parent);
        logger.info(node.getName() + " [ID: " + cmisObject.getId() + "] from Path: " + sourceFolder.getName() + " [ID: " + sourceFolder.getId() + "] to Path: " + parent.getName() + " [ID: " + parent.getId() + "] moved!");
        return movedNode.getData();
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
        } else {
            if (root == null)
                throw new RuntimeException("no Root Node!");
            TreeNode<FileableCmisObject> node = root.findTreeNodeForPath(parentPath);
            if (node != null)
                node.addNode(id, name, cmisObject, contentStream, version);
            else
                throw new RuntimeException("Parent " + parentPath + " not found!");
        }
        logger.info(name + " [ID: " + cmisObject.getId() + "] [Path: " + cmisObject.getPaths().get(0) + "] inserted into repository!");
    }


    boolean containsPath(String path) {
        if (root == null)
            throw new RuntimeException("no Root Node!");
        if (path == null)
            throw new RuntimeException("path must be set!");
        path = path.replace(".", "\\.");
        path = path.replace("?", ".");
        path = path.replace("%", ".*");
        return root.containsPath(path);
    }

    boolean containsId(String id) {
        if (root == null)
            throw new RuntimeException("no Root Node!");
        if (id == null)
            throw new RuntimeException("id must be set!");
        return root.containsId(id);
    }

    FileableCmisObject getByPath(String path) {
        if (root == null)
            throw new RuntimeException("no Root Node!");
        if (path == null)
            throw new RuntimeException("path must be set!");
        if (path.length() > 1 && path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        TreeNode<FileableCmisObject> node = root.findTreeNodeForPath(path);
        if (node != null)
            return node.getData();
        else
            return null;
    }

    FileableCmisObject getById(String id) {
        if (root == null)
            throw new RuntimeException("no Root Node!");
        if (id == null)
            throw new RuntimeException("id must be set!");
        TreeNode<FileableCmisObject> node = root.findTreeNodeForId(id);
        if (node != null) {
            String[] parts = id.split(";");
            if (parts.length == 1)
                return node.getData();
            else if (node.containsData(parts[1]))
                return node.getData(parts[1]);
            else
                throw new CmisObjectNotFoundException(("version not found!"));
        }
        else
            return null;
    }

    FileableCmisObject makeNewVersion(String id, String version) {
        if (root == null)
            throw new RuntimeException("no Root Node!");
        if (id == null)
            throw new RuntimeException("id must be set!");
        TreeNode<FileableCmisObject> node = root.findTreeNodeForId(id);
        return node.makeNewVersion(version).getData();
    }

    ContentStream getContent(FileableCmisObject cmisObject) {
        if (cmisObject == null)
            throw new RuntimeException("cmisObject must be set!");
        return getContent(new ObjectIdImpl(cmisObject.getId()));
    }

    ContentStream getContent(ObjectId id) {
        if (id == null)
            throw new RuntimeException("id must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        return root.findTreeNodeForId(id.getId()).getContentStream();
    }

    void changeContent(FileableCmisObject cmisObject, ContentStream newContent) {
        ContentStreamImpl streamCurrent = (ContentStreamImpl) getContent(new ObjectIdImpl(cmisObject.getId()));
        streamCurrent.setStream(newContent.getStream());
        logger.info(cmisObject.getName() + " [ID: " + cmisObject.getId() + "] changed content!");
    }
}
