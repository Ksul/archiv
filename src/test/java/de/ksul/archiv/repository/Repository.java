package de.ksul.archiv.repository;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.ksul.archiv.configuration.ArchivTestProperties;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 4/12/17
 * Time: 11:24 AM
 */
@Component
public class Repository {

    private String file;

    private static Logger logger = LoggerFactory.getLogger(Repository.class.getName());

    private TreeNode<FileableCmisObject> root;


    @Autowired
    public Repository(ArchivTestProperties archivTestProperties) {
        this.file = archivTestProperties.getTestData();
    }

    String UUId() {
        return UUID.randomUUID().toString();
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
        TreeNode<FileableCmisObject> startNode = root.findTreeNodeForId(id);
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
        TreeNode<FileableCmisObject> startNode = root.findTreeNodeForId(id);
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
        TreeNode<FileableCmisObject> node = root.findTreeNodeForId(id);
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
        TreeNode<FileableCmisObject> node = root.findTreeNodeForId(id);
        if (node == null)
            throw new RuntimeException("Node with Id " + id + " not found!");
        List<Property<?>> props = node.getObj().getProperties();
        LinkedHashMap<String, Property<?>> newProps = new LinkedHashMap<>();
        for (Property<?> p : props) {
            newProps.put(p.getId(), new PropertyImpl(p));
        }
        Iterator<String> it = properties.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            newProps.put(key,  new PropertyImpl(MockUtils.getInstance().getAllPropertyDefinitionMap().get(key), properties.get(key).getValues()));
        }
        node.updateNode(newProps);

//        for (PropertyData p : properties.) {
//
//            PropertyImpl property = new PropertyImpl()
//            properties.addProperty(property);
//        }
//        Iterator<String> it = versionProps.keySet().iterator();
//        while (it.hasNext()) {
//            String key = it.next();
//            props.put(key, new PropertyImpl<>(versionProps.get(key)));
//        }
//        node.updateNode(cmisObjectNew, version);
//        logger.info(cmisObject.getName() + " [ID: " + cmisObject.UUId() + "][Path: " + cmisObject.getPaths().get(0) + "] changed in repository!");

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
        } else {
            if (root == null)
                throw new RuntimeException("no Root Node!");
            TreeNode<FileableCmisObject> node = root.findTreeNodeForPath(parentPath);
            if (node != null) {
                node.addNode(id, name, cmisObject, version);
                if (contentStream != null) {
                    ((Document) cmisObject).setContentStream(contentStream, true);
                }
            }
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
            return node.getObj();
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
                return node.getObj();
            else if (node.containsData(parts[1]))
                return node.getObj(parts[1]);
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

        return node.makeNewVersion(version).getObj();
    }

    List<ObjectData> getAllVersions(String id) {
        if (root == null)
            throw new RuntimeException("no Root Node!");
        if (id == null)
            throw new RuntimeException("id must be set!");
        TreeNode<FileableCmisObject> node = root.findTreeNodeForId(id);
        TreeMap<String, LinkedHashMap<String, Property<?>>> allVersions = node.getAllVersions();
        List<ObjectData> versions = new ArrayList<>();
        Collection<PropertyData<?>> props = new ArrayList<>();
        Iterator<String> it = allVersions.keySet().iterator();
        while (it.hasNext()) {
            ObjectDataImpl objectData = new ObjectDataImpl();
            String key = it.next();
            for (Property p : allVersions.get(key).values()) {
                props.add(MockUtils.getInstance().fillProperty(p.getId(), p.getValue()));
            }
            Properties properties = new PropertiesImpl(props);
            objectData.setProperties(properties);
            versions.add(objectData);
        }
        return versions;
    }


    ContentStream getContent(String id) {
        if (id == null)
            throw new RuntimeException("id must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        return root.findTreeNodeForId(id).getContent();
    }

    void createContent(String objectId, ContentStream content, boolean overwrite) {
        if (objectId == null)
            throw new RuntimeException("id must be set!");
        if (root == null)
            throw new RuntimeException("no Root Node!");
        TreeNode<FileableCmisObject> node = root.findTreeNodeForId(objectId);
        String uuid = node.createContent(content, overwrite);
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

    @PreDestroy
    public void shutDown() throws IOException {
        if (file != null && !file.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.configure(SerializationFeature.INDENT_OUTPUT,true);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.writeValue(new File(file), root);
        }

    }

    @PostConstruct
    public void setup() throws Exception{
        if (file != null && !file.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            File jsonFile = new File(file);
            if (jsonFile.exists())
                root = mapper.readValue(jsonFile, new TypeReference<TreeNode<?>>() {
            });
        }
    }
}
