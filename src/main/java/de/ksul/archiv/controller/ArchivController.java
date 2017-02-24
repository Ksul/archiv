package de.ksul.archiv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ksul.archiv.*;
import de.ksul.archiv.request.ConnectionRequest;
import de.ksul.archiv.request.DataTablesRequest;
import de.ksul.archiv.request.RestRequest;
import de.ksul.archiv.response.ContentResponse;
import de.ksul.archiv.response.DataTablesResponse;
import de.ksul.archiv.response.MoveResponse;
import de.ksul.archiv.response.RestResponse;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.util.EmptyItemIterable;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
public class ArchivController {

    private AlfrescoConnector con;

    private static Logger logger = LoggerFactory.getLogger(ArchivController.class.getName());

    // Speicher für Files
    private Collection<FileEntry> entries = new ArrayList<>();

    //Speicher für Titel
    private HashSet<String> titles = new HashSet<>();




    /**
     * prüft, ob schon eine Verbindung zu einem Alfresco Server besteht
     * @return obj               ein Object mit den Feldern success:     true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                      data         true, wenn Verbindung vorhanden
     */
    @RequestMapping(value = "/isConnected")
    public @ResponseBody RestResponse isConnected() {
        RestResponse obj = new RestResponse();
        try {
            obj.setSuccess(true);
            obj.setData(con.isConnected());

        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * liefert die vorhandenen Titel
     * @return obj               ein Object mit den Feldern     success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          data              die Titel als String
     */
    @RequestMapping(value = "/getTitles")
    public @ResponseBody RestResponse getTitles() {
        RestResponse obj = new RestResponse();
        try {
            obj.setSuccess(true);
            obj.setData(titles);
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
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
     * über diese Methode können die Alfresco Parameter nachträglich gesetzt werden.
     * @param server         der Name des Alfresco Servers
     * @param binding        der Binding teil der URL
     * @param username       der verwendete Username
     * @param password       das Passwort
     */
/*    public void setParameter(String server,
                             String binding,
                             String username,
                             String password) {
        this.con = new AlfrescoConnector(username, password, server, binding);
        if (this.con.isConnected()) {
            collectTitle();
            logger.info("Connected to Alfresco Server!");
        } else {
            logger.info("Could not establish Connection to Alfresco Server!");
        }
    }*/

    /**
     * sucht alle Titel und stellt sie in einer Liste zur Verfügung
     */
    private void collectTitle()  {
        try {
            String documentFolderId = con.getNode("/Archiv/Dokumente").getId();
            if (documentFolderId != null && documentFolderId.length() > 0) {
                for (List<PropertyData<?>> propData : con.query("SELECT T.cm:title FROM cmis:document AS D JOIN cm:titled AS T ON D.cmis:objectId = T.cmis:objectId WHERE IN_TREE(D, '" + documentFolderId + "')")) {
                    titles.add((String) propData.get(0).getValues().get(0));
                }
                titles.remove("");
            }
        } catch (Exception e) {}
    }

    /**
     * liefert Informationen zur Connection
     * @return obj               ein Object mit den Feldern     success: true        die Operation war erfolgreich
     *                                                                   false       ein Fehler ist aufgetreten
     *                                                          data     false       keine Connection
     *                                                                   Map         Die Verbindungsparameter
     */
    @RequestMapping(value = "/getConnection")
    public @ResponseBody RestResponse getConnection() {

        RestResponse obj = new RestResponse();
        try {
            obj.setSuccess(true);
            if (this.con.isConnected()) {
                Map<String, String> map = new HashMap();
                map.put("server", this.con.getServer());
                map.put("binding", this.con.getBinding());
                map.put("user", this.con.getUser());
                map.put("password", this.con.getPassword());
                obj.setData(map);
            } else {
                obj.setData(false);
            }
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }


    /**
     * öffnet ein Dokument in einem neuen Tab
     * @param model        das Requestmodel
     * @return obj               ein Object mit den Feldern     success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          data              der Inhalt als JSON Object
     */
    @RequestMapping(value = "/openDocument")
    public @ResponseBody ContentResponse openDocument(@RequestBody @Valid final RestRequest model) {

        ContentResponse obj = new ContentResponse();
        try {
            Document document = (Document) con.getNodeById(model.getDocumentId());
            obj.setMimeType(document.getContentStreamMimeType());
            obj.setName(document.getName());
            obj.setData(Base64.encodeBase64String(con.getDocumentContent(document)));
            obj.setSuccess(true);
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * liefert den Inhalt eines Dokumentes als String
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/getThumbnail")
    public @ResponseBody RestResponse getThumbnail(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            Document document = (Document) con.getNodeById(model.getDocumentId());
            for (Rendition rendition : document.getRenditions()){
                if (rendition.getKind().equalsIgnoreCase("cmis:thumbnail")) {
                    obj.setData(Base64.encodeBase64String(IOUtils.toByteArray(rendition.getContentStream().getStream())));
                    break;
                }
            }
            obj.setSuccess(true);
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * liefert die Kommentare zu einem Knoten
     * @param model        das Requestmodel
     * @return obj         ein Object mit den Feldern      success: true     die Operation war erfolgreich
     *                                                              false    ein Fehler ist aufgetreten
     *                                                     data              die Kommentare  in einer Map
     */
    @RequestMapping(value = "/getComments")
    public @ResponseBody RestResponse getComments(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            CmisObject cmisObject = con.getNodeById(model.getDocumentId());
            obj.setSuccess(true);
            obj.setData(con.getComments(cmisObject));
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * Fügt zu einem Knoten einen neuen Kommentar hinzu
     * @param model        das Requestmodel
     * @return obj         ein Object mit den Feldern      success: true     die Operation war erfolgreich
     *                                                              false    ein Fehler ist aufgetreten
     *                                                     data              der neue Kommentare in einer Map
     */
    @RequestMapping(value = "/addComment")
    public @ResponseBody RestResponse addComment(@RequestBody @Valid final RestRequest model){
        RestResponse obj = new RestResponse();
        try {
            CmisObject cmisObject = con.getNodeById(model.getDocumentId());
            obj.setSuccess(true);
            obj.setData(con.addComment(cmisObject, model.getComment()));
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }


    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     *
     * @param  model             das Datatables Request Model
     * @return resp              das Datatables Response Model
     */
    @RequestMapping(value = "/listFolderWithPagination")
    public @ResponseBody DataTablesResponse listFolderWithPagination(@RequestBody @Valid final DataTablesRequest model) {

        ArrayList<Map<String, Object>> list = new ArrayList();
        DataTablesResponse resp = new DataTablesResponse();
        long itemsToSkip;
        ItemIterable<CmisObject> cmisObjects;
        try {
            if (model.getLength() == -1) {
                model.setLength(Integer.MAX_VALUE);
                itemsToSkip = 0;
            } else
                itemsToSkip = model.getStart();

            // state = new JSONObject("{state: {opened: false, disabled: false, selected: false}}");
            // das Root Object übergeben?
            if (model.getFilePath().equals("-1"))
                model.setFilePath(con.getNode("/Archiv").getId());

            cmisObjects = con.listFolder(model.getFilePath(), model.getOrders(), model.getColumns(), model.getWithFolder()).skipTo(itemsToSkip).getPage(model.getLength());

            for (CmisObject cmisObject : cmisObjects) {
                list.add(convertObjectToJson(model.getFilePath(), cmisObject));
            }


            resp.setRecordsTotal(cmisObjects.getTotalNumItems());
            resp.setDraw(model.getDraw());
            resp.setRecordsFiltered(cmisObjects.getTotalNumItems());
            resp.setMoreItems(cmisObjects.getHasMoreItems());
            resp.setParent(model.getFilePath());
            resp.setData(list);
            resp.setSuccess(true);

        } catch (Throwable t) {
            resp.setError(t);
            resp.setSuccess(false);
        }
        return resp;
    }

    /** liefert die Dokumente eines Alfresco Folders als JSON Objekte
     * @return                   ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                           result           der Inhalt des Verzeichnisses als JSON Objekte
     */
    @RequestMapping(value = "/listFolder")
    public @ResponseBody DataTablesResponse listFolder(@RequestBody @Valid final DataTablesRequest model)  {
        model.setLength(-1);
        return listFolderWithPagination(model);
    }


    /**
     * liefert eine NodeId
     *
     * @param model        das Requestmodel
     * @return obj          ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                              false   ein Fehler ist aufgetreten
     *                                                     data     die Id des Knotens
     */
    @RequestMapping(value = "/getNodeId")
    public @ResponseBody RestResponse getNodeId(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            obj.setSuccess(true);
            obj.setData(con.getNode(model.getFilePath()).getId());
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * liefert einen Knoten
     *
     * @param model         das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/getNode")
    public @ResponseBody RestResponse getNode(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            obj.setSuccess(true);
            obj.setData(convertCmisObjectToJSON(con.getNode(model.getFilePath())));
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * sucht ein Objekt nach seiner ObjektId
     * @param model         das Requestmodel
     * @return obj          ein Object mit den Feldern     success: true    die Operation war erfolgreich
     *                                                              false   ein Fehler ist aufgetreten
     *                                                     data     der Knoten als Map
     */
    @RequestMapping(value = "/getNodeById")
    public @ResponseBody RestResponse getNodeById(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            obj.setSuccess(true);
            obj.setData(convertCmisObjectToJSON(con.getNodeById(model.getDocumentId())));
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * liefert eine Liste mit Documenten aus einer CMIS Query
     *
     * @param  model             das Datatables Request Model
     * @return obj
     */
    @RequestMapping(value = "/findDocumentWithPagination")
    public @ResponseBody DataTablesResponse findDocumentWithPagination( @RequestBody @Valid final DataTablesRequest model) {

        ArrayList<Map<String, Object>> list = new ArrayList();
        DataTablesResponse obj = new DataTablesResponse();
        long itemsToSkip;
        ItemIterable<CmisObject> cmisObjects;
        try {

            if (model.getLength() == -1) {
                model.setLength(Integer.MAX_VALUE);
                itemsToSkip = 0;
            } else
                itemsToSkip = model.getStart();


            if (model.getCmisQuery() == null || model.getCmisQuery().trim().length() == 0)
                cmisObjects = EmptyItemIterable.instance();
            else
                cmisObjects = con.findDocument(model.getCmisQuery(), model.getOrders(), model.getColumns()).skipTo(itemsToSkip).getPage(model.getLength());

            for (CmisObject cmisObject : cmisObjects) {
                list.add(convertCmisObjectToJSON(cmisObject));
            }


            obj.setRecordsTotal(cmisObjects.getTotalNumItems());
            obj.setDraw(model.getDraw());

            obj.setRecordsFiltered(cmisObjects.getTotalNumItems());
            obj.setMoreItems(cmisObjects.getHasMoreItems());

            obj.setSuccess(true);
            obj.setData(list);
        } catch (Throwable t) {
            obj.setError(t);
            obj.setSuccess(false);
        }
        return obj;
    }

    /**
     * findet Documente
     * @param model        das Requestmodel
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            Dokument als JSONObject
     */
    @RequestMapping(value = "/findDocument")
    public @ResponseBody DataTablesResponse findDocument(@RequestBody @Valid final DataTablesRequest model) throws ArchivException {

        model.setLength(-1);
        return findDocumentWithPagination(model);
    }

    /**
     * führt eine Query durch und liefert die Ergebnisse als JSON Objekte zurück
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/query")
    public @ResponseBody RestResponse query(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        List<HashMap<String, Object>> list = new ArrayList<>();
        try {
            for (List<PropertyData<?>> propData : con.query(model.getCmisQuery())) {

                for (PropertyData prop : propData) {
                    HashMap<String, Object> o = new HashMap<>();
                    Object propObj = prop.getFirstValue();
                    if (propObj != null) {
                        o.put(prop.getLocalName(), propObj);
                        list.add(o);
                    }

                }
                obj.setSuccess(true);
                obj.setData(list);
            }

        } catch (Throwable t) {
            obj.setError(t);
            obj.setSuccess(false);
        }
        return obj;
    }

    /**
     * liefert den Inhalt eines Dokumentes als String
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/getDocumentContent")
    public @ResponseBody RestResponse getDocumentContent(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            Document document = (Document) con.getNodeById(model.getDocumentId());
            obj.setSuccess(true);
            obj.setData(con.getDocumentContent(document));
            if (model.getExtract()) {
                PDFConnector con = new PDFConnector();
                InputStream is = new ByteArrayInputStream((byte[]) obj.getData());
                obj.setData(con.pdftoText(is));
            } else
                obj.setData(new String((byte[]) obj.getData(), "UTF-8"));

        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * lädt ein Dokument hoch
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/uploadDocument")
    public @ResponseBody RestResponse uploadDocument(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
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
                obj.setSuccess(false);
                obj.setData("Der verwendete Pfad ist kein Folder!");
            }
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * löscht ein Dokument
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/deleteDocument")
    public @ResponseBody RestResponse deleteDocument(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            CmisObject document;
            document = con.getNodeById(model.getDocumentId());
            if (document != null && document instanceof Document) {
                if (((Document) document).isVersionSeriesCheckedOut())
                    ((Document) document).cancelCheckOut();
                document.delete(true);
                obj.setSuccess(true);
                obj.setData("");
            } else {
                obj.setSuccess(false);
                obj.setData(document == null ? "Das Document ist nicht vorhanden!" : "Das Document ist nicht vom Typ Document!");
            }
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * erstellt ein Dokument
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/createDocument")
    public @ResponseBody RestResponse createDocument(@RequestBody @Valid final RestRequest model) {

        //TODO Content als String oder als Stream?
        RestResponse obj = new RestResponse();
        try {
            CmisObject document;
            CmisObject folderObject;

            folderObject = con.getNodeById(model.getDocumentId());
            Map<String, Object> outMap = null;
            if (folderObject != null && folderObject instanceof Folder) {

                if (model.getExtraProperties() != null && model.getExtraProperties().length() > 0)
                  outMap = buildProperties(model.getExtraProperties());

                document = con.createDocument((Folder) folderObject, model.getFileName(), Base64.decodeBase64(model.getContent()), model.getMimeType(), outMap, createVersionState(model.getVersionState()));
                if (document != null) {
                    obj.setSuccess(true);
                    obj.setData(convertCmisObjectToJSON(document));
                } else {
                    obj.setSuccess(false);
                    obj.setData("Ein Document mit dem Namen " + model.getFileName() + " ist nicht vorhanden!");
                }
            } else {
                obj.setSuccess(false);
                obj.setData(folderObject == null ? "Der angegebene Pfad  ist nicht vorhanden!" : "Der verwendete Pfad ist kein Folder!");
            }
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }


    /**
     * aktualisiert den Inhalt eines Dokumentes
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/updateDocument")
    public @ResponseBody RestResponse updateDocument(@RequestBody @Valid final RestRequest model) {

        //TODO Content als String oder als Stream?
        RestResponse obj = new RestResponse();

        try {

            Map<String, Object> outMap = new HashMap<>();
            List<String> asp = null;
            CmisObject cmisObject = con.getNodeById(model.getDocumentId());

            if (cmisObject != null && cmisObject instanceof Document) {



                if (model.getExtraProperties() != null && model.getExtraProperties().length() > 0) {
                    outMap = buildProperties(model.getExtraProperties());
                }

                Document document = con.updateDocument((Document) cmisObject, Base64.decodeBase64(model.getContent()), model.getMimeType(), outMap, createVersionState(model.getVersionState()), model.getVersionComment());
                obj.setSuccess(true);
                obj.setData(convertCmisObjectToJSON(document));

            } else {

                obj.setSuccess(false);
                obj.setData(cmisObject == null ? "Ein Document mit der Id " + model.getDocumentId() + " ist nicht vorhanden!" : "Das verwendete Document mit der Id" + model.getDocumentId() + " ist nicht vom Typ Document!");
            }

        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }

        return obj;
    }

    /**
     * aktualisiert die Properties eines Objectes
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/updateProperties")
    public @ResponseBody RestResponse updateProperties(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();

        try {

            Map<String, Object> outMap = null;
            CmisObject cmisObject = con.getNodeById(model.getDocumentId());
            if (cmisObject != null) {

                if (model.getExtraProperties() != null && model.getExtraProperties().length() > 0)
                    outMap = buildProperties(model.getExtraProperties());

                else {
                    obj.setSuccess(false);
                    obj.setData("keine Properties vorhanden!");
                }

                cmisObject = con.updateProperties(cmisObject, outMap);

                obj.setSuccess(true);
                obj.setData(convertCmisObjectToJSON(cmisObject));
            } else {
                obj.setSuccess(false);
                obj.setData("Ein Document mit der Id " + model.getDocumentId() + " ist nicht vorhanden!");
            }

        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }

        return obj;
    }

    /**
     * verschiebt ein Dokument
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/moveNode")
    public @ResponseBody MoveResponse moveNode(@RequestBody @Valid final RestRequest model) {

        MoveResponse obj = new MoveResponse();
        try {
            CmisObject node = con.getNodeById(model.getDocumentId());
            CmisObject oldFolder = con.getNodeById(model.getCurrentLocationId());
            CmisObject newFolder = con.getNodeById(model.getDestinationId());
            if (node != null && node instanceof Document || node instanceof Folder) {
                if (oldFolder != null && oldFolder instanceof Folder) {
                    if (newFolder != null && newFolder instanceof Folder) {
                        FileableCmisObject fileableCmisObject = con.moveNode((FileableCmisObject) node, (Folder) oldFolder, (Folder) newFolder);
                        logger.trace("Knoten " + node.getId() + " von " + ((FileableCmisObject) node).getPaths().get(0) + " nach " + fileableCmisObject.getPaths().get(0) + " verschoben!");
                        obj.setSuccess(true);
                        obj.setData(convertObjectToJson(model.getDestinationId(), fileableCmisObject));
                        // Quell und Zielordner zurückgeben
                        obj.setSource(convertCmisObjectToJSON(oldFolder));
                        obj.setTarget(convertCmisObjectToJSON(newFolder));
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
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * erstellt einen Ordner
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/createFolder")
    public @ResponseBody RestResponse createFolder(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            Folder folder;
            CmisObject target;
            Map<String, Object> outMap = null;

            if (model.getExtraProperties() != null && model.getExtraProperties().length() > 0)
                outMap = buildProperties(model.getExtraProperties());
            else {
                obj.setSuccess(false);
                obj.setData("keine Properties vorhanden!");
            }

            target = con.getNodeById(model.getDocumentId());
            if (target != null && target instanceof Folder) {
                folder = con.createFolder((Folder) target, outMap);
                if (folder != null ) {
                    obj.setSuccess(true);
                    Map<String, Object> o = convertCmisObjectToJSON(folder);
                    // neu definierter Folder kann keine Children haben
                    o.put("hasChildren", false);
                    o.put("hasChildFolder", false);
                    o.put("hasChildDocuments", false);
                    obj.setData(o);
                } else {
                    obj.setSuccess(false);
                    obj.setData("Ein Folder konnte nicht angelegt werden!" );
                }
            } else {
                obj.setSuccess(false);
                obj.setData(target == null ? "Der angebene Pfad mit der Id " + model.getDocumentId() + " ist nicht vorhanden!" : "Der verwendete Pfad " + target + " ist kein Folder!");
            }
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * löscht einen Folder
     * löscht einen Pfad
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/deleteFolder")
    public @ResponseBody RestResponse deleteFolder(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            CmisObject folder;
            folder = con.getNodeById(model.getDocumentId());
            if (folder != null && folder instanceof Folder) {

                List<String> list = ((Folder) folder).deleteTree(true, UnfileObject.DELETE, true);
                obj.setSuccess(true);
                obj.setData(list);
            } else {
                obj.setSuccess(false);
                obj.setData(folder == null ? "Der  angegebene Pfad ist nicht vorhanden!" : "Der verwendete Pfad st kein Folder!");
            }
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }


    /**
     * prüft, ob eine Url verfügbar ist
     * @param model        das Requestmodel
     * @return             ein Object mit den Feldern     success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    data              true, wenn die URL verfügbar ist
     */
    @RequestMapping(value = "/isURLAvailable")
    public @ResponseBody RestResponse isURLAvailable(@RequestBody @Valid final ConnectionRequest model) {

        RestResponse obj = new RestResponse();
        URL url;
        try {
            logger.trace("check availibility of: " + model.getServer());
            url = new URL(model.getServer());
            HttpURLConnection httpUrlConn;
            httpUrlConn = (HttpURLConnection) url.openConnection();
            httpUrlConn.setRequestMethod("HEAD");
            // Set timeouts in milliseconds
            httpUrlConn.setConnectTimeout(model.getTimeout());
            httpUrlConn.setReadTimeout(model.getTimeout());

            int erg = httpUrlConn.getResponseCode();
            if (erg == HttpURLConnection.HTTP_OK || erg == HttpURLConnection.HTTP_UNAUTHORIZED) {
                logger.trace("URL is available: " + model.getServer());
                obj.setSuccess(true);
                obj.setData(true);
            } else {
                logger.trace("URL is not available: " + model.getServer());
                obj.setSuccess(false);
                obj.setData(httpUrlConn.getResponseMessage());
            }
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * extrahiert eine PDF Datei und trägt den Inhalt in den internen Speicher ein.
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/extractPDFToInternalStorage")
    public @ResponseBody RestResponse extractPDFToInternalStorage(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            byte[] bytes = Base64.decodeBase64(model.getContent());
            InputStream bais = new ByteArrayInputStream(bytes);
            PDFConnector con = new PDFConnector();
            if (entries != null) {
                entries.add(new FileEntry(model.getFileName(), bytes, con.pdftoText(bais)));
            }
            obj.setSuccess(true);
            obj.setData(1);
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * extrahiert eine PDF Datei.
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/extractPDFFile")
    public @ResponseBody RestResponse extractPDFFile(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            byte[] bytes = readFile(model.getFilePath());
            PDFConnector con = new PDFConnector();
            obj.setSuccess(true);
            obj.setData(con.pdftoText(new ByteArrayInputStream(bytes)));
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }


    /**
     * extrahiert den Inhalt einer PDF Datei.
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/extractPDFContent")
    public @ResponseBody RestResponse extractPDFContent(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            byte[] bytes = Base64.decodeBase64(model.getContent());
            PDFConnector con = new PDFConnector();
            obj.setSuccess(true);
            obj.setData(con.pdftoText(new ByteArrayInputStream(bytes)));
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * extrahiert ein ZIP File und gibt den Inhalt als Base64 encodete Strings zurück
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/extractZIP")
    public @ResponseBody RestResponse extractZIP(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        ArrayList<String> arrayList = new ArrayList();
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
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        } finally {
            try {
                if (zipin != null)
                    zipin.close();
            } catch (Throwable t) {
                obj.setSuccess(false);
                obj.setError(t);
            }
        }
        return obj;
    }

    /**
     * entpackt ein ZIP File in den internen Speicher
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/extractZIPToInternalStorage")
    public @ResponseBody RestResponse extractZIPToInternalStorage(@RequestBody @Valid final RestRequest model) {
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
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        } finally {
            try {
                if (zipin != null)
                    zipin.close();
            } catch (Throwable t) {
                obj.setSuccess(false);
                obj.setError(t);
            }
        }
        return obj;
    }

    /**
     * entpackt ein ZIP File und stellt die Inhalte und die extrahierten PDF Inhalte in den internen Speicher
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/extractZIPAndExtractPDFToInternalStorage")
    public @ResponseBody RestResponse extractZIPAndExtractPDFToInternalStorage(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = null;
        String extractedData;
        int counter = 0;
        try {
            obj = extractZIPToInternalStorage(model);
            if (obj.isSuccess()) {
                PDFConnector con = new PDFConnector();
                for (FileEntry entry : entries) {

                    if (entry.getName().toLowerCase().endsWith(".pdf")) {
                        InputStream bais = new ByteArrayInputStream(entry.getData());
                        extractedData = con.pdftoText(bais);
                    }
                    else
                        extractedData = new String(entry.getData());
                    entry.setExtractedData(extractedData);
                    counter++;
                }
                obj.setData(counter);
            }
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * liefert den Inhalt aus dem internen Speicher
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/getDataFromInternalStorage")
    public @ResponseBody RestResponse getDataFromInternalStorage(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        Map<String, Object> data = new HashMap<>();
        boolean found = false;
        try {
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

        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }


    /**
     * liefert den kompletten Inhalt aus dem internen Speicher
     * @return obj
     */
    @RequestMapping(value = "/getCompleteDataFromInternalStorage")
    public @ResponseBody  RestResponse getCompleteDataFromInternalStorage() {

        RestResponse obj = new RestResponse();
        Map<String, Object> data = new HashMap<>();
        try {
            if (entries.isEmpty()) {
                obj.setSuccess(false);
                obj.setData("keine Einträge vorhanden");
            } else {
                for (FileEntry entry: entries) {
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
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }


    /**
     * löscht den internen Speicher
     * @return obj
     */
    @RequestMapping(value = "/clearInternalStorage")
    public @ResponseBody RestResponse clearInternalStorage() {

        RestResponse obj = new RestResponse();
        try {
            entries.clear();
            obj.setSuccess(true);
            obj.setData("");
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }

    /**
     * öfnnet eine Datei und liest den Inhalt
     * @param model        das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/openFile")
    public @ResponseBody RestResponse openFile(@RequestBody @Valid final RestRequest model) {

        RestResponse obj = new RestResponse();
        try {
            byte[] buffer = readFile(model.getFilePath());
            obj.setSuccess(true);
            obj.setData(Base64.encodeBase64String(buffer));
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }


    /**
     * liest eine Datei
     * @param filePath                  der Pfad zur Datei
     * @return                          der Inhalt als Byte Array
     * @throws URISyntaxException
     * @throws IOException
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
     * @return    die FileEntries
     */
    public Collection<FileEntry> getEntries() {
        return entries;
    }

    /**
     * bereitet die Properties auf
     * @param extraCMSProperties    der String mit den Properties im JSON Format
     * @throws IOException
     */
    protected Map<String, Object> buildProperties(String extraCMSProperties) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        logger.trace("buildProperties from " + extraCMSProperties);
        Map<String, Object> props = mapper.readValue(extraCMSProperties, Map.class);
        Iterator nameItr = props.keySet().iterator();
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
            Iterator innerItr = ((Map) props.get(name)).keySet().iterator();
            while (innerItr.hasNext()) {
                String innerName = (String) innerItr.next();
                outMap.put(innerName, ((Map) props.get(name)).get(innerName));
            }
        }
        if (liste.size() > 0)
            outMap.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, liste);
        return outMap;
    }

    /**
     * baute den Versionstate aus dem String auf
     * @param  versionState  der VersionsStatus ( none, major, minor, checkedout) als String
     * @return               das VersionsState Object
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
     * @param  cmisObject    das Objekt
     * @return obj1          das Object als JSON Objekt
     */
    private Map<String, Object> convertCmisObjectToJSON(CmisObject cmisObject)  {

        List<Property<?>> properties = cmisObject.getProperties();
        Map<String, Object> props = convPropertiesToJSON(properties);

        // Parents suchen
        List<Folder> parents = ((FileableCmisObject) cmisObject).getParents();
        if (parents != null && parents.size() > 0) {
            Map<String, Object>  obj = new HashMap<>();
            int i = 0;
            for (Folder folder:parents) {
                obj.put(Integer.toString(i++), convPropertiesToJSON(folder.getProperties()));
            }
            props.put("parents", obj);
        }
        return props;
    }

    /**
     *
     * @param properties
     * @return
     */
    private Map<String, Object> convPropertiesToJSON(List<Property<?>> properties) {
        Map<String, Object> props = new HashMap<>();
        for (Property prop : properties) {
            // falls Datumswert dann konvertieren
            if (prop.getDefinition().getPropertyType().equals(PropertyType.DATETIME) && prop.getValue() != null) {
                props.put(prop.getLocalName(), ((GregorianCalendar) prop.getValue()).getTime().getTime());
            } else if (prop.getDefinition().getPropertyType().equals(PropertyType.DECIMAL) && prop.getValue() != null) {
                props.put(prop.getLocalName(), prop.getValue());
            } else if (prop.getDefinition().getPropertyType().equals(PropertyType.BOOLEAN) && prop.getValue() != null) {
                props.put(prop.getLocalName(),  prop.getValue());
            } else if (prop.getDefinition().getPropertyType().equals(PropertyType.INTEGER) && prop.getValue() != null) {
                props.put(prop.getLocalName(),  prop.getValue());
            }else if (prop.getLocalName().equals("objectId")) {
                String id = prop.getValueAsString();
                props.put(prop.getLocalName(), id);
                id = VerteilungHelper.getRealId(id);
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
     * @param  parentId               die Id des Parent Objektes
     * @param  cmisObject             das zu konvertierende CMIS Objekt
     * @return JSONObject             das gefüllte JSON Objekt
     */
    private Map<String, Object> convertObjectToJson(String parentId,
                                           CmisObject cmisObject) {

        Map<String, Object> o = convertCmisObjectToJSON(cmisObject);
        // prüfen, ob Children vorhanden sind
        if (cmisObject instanceof Folder) {
            OperationContext operationContext = con.getSession().createOperationContext();
            operationContext.setIncludeAcls(false);
            operationContext.setIncludePolicies(false);
            operationContext.setIncludeAllowableActions(false);
            ItemIterable<CmisObject> children = ((Folder) cmisObject).getChildren(operationContext);
            o.put("hasChildren", children.getTotalNumItems() > 0);
            boolean hasChildFolder = false;
            boolean hasChildDocuments = false;
            for (CmisObject childObject : children) {
                if (childObject instanceof Folder) {
                    hasChildFolder = true;
                }
                if (childObject instanceof Document) {
                    hasChildDocuments = true;
                }
                if (hasChildDocuments && hasChildFolder)
                    break;
            }

            o.put("hasChildFolder", hasChildFolder);
            o.put("hasChildDocuments", hasChildDocuments);
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
            o.put("state",  state);
        }
        o.put("parentId", parentId);

        return o;
    }

    /**
     * öffnet ein PDF im Browser
     * @param model      das Requestmodel
     * @return obj
     */
    @RequestMapping(value = "/openPDF")
    public
    @ResponseBody ContentResponse openPDF(@RequestBody @Valid final RestRequest model) {

        ContentResponse obj = new ContentResponse();
        try {
            for (FileEntry entry : getEntries()) {
                if (entry.getName().equalsIgnoreCase(model.getFileName())) {
                    obj.setData(Base64.encodeBase64String(entry.getData()));
                    obj.setName(entry.getName());
                    obj.setMimeType("application/pdf");
                    break;
                }
            }
            obj.setSuccess(true);
        } catch (Throwable t) {
            obj.setSuccess(false);
            obj.setError(t);
        }
        return obj;
    }


    /**
     * liefert den Alfresco Server
     * @return   den Alfresco Server
     */
    public String getServer() {
        return con.getServer();
    }
}