package de.ksul.archiv.controller;

import de.ksul.archiv.*;
import de.ksul.archiv.request.*;
import de.ksul.archiv.response.ContentResponse;
import de.ksul.archiv.response.DataTablesResponse;
import de.ksul.archiv.response.MoveResponse;
import de.ksul.archiv.response.RestResponse;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.util.EmptyItemIterable;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 19.12.13
 * Time: 15:37
 */
@RestController
@CrossOrigin
public class ArchivController {

    public static final String COMPANY_HOME = "companyHome";
    public static final String DATA_DICTIONARY = "dataDictionary";
    public static final String SCRIPT_FOLDER = "scriptFolder";


    private AlfrescoConnector con;

    private static Logger logger = LoggerFactory.getLogger(ArchivController.class.getName());

    // Speicher für Files
    private Collection<FileEntry> entries = new ArrayList<>();

    //Speicher für Titel
    private HashSet<String> titles = new HashSet<>();


    /**
     * prüft, ob schon eine Verbindung zu einem Alfresco Server besteht
     *
     * @return obj               ein Object mit den Feldern success:     true     die Operation war erfolgreich
     * false    ein Fehler ist aufgetreten
     * data         true, wenn Verbindung vorhanden
     */
    @RequestMapping(value = "/isConnected")
    public @ResponseBody
    RestResponse isConnected() {
        RestResponse obj = new RestResponse();
        obj.setSuccess(true);
        obj.setData(con.isConnected());


        return obj;
    }

    /**
     * liefert die vorhandenen Titel
     *
     * @return obj               ein Object mit den Feldern     success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          data              die Titel als String
     */
    @RequestMapping(value = "/getTitles", produces = "application/json")
    public @ResponseBody
    RestResponse getTitles() {
        RestResponse obj = new RestResponse();

        obj.setSuccess(true);
        obj.setData(titles);

        return obj;
    }

    /**
     * liefert die Server Pfade aus den Properties
     * @return obj               ein Object mit den Feldern     success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          data              die Pfade
     */
    @RequestMapping(value = "/getServerPaths", produces = "application/json")
    RestResponse getServerPaths() {

        RestResponse obj = new RestResponse();
        try {
            obj.setSuccess(true);
            HashMap<String, String> paths = new HashMap<>(3);
            paths.put(COMPANY_HOME, con.getCompanyHomeName());
            paths.put(DATA_DICTIONARY, con.getDataDictionaryName());
            paths.put(SCRIPT_FOLDER, con.getScriptFolderName());
            obj.setData(paths);
        } catch (Exception e) {
            obj.setSuccess(false);
        }
        return obj;
    }

    /**
     * Konstruktor
     */
    @Autowired
    public ArchivController(AlfrescoConnector con) {
        super();
        this.con = con;
    }


    /**
     * sucht alle Titel und stellt sie in einer Liste zur Verfügung
     */
    @SuppressWarnings("unused")
    private void collectTitle() {
        try {
            String documentFolderId = con.getNode("/Archiv/Dokumente").getId();
            if (documentFolderId != null && documentFolderId.length() > 0) {
                for (QueryResult queryResult : con.query("SELECT T.cm:title FROM cmis:document AS D JOIN cm:titled AS T ON D.cmis:objectId = T.cmis:objectId WHERE IN_TREE(D, '" + documentFolderId + "')")) {
                    titles.add((String) queryResult.getPropertyById("cm:title").getFirstValue());
                }
                titles.remove("");
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * liefert Informationen zur Connection
     *
     * @return obj               ein Object mit den Feldern     success: true        die Operation war erfolgreich
     * false       ein Fehler ist aufgetreten
     * data     false       keine Connection
     * Map         Die Verbindungsparameter
     */
    @RequestMapping(value = "/getConnection")
    public @ResponseBody
    RestResponse getConnection() {

        RestResponse obj = new RestResponse();
        obj.setSuccess(true);
        if (this.con.isConnected()) {
            Map<String, String> map = new HashMap<>();
            map.put("server", this.con.getServer());
            map.put("binding", this.con.getBinding());
            map.put("user", this.con.getUser());
            map.put("password", this.con.getPassword());
            obj.setData(map);
        } else {
            obj.setData(false);
        }

        return obj;
    }


    /**
     * öffnet ein Dokument in einem neuen Tab
     *
     * @param model das Requestmodel
     * @return obj               ein Object mit den Feldern     success: true     die Operation war erfolgreich
     * false    ein Fehler ist aufgetreten
     * data              der Inhalt als JSON Object
     */
    @RequestMapping(value = "/openDocument", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    ContentResponse openDocument(@RequestBody @Valid final ObjectByIdRequest model) throws Exception {

        ContentResponse obj = new ContentResponse();

        Document document = (Document) con.getNodeById(model.getDocumentId());
        obj.setMimeType(document.getContentStreamMimeType());
        obj.setName(document.getName());
        obj.setData(Base64.encodeBase64String(con.getDocumentContent(document)));
        obj.setSuccess(true);
        return obj;
    }

    /**
     * liefert den Inhalt eines Dokumentes als String
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/getThumbnail", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse getThumbnail(@RequestBody @Valid final ObjectByIdRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        Document document = (Document) con.getNodeById(model.getDocumentId());
        if (document != null ) {
            List<Rendition> renditions = document.getRenditions();
            if (renditions != null) {
                for (Rendition rendition : renditions) {
                    if (rendition.getKind().equalsIgnoreCase("cmis:thumbnail")) {
                        obj.setData(Base64.encodeBase64String(IOUtils.toByteArray(rendition.getContentStream().getStream())));
                        break;
                    }
                }
            }
            obj.setSuccess(true);
        }

        return obj;
    }

    /**
     * liefert die Kommentare zu einem Knoten
     *
     * @param model das Requestmodel
     * @return obj         ein Object mit den Feldern      success: true     die Operation war erfolgreich
     * false    ein Fehler ist aufgetreten
     * data              die Kommentare  in einer Map
     */
    @RequestMapping(value = "/getComments", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse getComments(@RequestBody @Valid final ObjectByIdRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        CmisObject cmisObject = con.getNodeById(model.getDocumentId());
        obj.setSuccess(true);
        obj.setData(con.getComments(cmisObject));

        return obj;
    }

    /**
     * Fügt zu einem Knoten einen neuen Kommentar hinzu
     *
     * @param model das Requestmodel
     * @return obj         ein Object mit den Feldern      success: true     die Operation war erfolgreich
     * false    ein Fehler ist aufgetreten
     * data              der neue Kommentare in einer Map
     */
    @RequestMapping(value = "/addComment", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse addComment(@RequestBody @Valid final CommentRequest model) throws Exception {
        RestResponse obj = new RestResponse();

        CmisObject cmisObject = con.getNodeById(model.getDocumentId());
        obj.setSuccess(true);
        obj.setData(con.addComment(cmisObject, model.getComment()));

        return obj;
    }


        /**
         * liefert die Dokumente eines Alfresco Folders als JSON Objekte
         *
         * @param model das Datatables Request Model
         * @return resp              das Datatables Response Model
         */
        @RequestMapping(value = "/listFolderWithPagination", consumes = "application/json", produces = "application/json")
        public @ResponseBody
    DataTablesResponse listFolderWithPagination(@RequestBody @Valid final DataTablesRequest model) throws Exception {


        ArrayList<Map<String, Object>> list = new ArrayList<>();
        DataTablesResponse resp = new DataTablesResponse();
        long start;
        long time = System.currentTimeMillis();
        ItemIterable<CmisObject> cmisObjects;

        if (model.getLength() == -1) {
            model.setLength(Integer.MAX_VALUE);
            start = 0;
        } else
            start = model.getStart();

        if (model.getFolderId() != null) {

            cmisObjects = con.listFolder(model.getFolderId().equals("-1") ? con.getNode("/Archiv").getId(): model.getFolderId(), model.getOrders(), model.getColumns(), model.getWithFolder()).skipTo(start).getPage(model.getLength());

            OperationContext operationContext = con.getSession().createOperationContext();
            operationContext.setIncludeAcls(false);
            operationContext.setIncludePolicies(false);
            operationContext.setIncludeAllowableActions(false);

            for (CmisObject cmisObject : cmisObjects) {

                list.add(convertObjectToJson(cmisObject, operationContext));
            }

            resp.setRecordsTotal(cmisObjects.getTotalNumItems());
            resp.setDraw(model.getDraw());
            resp.setRecordsFiltered(cmisObjects.getTotalNumItems());
            resp.setMoreItems(cmisObjects.getHasMoreItems());
            resp.setParent(model.getFolderId());
            resp.setData(list);
            resp.setSuccess(true);
        }
        logger.debug("Time for Execution of listFolderWithPagination() " + (System.currentTimeMillis() - time) + " ms");

        return resp;
    }

    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     *
     * @return ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     * false    ein Fehler ist aufgetreten
     * result           der Inhalt des Verzeichnisses als JSON Objekte
     */
    @RequestMapping(value = "/listFolder", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    DataTablesResponse listFolder(@RequestBody @Valid final DataTablesRequest model) throws Exception {
        model.setLength(-1);
        return listFolderWithPagination(model);
    }

    /**
     * liefert eine Liste mit Documenten aus einer CMIS Query
     *
     * @param model das Datatables Request Model
     * @return obj
     */
    @RequestMapping(value = "/findDocumentWithPagination", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    DataTablesResponse findDocumentWithPagination(@RequestBody @Valid final DataTablesRequest model) throws Exception {

        ArrayList<Map<String, Object>> list = new ArrayList<>();
        DataTablesResponse obj = new DataTablesResponse();
        long start;
        ItemIterable<CmisObject> cmisObjects;


        if (model.getLength() == -1) {
            model.setLength(Integer.MAX_VALUE);
            start = 0;
        } else
            start = model.getStart();


        if (model.getCmisQuery() == null || model.getCmisQuery().trim().length() == 0)
            cmisObjects = EmptyItemIterable.instance();
        else
            cmisObjects = con.findDocument(model.getCmisQuery(), model.getOrders(), model.getColumns()).skipTo(start).getPage(model.getLength());

        OperationContext operationContext = con.getSession().createOperationContext();
        operationContext.setIncludeAcls(false);
        operationContext.setIncludePolicies(false);
        operationContext.setIncludeAllowableActions(false);


        for (CmisObject cmisObject : cmisObjects) {
            list.add(convertObjectToJson(cmisObject, operationContext));
        }


        obj.setRecordsTotal(cmisObjects.getTotalNumItems());
        obj.setDraw(model.getDraw());

        obj.setRecordsFiltered(cmisObjects.getTotalNumItems());
        obj.setMoreItems(cmisObjects.getHasMoreItems());

        obj.setSuccess(true);
        obj.setData(list);

        return obj;
    }

    /**
     * findet Documente
     *
     * @param model das Requestmodel
     * @return ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     * false    ein Fehler ist aufgetreten
     * result            Dokument als JSONObject
     */
    @RequestMapping(value = "/findDocument", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    DataTablesResponse findDocument(@RequestBody @Valid final DataTablesRequest model) throws Exception {

        model.setLength(-1);
        return findDocumentWithPagination(model);
    }


    /**
     * liefert eine NodeId
     *
     * @param model das Requestmodel
     * @return obj          ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     * false   ein Fehler ist aufgetreten
     * data    Map mit der Id des Objekts (objectId: vom Alfresco gelieferte Id, objectID: Id die im Browser verwendet werden kann)
     */
    @RequestMapping(value = "/getNodeId", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse getNodeId(@RequestBody @Valid final ObjectByPathRequest model) throws Exception {

        RestResponse obj = new RestResponse();
        CmisObject cmisObject = con.getNode(model.getFilePath());
        if (cmisObject != null) {
            obj.setSuccess(true);
            Map<String, Object> result = new HashMap<>();
            result.put("objectId", cmisObject.getId());
            result.put("objectID", VerteilungHelper.normalizeObjectId(cmisObject.getId()));
            obj.setData(result);
        } else
            obj.setSuccess(false);

        return obj;
    }

    /**
     * liefert einen Knoten
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/getNode", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse getNode(@RequestBody @Valid final ObjectByPathRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        obj.setSuccess(true);
        obj.setData(convertCmisObjectToJSON(con.getNode(model.getFilePath()), true));

        return obj;
    }

    /**
     * sucht ein Objekt nach seiner ObjektId
     *
     * @param model das Requestmodel
     * @return obj          ein Object mit den Feldern     success: true    die Operation war erfolgreich
     * false   ein Fehler ist aufgetreten
     * data    der Knoten als Map
     */
    @RequestMapping(value = "/getNodeById", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse getNodeById(@RequestBody @Valid final ObjectByIdRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        CmisObject cmisObject = con.getNodeById(model.getDocumentId());

        if (cmisObject != null) {
            obj.setData(convertCmisObjectToJSON(cmisObject, true));
            obj.setSuccess(true);
        }
        else {
            obj.setData(null);
            obj.setSuccess(false);
        }

        return obj;
    }

    /**
     * prüft, ob ein Knoten Childknoten hat
     *
     * @param model das Requestmodel
     * @return obj          ein Object mit den Feldern     success: true    die Operation war erfolgreich
     * false   ein Fehler ist aufgetreten
     * data    true oder false
     */
    @RequestMapping(value = "/hasChildFolder", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse hasChildFolder(@RequestBody @Valid final ObjectByIdRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        CmisObject cmisObject = con.getNodeById(model.getDocumentId());

        if (cmisObject != null) {
            obj.setData(con.hasChildFolder(cmisObject));
            obj.setSuccess(true);
        }
        else {
            obj.setData(null);
            obj.setSuccess(false);
        }

        return obj;
    }


    /**
     * führt eine Query durch und liefert die Ergebnisse als JSON Objekte zurück
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/query", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse query(@RequestBody @Valid final QueryRequest model) throws Exception {

        RestResponse obj = new RestResponse();
        obj.setSuccess(true);
        obj.setData(con.query(model.getCmisQuery()));
        
        return obj;
    }

    /**
     * liefert den Inhalt eines Dokumentes als Base64 encodeter String
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/getDocumentContent", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse getDocumentContent(@RequestBody @Valid final ObjectByIdRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        Document document = (Document) con.getNodeById(model.getDocumentId());
        obj.setSuccess(true);
        obj.setData(Base64.encodeBase64String(con.getDocumentContent(document)));

        return obj;
    }

    /**
     * liefert den Inhalt eines PDFDokumentes als String
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/getDocumentContentExtracted", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse getDocumentContentExtracted(@RequestBody @Valid final ObjectByIdRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        Document document = (Document) con.getNodeById(model.getDocumentId());
        obj.setSuccess(true);
        PDFConnector pdfConnector = new PDFConnector();
        InputStream is = new ByteArrayInputStream(con.getDocumentContent(document));
        obj.setData(pdfConnector.pdftoText(is));

        return obj;
    }

    /**
     * lädt ein Dokument hoch
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/uploadDocument", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse uploadDocument(@RequestBody @Valid final UploadRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        String typ = null;
        if (model.getFileName().toLowerCase().endsWith(".pdf"))
            typ = "application/pdf";
        File file = new File(model.getFileName());
        CmisObject cmisObject = con.getNodeById(model.getDocumentId());
        if (cmisObject != null && cmisObject instanceof Folder) {
            String id = con.uploadDocument(((Folder) cmisObject), file, typ, createVersionState(model.getVersionState()));
            //TODO Cache
            obj.setSuccess(true);
            obj.setData(id);
        } else {
            throw new ArchivException("Der verwendete Pfad ist kein Folder!");
        }

        return obj;
    }

    /**
     * löscht ein Dokument
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/deleteDocument", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse deleteDocument(@RequestBody @Valid final ObjectByIdsRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        CmisObject document;
        for (String id : model.getDocumentId()) {
            document = con.getNodeById(id);
            if (document != null && document instanceof Document) {
                if (((Document) document).isVersionSeriesCheckedOut())
                    ((Document) document).cancelCheckOut();
                document.delete(true);
                obj.setSuccess(true);
                obj.setData("");
            } else {
                obj.setSuccess(false);
                obj.setData(document == null ? "Das Dokument ist nicht vorhanden!" : "Das Dokument ist nicht vom Typ Dokument!");
                return obj;
            }
        }

        return obj;
    }

    /**
     * erstellt ein Dokument
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/createDocument", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse createDocument(@RequestBody @Valid final DocumentCreateRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        CmisObject document;
        CmisObject folderObject;

        folderObject = con.getNodeById(model.getDocumentId());
        Map<String, Object> outMap = null;
        if (folderObject != null && folderObject instanceof Folder) {

            if (model.getExtraProperties() != null)
                outMap = buildProperties(model.getExtraProperties());

            document = con.createDocument((Folder) folderObject, model.getFileName(), Base64.decodeBase64(model.getContent()), model.getMimeType(), outMap, createVersionState(model.getVersionState()));
            if (document != null) {
                obj.setSuccess(true);
                obj.setData(convertCmisObjectToJSON(document, true));
            } else {
                obj.setSuccess(false);
                obj.setData("Ein Document mit dem Namen " + model.getFileName() + " konnte nicht erstellt werden!");
            }
        } else {
            obj.setSuccess(false);
            obj.setData(folderObject == null ? "Der angegebene Pfad  ist nicht vorhanden!" : "Der verwendete Pfad ist kein Folder!");
        }

        return obj;
    }


    /**
     * aktualisiert den Inhalt eines Dokumentes
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/updateDocument", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse updateDocument(@RequestBody @Valid final DocumentRequest model) throws Exception {

        RestResponse obj = new RestResponse();


        Map<String, Object> outMap = new HashMap<>();
        CmisObject cmisObject = con.getNodeById(model.getDocumentId());

        if (cmisObject != null && cmisObject instanceof Document) {


            if (model.getExtraProperties() != null) {
                outMap = buildProperties(model.getExtraProperties());
            }

            Document document = con.updateDocument((Document) cmisObject, Base64.decodeBase64(model.getContent()), model.getMimeType(), outMap, createVersionState(model.getVersionState()), model.getVersionComment());
            obj.setSuccess(true);
            obj.setData(convertCmisObjectToJSON(document, true));

        } else {

            obj.setSuccess(false);
            obj.setData(cmisObject == null ? "Ein Document mit der Id " + model.getDocumentId() + " ist nicht vorhanden!" : "Das verwendete Document mit der Id" + model.getDocumentId() + " ist nicht vom Typ Document!");
        }


        return obj;
    }

    /**
     * aktualisiert die Properties eines Objectes
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/updateProperties", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse updateProperties(@RequestBody @Valid final PropertiesRequest model) throws Exception {

        RestResponse obj = new RestResponse();


        Map<String, Object> outMap;
        CmisObject cmisObject = con.getNodeById(model.getDocumentId());
        if (cmisObject != null) {

            OperationContext operationContext = con.getSession().createOperationContext();

            outMap = buildProperties(model.getExtraProperties());

            cmisObject = con.updateProperties(cmisObject, outMap);

            Map<String, Object> props = convertCmisObjectToJSON(cmisObject, true);
            // Versionen suchen
            if (cmisObject instanceof Document) {
                TreeMap<String, Object> versions = new TreeMap<>(Collections.reverseOrder());
                for (Document document : ((Document) cmisObject).getAllVersions(operationContext)) {
                    versions.put(document.getVersionLabel(), convertCmisObjectToJSON(document, false));
                }
                props.put("versions", versions);
            }

            obj.setSuccess(true);
            obj.setData(props);
        } else {
            throw new ArchivException("Ein Document mit der Id " + model.getDocumentId() + " ist nicht vorhanden!");
        }


        return obj;
    }

    /**
     * verschiebt ein Dokument
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/moveNode", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    MoveResponse moveNode(@RequestBody @Valid final MoveRequest model) throws Exception {

        MoveResponse obj = new MoveResponse();

        CmisObject node = con.getNodeById(model.getDocumentId());
        CmisObject oldFolder = con.getNodeById(model.getCurrentLocationId());
        CmisObject newFolder = con.getNodeById(model.getDestinationId());
        if (node != null && node instanceof Document || node instanceof Folder) {
            if (oldFolder != null && oldFolder instanceof Folder) {
                if (newFolder != null && newFolder instanceof Folder) {
                    FileableCmisObject fileableCmisObject = con.moveNode((FileableCmisObject) node, (Folder) oldFolder, (Folder) newFolder);
                    logger.trace("Knoten " + node.getId() + " von " + ((FileableCmisObject) node).getPaths().get(0) + " nach " + fileableCmisObject.getPaths().get(0) + " verschoben!");
                    obj.setSuccess(true);
                    OperationContext operationContext = con.getSession().createOperationContext();
                    operationContext.setIncludeAcls(false);
                    operationContext.setIncludePolicies(false);
                    operationContext.setIncludeAllowableActions(false);
                    obj.setData(convertObjectToJson(fileableCmisObject, operationContext));
                    // Quell und Zielordner zurückgeben
                    obj.setSource(convertCmisObjectToJSON(oldFolder, true));
                    obj.setTarget(convertCmisObjectToJSON(newFolder, true));
                } else {
                    obj.setSuccess(false);
                    obj.setData("Der verwendete Pfad mit der Id" + model.getDestinationId() + " ist kein Folder!");

                }
            } else {
                obj.setSuccess(false);
                obj.setData(oldFolder == null ? "Der Pfad mit der Id " + model.getCurrentLocationId() + "  ist nicht vorhanden!" : "Der verwendete Pfad mit der Id" + model.getCurrentLocationId() + " ist kein Folder!");

            }
        } else {
            obj.setSuccess(false);
            obj.setData(node == null ? "Ein Document mit der Id " + model.getDocumentId() + " ist nicht vorhanden!" : "Das verwendete Document mit der Id" + model.getDocumentId() + " ist nicht vom Typ Document oder Folder!");
        }

        return obj;
    }

    /**
     * erstellt einen Ordner
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/createFolder", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse createFolder(@RequestBody @Valid final PropertiesRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        Folder folder;
        CmisObject target;
        Map<String, Object> outMap;

        outMap = buildProperties(model.getExtraProperties());

        target = con.getNodeById(model.getDocumentId());
        if (target != null && target instanceof Folder) {
            folder = con.createFolder((Folder) target, outMap);
            if (folder != null) {
                obj.setSuccess(true);
                Map<String, Object> o = convertCmisObjectToJSON(folder, true);
                // neu definierter Folder kann keine Children haben
                o.put("hasChildren", false);
                o.put("hasChildFolder", false);
                o.put("hasChildDocuments", false);
                obj.setData(o);
            } else {
                obj.setSuccess(false);
                obj.setData("Ein Folder konnte nicht angelegt werden!");
            }
        } else {
            obj.setSuccess(false);
            obj.setData(target == null ? "Der angebene Pfad mit der Id " + model.getDocumentId() + " ist nicht vorhanden!" : "Der verwendete Pfad " + target + " ist kein Folder!");
        }

        return obj;
    }

    /**
     * löscht einen Folder
     * löscht einen Pfad
     *
     * @param model das Array mit den Requestmodels
     * @return obj
     */
    @RequestMapping(value = "/deleteFolder", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse deleteFolder(@RequestBody @Valid final ObjectByIdsRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        CmisObject folder;
        List<String> list = new ArrayList<>();
        for (String id : model.getDocumentId()) {
            folder = con.getNodeById(id);
            if (folder != null && folder instanceof Folder) {

                list.addAll(((Folder) folder).deleteTree(true, UnfileObject.DELETE, true));

            } else {
                obj.setSuccess(false);
                obj.setData(folder == null ? "Der angegebene Pfad ist nicht vorhanden!" : "Der verwendete Pfad ist kein Folder!");
                return obj;
            }
        }

        obj.setSuccess(true);
        obj.setData(list);
        
        return obj;
    }


    /**
     * prüft, ob eine Url verfügbar ist
     *
     * @param model das Requestmodel
     * @return ein Object mit den Feldern     success: true     die Operation war erfolgreich
     *                   false    ein Fehler ist aufgetreten
     * data              true, wenn die URL verfügbar ist
     */
    @RequestMapping(value = "/isURLAvailable", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse isURLAvailable(@RequestBody @Valid final ConnectionRequest model) throws Exception {

        RestResponse obj = new RestResponse();
        URL url;
        int erg;

        logger.trace("check availibility of: " + model.getServer());
        url = new URL(model.getServer());
        HttpURLConnection httpUrlConn;
        httpUrlConn = (HttpURLConnection) url.openConnection();
        httpUrlConn.setRequestMethod("HEAD");
        // Set timeouts in milliseconds
        httpUrlConn.setConnectTimeout(model.getTimeout());
        httpUrlConn.setReadTimeout(model.getTimeout());
        try {
            erg = httpUrlConn.getResponseCode();
        } catch (UnknownHostException u) {
           erg = HttpURLConnection.HTTP_NOT_FOUND;
        }
        if (erg == HttpURLConnection.HTTP_OK || erg == HttpURLConnection.HTTP_UNAUTHORIZED) {
            logger.trace("URL is available: " + model.getServer());
            obj.setSuccess(true);
            obj.setData(true);
        } else {
            logger.trace("URL is not available: " + model.getServer());
            obj.setSuccess(false);
            obj.setData(erg);
        }

        return obj;
    }

    /**
     * prüft, ob eine Regel für einen Knoten vorhanden ist
     *
     * @param model     das RequestModel
     * @return ein Object mit den Feldern     success: true     die Operation war erfolgreich
     * false             ein Fehler ist aufgetreten
     * data              true, wenn die Regel verfügbar ist
     * @throws Exception
     */
    @RequestMapping(value = "/hasRule", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse hasRule(@RequestBody @Valid final RuleAvailableRequest model) throws Exception {

        RestResponse obj = new RestResponse();
        boolean result = con.hasRule(model.getTitle(), model.getFolderId());

        obj.setSuccess(true);
        obj.setData(result);

        return obj;
    }

    /**
     * legt eine neue Regel für einen Folder an.
     *
     * @param model     das RequestModel
     * @return obj          ein Object mit den Feldern     success: true    die Operation war erfolgreich
     *                      false   ein Fehler ist aufgetreten
     * @throws Exception
     */
    @RequestMapping(value = "/createRule", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse createRule(@RequestBody @Valid final RuleCreateRequest model) throws Exception {

        RestResponse obj = new RestResponse();
        con.createRule(model.getFolderId(), model.getScriptId(), model.getTitle(), model.getDescription());

        obj.setSuccess(true);
        obj.setData("");

        return obj;
    }

    /**
     * extrahiert eine PDF Datei und trägt den Inhalt in den internen Speicher ein.
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/extractPDFToInternalStorage", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse extractPDFToInternalStorage(@RequestBody @Valid final ExtractRequest model)  {

        RestResponse obj = new RestResponse();

        byte[] bytes = Base64.decodeBase64(model.getContent());
        InputStream bais = new ByteArrayInputStream(bytes);
        PDFConnector con = new PDFConnector();
        if (entries != null) {
            entries.add(new FileEntry(model.getFileName(), bytes, con.pdftoText(bais)));
        }
        obj.setSuccess(true);
        obj.setData(1);

        return obj;
    }

    /**
     * extrahiert eine PDF Datei.
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/extractPDFFile", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse extractPDFFile(@RequestBody @Valid final ObjectByPathRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        byte[] bytes = readFile(model.getFilePath());
        PDFConnector con = new PDFConnector();
        obj.setSuccess(true);
        obj.setData(con.pdftoText(new ByteArrayInputStream(bytes)));

        return obj;
    }


    /**
     * extrahiert den Inhalt einer PDF Datei.
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/extractPDFContent", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse extractPDFContent(@RequestBody @Valid final ContentRequest model) {

        RestResponse obj = new RestResponse();

        byte[] bytes = Base64.decodeBase64(model.getContent());
        PDFConnector con = new PDFConnector();
        obj.setSuccess(true);
        obj.setData(con.pdftoText(new ByteArrayInputStream(bytes)));

        return obj;
    }

    /**
     * extrahiert ein ZIP File und gibt den Inhalt als Base64 encodete Strings zurück
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/extractZIP", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse extractZIP(@RequestBody @Valid final ContentRequest model) throws Exception {

        RestResponse obj = new RestResponse();
        ArrayList<String> arrayList = new ArrayList<>();
        ZipInputStream zipin = null;
        try {

            final byte[] bytes = Base64.decodeBase64(model.getContent());
            InputStream bais = new ByteArrayInputStream(bytes);
            zipin = new ZipInputStream(bais);
            int size;
            while ((zipin.getNextEntry()) != null) {
                byte[] buffer = new byte[2048];
                ByteArrayOutputStream bys = new ByteArrayOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(bys, buffer.length);
                while ((size = zipin.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }
                bos.flush();
                bos.close();
                arrayList.add(Base64.encodeBase64String(bys.toByteArray()));
                bys.toByteArray();
            }
            if (arrayList.isEmpty()) {
                obj.setSuccess(false);
                obj.setData("Keine Files im ZIP File gefunden!");
            } else {
                obj.setSuccess(true);
                obj.setData(arrayList);
            }

        } finally {
            try {
                if (zipin != null)
                    zipin.close();
            } catch (Exception e) {
                obj.setSuccess(false);
                obj.setError(e);
            }
        }
        return obj;
    }

    /**
     * entpackt ein ZIP File in den internen Speicher
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/extractZIPToInternalStorage", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse extractZIPToInternalStorage(@RequestBody @Valid final ContentRequest model) throws Exception {
        RestResponse obj = new RestResponse();
        ZipInputStream zipin = null;
        try {

            final byte[] bytes = Base64.decodeBase64(model.getContent());
            InputStream bais = new ByteArrayInputStream(bytes);
            zipin = new ZipInputStream(bais);
            ZipEntry entry;
            int size;
            int counter = 0;
            entries.clear();

            while ((entry = zipin.getNextEntry()) != null) {
                byte[] buffer = new byte[2048];
                ByteArrayOutputStream bys = new ByteArrayOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(bys, buffer.length);
                while ((size = zipin.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }
                bos.flush();
                bos.close();
                entries.add(new FileEntry(entry.getName(), bys.toByteArray()));
                counter++;
            }
            if (counter == 0) {
                obj.setSuccess(false);
                obj.setData("Keine Files im ZIP File gefunden!");
            } else {
                obj.setSuccess(true);
                obj.setData(counter);
            }

        } finally {
            try {
                if (zipin != null)
                    zipin.close();
            } catch (Exception e) {
                obj.setSuccess(false);
                obj.setError(e);
            }
        }
        return obj;
    }

    /**
     * entpackt ein ZIP File und stellt die Inhalte und die extrahierten PDF Inhalte in den internen Speicher
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/extractZIPAndExtractPDFToInternalStorage", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse extractZIPAndExtractPDFToInternalStorage(@RequestBody @Valid final ContentRequest model) throws Exception {

        RestResponse obj;
        String extractedData;
        int counter = 0;

        obj = extractZIPToInternalStorage(model);
        if (obj.isSuccess()) {
            PDFConnector con = new PDFConnector();
            for (FileEntry entry : entries) {

                if (entry.getName().toLowerCase().endsWith(".pdf")) {
                    InputStream bais = new ByteArrayInputStream(entry.getData());
                    extractedData = con.pdftoText(bais);
                } else
                    extractedData = new String(entry.getData());
                entry.setExtractedData(extractedData);
                counter++;
            }
            obj.setData(counter);
        }

        return obj;
    }

    /**
     * liefert den Inhalt aus dem internen Speicher
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/getDataFromInternalStorage", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse getDataFromInternalStorage(@RequestBody @Valid final FileNameRequest model) {

        RestResponse obj = new RestResponse();
        Map<String, Object> data = new HashMap<>();
        boolean found = false;

        if (entries.isEmpty()) {
            obj.setSuccess(false);
            obj.setData("keine Einträge vorhanden");
        } else {
            for (FileEntry entry : entries) {
                if (entry.getName().equals(model.getFileName())) {
                    obj.setSuccess(true);
                    Map<String, Object> jEntry = new HashMap<>();
                    jEntry.put("name", entry.getName());
                    if (entry.getData().length > 0) {
                        jEntry.put("data", Base64.encodeBase64String(entry.getData()));
                        if (entry.getExtractedData() != null && !entry.getExtractedData().isEmpty())
                            jEntry.put("extractedData", entry.getExtractedData());
                        data.put(entry.getName(), jEntry);
                        obj.setData(data);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                obj.setSuccess(false);
                obj.setData("keine Einträge vorhanden");
                logger.info("keine Einträge vorhanden");
            }
        }


        return obj;
    }


    /**
     * liefert den kompletten Inhalt aus dem internen Speicher
     *
     * @return obj
     */
    @RequestMapping(value = "/getCompleteDataFromInternalStorage", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse getCompleteDataFromInternalStorage() {

        RestResponse obj = new RestResponse();
        Map<String, Object> data = new HashMap<>();

        if (entries.isEmpty()) {
            obj.setSuccess(false);
            obj.setData("keine Einträge vorhanden");
        } else {
            for (FileEntry entry : entries) {
                Map<String, Object> jEntry = new HashMap<>();
                jEntry.put("name", entry.getName());
                if (entry.getData().length > 0) {
                    jEntry.put("data", Base64.encodeBase64String(entry.getData()));
                    if (entry.getExtractedData() != null && !entry.getExtractedData().isEmpty())
                        jEntry.put("extractedData", entry.getExtractedData());
                    data.put(entry.getName(), jEntry);
                }
            }
            obj.setSuccess(true);
            obj.setData(data);
        }

        return obj;
    }


    /**
     * löscht den internen Speicher
     *
     * @return obj
     */
    @RequestMapping(value = "/clearInternalStorage", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse clearInternalStorage() {

        RestResponse obj = new RestResponse();

        entries.clear();
        obj.setSuccess(true);
        obj.setData("");

        return obj;
    }

    /**
     * öffnet eine Datei und liest den Inhalt
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/openFile", consumes = "application/json", produces = "application/json")
    public @ResponseBody
    RestResponse openFile(@RequestBody @Valid final ObjectByPathRequest model) throws Exception {

        RestResponse obj = new RestResponse();

        byte[] buffer = readFile(model.getFilePath());
        obj.setSuccess(true);
        obj.setData(Base64.encodeBase64String(buffer));

        return obj;
    }


    /**
     * liest eine Datei
     *
     * @param filePath der Pfad zur Datei
     * @return der Inhalt als Byte Array
     * @throws URISyntaxException wird geworfen wenn Path eine ungültige URI enthält
     * @throws IOException wird geworfen wenn das File nich geöffnet werden kann
     */

    private byte[] readFile(String filePath) throws URISyntaxException, IOException {

        File sourceFile = new File(new URI(filePath.replace("\\", "/")));
        InputStream inp = new FileInputStream(sourceFile);
        byte[] buffer = new byte[(int) sourceFile.length()];
        //noinspection ResultOfMethodCallIgnored
        inp.read(buffer);
        inp.close();
        return buffer;
    }

    /**
     * nur für Testzwecke
     *
     * @return die FileEntries
     */
    public Collection<FileEntry> getEntries() {
        return entries;
    }

    /**
     * bereitet die Properties auf
     *
     * @param extraCMSProperties der String mit den Properties im JSON Format
     */
    private Map<String, Object> buildProperties(Map<String, Object> extraCMSProperties) {

        Iterator nameItr = extraCMSProperties.keySet().iterator();
        Map<String, Object> outMap = new HashMap<>();
        List<String> liste = new ArrayList<>();
        while (nameItr.hasNext()) {
            String name = (String) nameItr.next();
            // Aspekte
            if (name.toUpperCase().startsWith("P:")) {
                liste.add(name);

            }
            // Type Definition
            if (name.toUpperCase().startsWith("D:")) {
                outMap.put(PropertyIds.OBJECT_TYPE_ID, name);
            }
            for (Object o : ((Map) extraCMSProperties.get(name)).keySet()) {
                String innerName = (String) o;
                outMap.put(innerName, ((Map) extraCMSProperties.get(name)).get(innerName));
            }
        }
        if (liste.size() > 0)
            outMap.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, liste);
        return outMap;
    }

    /**
     * baute den Versionstate aus dem String auf
     *
     * @param versionState der VersionsStatus ( none, major, minor, checkedout) als String
     * @return das VersionsState Object
     */
    private VersioningState createVersionState(String versionState) throws ArchivException {

        if (versionState == null || versionState.length() == 0)
            versionState = "none";
        if (!versionState.equals("none") && !versionState.equals("major") && !versionState.equals("minor") && !versionState.equals("checkedout"))
            throw new ArchivException("ungültiger VersionsStatus");
        VersioningState vs = VersioningState.fromValue(versionState);
        if (vs == null)
            vs = VersioningState.NONE;
        return vs;
    }

    /**
     * konvertiert die Properties eines Documentes in ein JSON Objekt
     *
     * @param cmisObject      das Objekt
     * @param searchParents   legt fest, ob die Parents gesucht werden sollen
     * @return props          das Object als JSON Objekt
     */
    private Map<String, Object> convertCmisObjectToJSON(CmisObject cmisObject,
                                                        boolean searchParents) {

        Map<String, Object> props = convProperties(cmisObject.getProperties());
        // Parents suchen
        if (searchParents) {
            List<Folder> parents = ((FileableCmisObject) cmisObject).getParents();
            if (parents != null && parents.size() > 0) {
                Map<String, Object> obj = new HashMap<>();
                int i = 0;
                for (Folder folder : parents) {
                    obj.put(Integer.toString(i++), convProperties(folder.getProperties()));
                }
                props.put("parents", obj);
                props.put("parentId", parents.get(0).getId());
            }
        }
        return props;
    }

    /**
     * konvertiert die Properties in einen passenden Typ
     * @param properties  die Properties
     * @return  eine Hasmap mit den Namen des Propitiers und dem Property selber im passenden Typ
     */
    private Map<String, Object> convProperties(List<Property<?>> properties) {
        Map<String, Object> props = new HashMap<>();
        for (Property prop : properties) {
            // falls Datumswert dann konvertieren
            if (prop.getDefinition().getPropertyType().equals(PropertyType.DATETIME) && prop.getValue() != null) {
                props.put(prop.getLocalName(), ((GregorianCalendar) prop.getValue()).getTime().getTime());
            } else if (prop.getDefinition().getPropertyType().equals(PropertyType.DECIMAL) && prop.getValue() != null) {
                props.put(prop.getLocalName(), prop.getValue());
            } else if (prop.getDefinition().getPropertyType().equals(PropertyType.BOOLEAN) && prop.getValue() != null) {
                props.put(prop.getLocalName(), prop.getValue());
            } else if (prop.getDefinition().getPropertyType().equals(PropertyType.INTEGER) && prop.getValue() != null) {
                props.put(prop.getLocalName(), prop.getValue());
            } else if (prop.getLocalName().equals("objectId")) {
                String id = prop.getValueAsString();
                props.put(prop.getLocalName(), id);
                id = VerteilungHelper.normalizeObjectId(id);
                // die modifizierte ObjectId diese ist auch eindeutig und kann im DOM benutzt werden.
                props.put("objectID", id);
                // Row Id für Datatables
                props.put("DT_RowId", id);
            } else {
                if (prop.getValueAsString() != null)
                    props.put(prop.getLocalName(), prop.getValueAsString());
            }
        }
        return props;
    }


    /**
     * konvertiert ein Objekt in ein JSON Objekt
     *
     * @param cmisObject das zu konvertierende CMIS Objekt
     * @param operationContext  ein Operationskontext
     * @return JSONObject             das gefüllte JSON Objekt
     */
    private Map<String, Object> convertObjectToJson(CmisObject cmisObject,
                                                    OperationContext operationContext) {

        Map<String, Object> o = convertCmisObjectToJSON(cmisObject, true);
        // prüfen, ob Children vorhanden sind
        if (cmisObject instanceof Folder) {
            boolean hasChildFolder = con.hasChildFolder(cmisObject);
            o.put("hasChildFolder", hasChildFolder);
            // JsTree
            o.put("id", cmisObject.getId());
            o.put("children", hasChildFolder);
            o.put("text", cmisObject.getName());
            //o.put("data", o);
            o.put("a_attr", "'class': 'drop'");
            o.put("icon", "");
            Properties state = new Properties();
            state.put("opened", "false");
            state.put("disabled", "false");
            state.put("selected", "false");
            o.put("state", state);
        } else {
            if (((Document) cmisObject).isVersionable()) {
                TreeMap<String, Object> versions = new TreeMap<>(Collections.reverseOrder());
                for (Document document : ((Document) cmisObject).getAllVersions(operationContext)) {
                    versions.put(document.getVersionLabel(), convertCmisObjectToJSON(document, false));
                }
                o.put("versions", versions);
            }
        }

        return o;
    }

    /**
     * öffnet ein PDF im Browser
     *
     * @param model das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/openPDF")
    public
    @ResponseBody
    ContentResponse openPDF(@RequestBody @Valid final FileNameRequest model) {

        ContentResponse obj = new ContentResponse();

        for (FileEntry entry : getEntries()) {
            if (entry.getName().equalsIgnoreCase(model.getFileName())) {
                obj.setData(Base64.encodeBase64String(entry.getData()));
                obj.setName(entry.getName());
                obj.setMimeType("application/pdf");
                break;
            }
        }
        obj.setSuccess(true);

        return obj;
    }


    /**
     * liefert den Alfresco Server
     *
     * @return den Alfresco Server
     */
    public String getServer() {
        return con.getServer();
    }
}