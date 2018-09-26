package de.ksul.archiv.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ksul.archiv.PDFConnector;
import de.ksul.archiv.VerteilungConstants;
import de.ksul.archiv.repository.script.RecognizeEndpoints;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.collections4.list.SetUniqueList;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 4/12/17
 * Time: 11:24 AM
 */
public class Repository {

    private static Logger logger = LoggerFactory.getLogger(Repository.class.getName());
    private TreeNode<FileableCmisObject> root;
    private TreeNode<FileableCmisObject> categoryroot;
    @JsonProperty("nodes")
    private TreeMap<String, TreeNode<FileableCmisObject>> nodes = new TreeMap<>() ;
    @JsonProperty("contents")
    private TreeMap<String, ContentStream> contents = new TreeMap<>();
    @JsonProperty("contentIds")
    private TreeMap<String, String> contentIds = new TreeMap<>();
    @JsonIgnore
    public static Repository repository;


    private Repository() {
    }

    public static Repository getInstance() {
        if (repository == null)
            repository = new Repository();
        return repository;
    }

    static void setInstance(Repository repo) {
        repository = repo;
    }

    String UUId() {
        return UUID.randomUUID().toString();
    }

    String getRootId() {
        return root.getId();
    }

    public TreeNode<FileableCmisObject> getRoot() {
        return root;
    }

    public TreeNode<FileableCmisObject> getCategoryroot() {
        return categoryroot;
    }

    List<FileableCmisObject> query(String query) {
        if (query == null)
            throw new RuntimeException("query must be set!");
        if (root == null)
            throw new RuntimeException("query:no Root Node!");
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
            throw new RuntimeException("getChildren:no Root Node!");
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
            throw new RuntimeException("getChildrenForAllLevels:no Root Node!");
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
            throw new RuntimeException("getParent:no Root Node!");
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

    void update(String id, Map<String, PropertyData<?>> properties) {
        if (id == null)
            throw new RuntimeException("id must be set!");
        if (properties == null)
            throw new RuntimeException("properties must be set!");
        if (root == null)
            throw new RuntimeException("update:no Root Node!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(id);
        if (node == null)
            throw new RuntimeException("Node with Id " + id + " not found!");
        List<Property<?>> props = node.getObj().getProperties();
        SetUniqueList<Object> secondaryTypes = SetUniqueList.setUniqueList(node.getObj().getProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues());
        if (properties.containsKey(PropertyIds.SECONDARY_OBJECT_TYPE_IDS))
            secondaryTypes.addAll(properties.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues());
        for (Object secondaryType : secondaryTypes) {
            node.getObj().getType().getPropertyDefinitions().putAll(MockUtils.getInstance().getPropertyDefinitionBuilder().getPropertyDefinitionMap((String) secondaryType));
        }
        List<Property<?>> newProps = new ArrayList<>();
        newProps.addAll(props);
        Iterator<String> it = properties.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (newProps.stream().filter(e -> e.getId().equalsIgnoreCase(key)).findFirst().isPresent()) {
                if (node.getType().getObjectType().getPropertyDefinitions().get(key).getCardinality().equals(Cardinality.SINGLE))
                    ((PropertyImpl) newProps.stream().filter(e -> e.getId().equalsIgnoreCase(key)).findFirst().get()).setValue(properties.get(key).getFirstValue());
                else
                    ((PropertyImpl) newProps.stream().filter(e -> e.getId().equalsIgnoreCase(key)).findFirst().get()).getValues().addAll(properties.get(key).getValues());
            } else
                newProps.add( new PropertyImpl(MockUtils.getInstance().getPropertyDefinitionBuilder().getCmisDictionaryService().findProperty(key).getPropertyDefinition(), properties.get(key).getValues()));

        }
        node.updateNode(newProps);

    }

    void deleteTree(String id) {
        if (id == null)
            throw new RuntimeException("Id must be set!");
        delete(id);
        return;
    }

    void delete(String id) {
        if (id == null)
            throw new RuntimeException("Id must be set!");
        if (root == null)
            throw new RuntimeException("delete:no Root Node!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(id);
        logger.info(node.getName() + " [ID: " + node.getId() + "] [Path: " + node.getPath() + "] deleted from repository!");
        node.removeNode();
        nodes.remove(node.getId());
    }

    FileableCmisObject move(String parentId, FileableCmisObject cmisObject){
        if (cmisObject == null)
            throw new RuntimeException("cmisObject not set!");
        if (parentId == null)
            throw new RuntimeException("parentId must be set!");
        if (root == null)
            throw new RuntimeException("move:no Root Node!");
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

    public TreeNode<FileableCmisObject> insert( TreeNode<FileableCmisObject> parent, FileableCmisObject cmisObject, boolean executeRule) {
        return insert(parent, cmisObject, null, null, executeRule);
    }

    public TreeNode<FileableCmisObject> insert(TreeNode<FileableCmisObject> parent, FileableCmisObject cmisObject, ContentStream contentStream, String version, boolean executeRule) {
        if (cmisObject == null)
            throw new RuntimeException("cmisObject must be set!");
        String name = cmisObject.getName();
        TreeNode<FileableCmisObject> newNode;
        if (parent == null && cmisObject.getType() instanceof FolderType) {
            newNode = new TreeNode<>(cmisObject);
            newNode.setPath("/");
            nodes.put(newNode.getId(), newNode);
            root = newNode;
            logger.info(name + " [ID: " + newNode.getId() + "] inserted into repository!");
        } else if (parent == null && cmisObject.getType() instanceof ItemType) {
            newNode = new TreeNode<>(cmisObject);
            nodes.put(newNode.getId(), newNode);
            categoryroot = newNode;
            logger.info(name + " [ID: " + newNode.getId() + "] inserted into repository!");
        } else {
            if (root == null)
                throw new RuntimeException("insert:no Root Node!");
            if (parent != null) {
                newNode = parent.addNode(cmisObject, version);
                nodes.put(newNode.getId().split(";")[0], newNode);
                if (contentStream != null) {
                    ((Document) cmisObject).setContentStream(contentStream, true);
                }
                if (parent.getName().equals("Inbox") && executeRule) {
                    executeScript(newNode);

                }
                logger.info(name + " [ID: " + newNode.getId() + "] [Path: " + newNode.getObj().getPaths().get(0) + "] inserted into repository!");
            } else
                throw new RuntimeException("Parent not found!");
        }

        return newNode;
    }

    public void executeScript(TreeNode<?> newNode) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            RecognizeEndpoints.setDocument(newNode);
            RecognizeEndpoints.setRepository(this);
            RecognizeEndpoints.setScript("/Data Dictionary/Scripts/recognition.js");
            Object rec = engine.eval("load(\"src/main/resources/static/js/recognition.js\");");

            Invocable invocable = (Invocable) engine;
            engine.eval("logger = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints.JSLogger');");
            engine.eval("document = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').document;");
            engine.eval("script = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').script;");
            engine.eval("companyhome = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').companyhome;");
            engine.eval("classification = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').categoryhome;");
            engine.eval("commentService = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').commentService;");
            engine.eval("search = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').searchService;");
            Object result = invocable.invokeMethod(rec, "run");
        } catch (ScriptException e2) {
            logger.error("Script error!", e2);
        } catch (NoSuchMethodException e3)  {
            logger.error("Java Script Function not found", e3);
        }
    }

    TreeNode<FileableCmisObject> findTreeNodeForId(String id) {
        if (id == null)
            throw new RuntimeException("Id must be set!");
        String[] parts = id.split(";");
        if (nodes.containsKey(parts[0]))
            return nodes.get(parts[0]);
        return null;
    }

    public TreeNode<FileableCmisObject> findTreeNodeForPath (String path) {
        if (path == null)
            throw new RuntimeException("Path must be set!");
        if (path.length() > 1 && path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        for (TreeNode<FileableCmisObject> element : nodes.values()) {
            String objectPath = element.getPath();
            if (objectPath != null && path.matches(objectPath))
                return element;
        }
        return null;
    }


    boolean containsPath(String path) {
        if (root == null)
            throw new RuntimeException("containsPath:no Root Node!");
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
            throw new RuntimeException("getByPath:no Root Node!");
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
            throw new RuntimeException("getById:no Root Node!");
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
            throw new RuntimeException("checkout:no Root Node!");
        if (id == null)
            throw new RuntimeException("id must be set!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(id);
        if (node.checkout() == null)
            throw new CmisVersioningException("is PWC");
        return node.getObj();

    }

    FileableCmisObject checkin(String id, String version, String checkinComment) {
        if (root == null)
            throw new RuntimeException("checkin:no Root Node!");
        if (id == null)
            throw new RuntimeException("id must be set!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(id);
        node.checkin(version, checkinComment);
        return node.getObj();
    }

    List<ObjectData> getAllVersions(String id) {
        if (root == null)
            throw new RuntimeException("getAllVersions:no Root Node!");
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
            throw new RuntimeException("getContent:no Root Node!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(id);
        if ( !contents.containsKey(node.getObj().getProperty(PropertyIds.CONTENT_STREAM_ID).getValueAsString()))
            return null;
        return contents.get(node.getObj().getProperty(PropertyIds.CONTENT_STREAM_ID).getValueAsString());
    }

    void createContent(String nodeId, ContentStream content, boolean overwrite) {
        if (nodeId == null)
            throw new RuntimeException("id must be set!");
        if (root == null)
            throw new RuntimeException("createContent:no Root Node!");
        TreeNode<FileableCmisObject> node = findTreeNodeForId(nodeId);
        try {
            if (contentIds.containsKey(node.getId()) && !overwrite)
                throw new CmisContentAlreadyExistsException();
        } catch (NullPointerException e) {
            nodeId = "";
        }
        String uuid = UUID.randomUUID().toString();
        contents.put(uuid, content);
        contentIds.put(node.getId(), uuid);
        node.updateProperty(PropertyIds.CONTENT_STREAM_ID, uuid);
        setTreeNodeContent(node, content);
        logger.info("[ID: " + nodeId + "] set content!");
    }

    void changeContent(String objectId, ContentStream newContent) {
        if (objectId == null)
        throw new RuntimeException("id must be set!");
        if (root == null)
            throw new RuntimeException("changeContent:no Root Node!");
        ContentStreamImpl streamCurrent = (ContentStreamImpl) getContent(objectId);
        streamCurrent.setStream(newContent.getStream());
        setTreeNodeContent(findTreeNodeForId(objectId), streamCurrent);
        logger.info("[ID: " + objectId + "] changed content!");
    }

    private void setTreeNodeContent(TreeNode<FileableCmisObject> node, ContentStream stream) {

        if (stream != null) {
            try {
                if (stream.getMimeType().equals(VerteilungConstants.DOCUMENT_TYPE_PDF)) {
                    PDFConnector con = new PDFConnector();
                    node.setContent(con.pdftoText(stream.getStream()));
                    stream.getStream().reset();
                } else {
                    node.setContent(IOUtils.toString(stream.getStream(), "UTF-8"));
                    stream.getStream().reset();
                }
            } catch (IOException ignored) {
            }
        }
    }




}
