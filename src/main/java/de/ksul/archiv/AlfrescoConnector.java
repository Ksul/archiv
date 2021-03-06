package de.ksul.archiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ksul.archiv.model.*;
import de.ksul.archiv.model.comments.Comment;
import de.ksul.archiv.model.comments.CommentPaging;
import de.ksul.archiv.model.rules.Rule;
import de.ksul.archiv.model.rules.Action;
import de.ksul.archiv.model.rules.Condition;
import de.ksul.archiv.request.RuleCreateRequest;
import de.ksul.archiv.response.RuleResponse;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.util.AbstractPageFetcher;
import org.apache.chemistry.opencmis.client.runtime.util.CollectionIterable;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.definitions.PropertyBooleanDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDateTimeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDecimalDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumBaseObjectTypeIds;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.IntStream;


/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 09.01.14
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class AlfrescoConnector {

    @Autowired
    RestTemplate restTemplate;

    private static final String NODES_URL = "service/api/node/workspace/SpacesStore/";
    private static final String NEW_API_NODES_URL = "api/-default-/public/alfresco/versions/1/nodes/";
    private static final String LOGIN_URL = "tickets";
    private static Logger logger = LoggerFactory.getLogger(AlfrescoConnector.class.getName());
    private String user;
    private String password;
    private String binding;
    private String server;
    private String companyHomeName;
    private String dataDictionaryName;
    private String scriptFolderName;
    private ObjectMapper mapper = new ObjectMapper();
    private Session session;
    private OperationContext operationContext;
    private static enum RequestType {
        POST("POST"),
        GET("GET");

        private final String name;
        RequestType(String name) {
            this.name = name;
        }
        String getName() {
            return this.name;
        }
    }
//    Dieser SQL sucht zwar alles in einem Rutsch, funktioniert aber nur, wenn die Documente alle notwendigen Aspekte besitzen
//
//    private final static String DOCUMENT_SQL_STRING = "select d.*, o.*, c.*, i.* from my:archivContent as d " +
//            "join cm:titled as o on d.cmis:objectId = o.cmis:objectId " +
//            "join my:amountable as c on d.cmis:objectId = c.cmis:objectId " +
//            "join my:idable as i on d.cmis:objectId = i.cmis:objectId " +
//            "WHERE IN_FOLDER(d, ?) ";
    // die SQL's müssen immer nur die Id mit liefern, weil im Fetcher aus dieser das Object aufgebaut wird  
    private final static String DOCUMENT_SQL_STRING = "select d.cmis:objectId from my:archivContent as d " +
            "join cm:titled as o on d.cmis:objectId = o.cmis:objectId " +
            "WHERE IN_FOLDER(d, ?) ";
    private final static String DOCUMENT_ALL_SQL_STRING = "select d.cmis:objectId  from cmis:document as d " +
            "join cm:titled as o on d.cmis:objectId = o.cmis:objectId " +
            "WHERE IN_FOLDER(o, ?) ";
    private final static String FOLDER_SQL_STRING = "select d.cmis:objectId from cmis:folder as d " +
            "join cm:titled as o on d.cmis:objectId = o.cmis:objectId " +
            "WHERE IN_FOLDER(d, ?) AND d.cmis:objectTypeId<>'F:cm:systemfolder'";

    private class IdHelper implements ObjectId {
        String id;

        public IdHelper(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }

    /**
     * Konstruktor
     * @param session  die CMIS Session
     * @param server   die Url zum Server
     * @param binding  die Url zum Binding
     * @param user     der User vom Server
     * @param password das Password auf dem Server
     */
    public AlfrescoConnector(Session session, String server, String binding, String user, String password, String companyHomeName, String dataDictionaryName, String scriptFolderName) {
        this.session = session;
        this.server = server;
        this.binding = binding;
        this.user = user;
        this.password = password;
        this.companyHomeName = companyHomeName;
        this.dataDictionaryName = dataDictionaryName;
        this.scriptFolderName = scriptFolderName;
    }

    /**
    * liefert den User
    * @return der User
    */
    public String getUser() {
        return user;
    }

    /**
     * liefert das Password
     * @return das Password
     */
    public String getPassword() {
        return password;
    }

    /**
     * liefert das Binding
      * @return das Binding
     */
    public String getBinding() {
        return binding;
    }

    /**
     * liefert den Server
     * @return der Server
     */
    public String getServer() {
        return server;
    }

    public String getCompanyHomeName() {
        return companyHomeName;
    }

    public String getDataDictionaryName() {
        return dataDictionaryName;
    }

    public String getScriptFolderName() {
        return scriptFolderName;
    }


    /**
     * liefert die CMIS Session
     * @return  die CMIS Session
     */
    public Session getSession() {
          return this.session;
    }

    /**
     * gibt Auskunft ob eine Alfresco Verbindung besteht
     * @return
     */
    public boolean isConnected(){
        return this.session != null;
    }



    /**
     * Hilfsmethode, um den Content eines Dokumentes als String zu liefern
     *
     * @param stream                           der Stream
     * @return                                 der Inhalt des Dokumentes als String
     * @throws IOException
     */
    private static String getContentAsString(ContentStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (Reader reader = new InputStreamReader(stream.getStream(), "UTF-8")) {
            final char[] buffer = new char[4 * 1024];
            int b;
            while (true) {
                b = reader.read(buffer, 0, buffer.length);
                if (b > 0) {
                    sb.append(buffer, 0, b);
                } else if (b == -1) {
                    break;
                }
            }
        }

        return sb.toString();
    }

    /**
     * prüft ob ein Folder Child Folder besitzt
     * @param cmisObject
     * @return boolean
     */
    public boolean hasChildFolder(CmisObject cmisObject ) {
        if (cmisObject instanceof Document)
            throw new IllegalArgumentException("Object must be of Folder Type");
        StringBuilder query = new StringBuilder(FOLDER_SQL_STRING);
        QueryStatement stmt = session.createQueryStatement(query.toString());
        stmt.setString(1, cmisObject.getId());
        DiscoveryService discoveryService = this.session.getBinding().getDiscoveryService();
        return discoveryService.query(session.getRepositoryInfo().getId(), stmt.toString(),
                false, operationContext.isIncludeAllowableActions(), operationContext.getIncludeRelationships(),
                operationContext.getRenditionFilterString(), BigInteger.valueOf(1),
                BigInteger.valueOf(0), null).getNumItems().longValue() > 0;
    }


    /**
     * listet den Inhalt eines Folders
     * @param folderId              die Id des Folders
     * @param order                 Die Spalte nach der sortiuert werden soll
     * @param modus                 was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @return                      die gefundenen Children
     */

    public ItemIterable<CmisObject> listFolder(String folderId,
                                               List<Order> order,
                                               List<Column> columns,
                                               int modus) throws ArchivException {

        ItemIterable<CmisObject> result = null;
        long time;

        OperationContext operationContext = getOperationContext();
        switch (modus) {
            case VerteilungConstants.LIST_MODUS_ALL: {  // Dokumente und Folder
                StringBuilder query = new StringBuilder();
                buildOrder(order, columns, query, modus);
                operationContext.setOrderBy(query.toString());
                CmisObject object = this.session.getObject(this.session.createObjectId(folderId));
                Folder folder = (Folder) object;
                time = System.currentTimeMillis();
                result = folder.getChildren(operationContext);
                logger.debug("Time for Execution of folder.getChildren() " + (System.currentTimeMillis() - time) + " ms");
                break;
            }
            case VerteilungConstants.LIST_MODUS_DOCUMENTS:{  // nur Dokumente
                StringBuilder query = new StringBuilder(DOCUMENT_SQL_STRING);

                query.append(" ORDER BY ");
                buildOrder(order, columns, query, modus);
                QueryStatement stmt = session.createQueryStatement(query.toString());
                stmt.setString(1, folderId);
                result = getCmisObjects(stmt, operationContext);
                break;
            }
            case VerteilungConstants.LIST_MODUS_DOCUMENTS_ALL:{  // alle Dokumente (nicht nur die aus dem Archiv)
                StringBuilder query = new StringBuilder(DOCUMENT_ALL_SQL_STRING);

                query.append(" ORDER BY ");
                buildOrder(order, columns, query, modus);
                QueryStatement stmt = session.createQueryStatement(query.toString());
                stmt.setString(1, folderId);
                result = getCmisObjects(stmt, operationContext);
                break;
            }
            case VerteilungConstants.LIST_MODUS_FOLDER: { // nur Folder
                StringBuilder query = new StringBuilder(FOLDER_SQL_STRING);

                query.append(" ORDER BY ");
                buildOrder(order, columns, query, modus);
                QueryStatement stmt = session.createQueryStatement(query.toString());
                stmt.setString(1, folderId);
                result = getCmisObjects(stmt, operationContext);

                break;
            }
        }

        return result;
    }


    /**
     * baut die Orderbedingung auf
     * @param order         die einzelnen Spalten nach denen sortiert werden soll
     * @param columns       die zu den Spalten gehörenden Feldnamen
     * @param select        der Select als String
     * @param modus         der Modus (1 == Dokumente 2 == Folder)
     * @return
     */
    private void buildOrder(List<Order> order, List<Column> columns, StringBuilder select, int modus) {

        String alias = null;
        String[] parts = select.toString().split(" ");
        if (Arrays.stream(parts).anyMatch("AS"::equalsIgnoreCase) && Arrays.stream(parts).anyMatch("JOIN"::equalsIgnoreCase)) {
            OptionalInt indexOpt = IntStream.range(0, parts.length)
                    .filter(i -> parts[i].equalsIgnoreCase("AS"))
                    .findFirst();
            alias = parts[indexOpt.getAsInt() + 1];
        }
        if (order == null) {
            order = new ArrayList<>();
        }
        if (columns == null) {
            columns = new ArrayList<>();
        }
        if (order.isEmpty()) {
            if (modus == 1) {
                addOrder("my:documentDate", order, columns, modus, alias);
                addOrder("cmis:creationDate", order, columns, modus, alias);
            } else {
                addOrder("cmis:name", order, columns, modus, alias);
            }
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Order o : order) {
            if (!first)
                sb.append(", ");
            String columnName = columns.get(o.getColumn()).getName();
            if (alias != null && !columnName.substring(1, 2).equalsIgnoreCase("."))
                columnName = alias + "." + columnName;
            sb.append(columnName);
            sb.append(" ");
            sb.append(o.getDir());
            first = false;
        }

        select.append(sb.toString());
        return;
    }

    private void addOrder(String field, List<Order> order, List<Column> columns, int modus, String alias) {

        if (columns.stream().anyMatch(column -> column.getName().contains(field))) {

            if (alias != null && !columns.stream().filter(column -> column.getName().contains(field)).findFirst().get().getName().substring(1, 2).equalsIgnoreCase(".")) {
                columns.stream().filter(column -> column.getName().contains(field)).findFirst().get().setName(alias + "." + field);
            }

        } else {
            columns.add(new Column(null, (alias != null ? alias + "." : "") + field, false, false, null, false));
        }
        OptionalInt indexOpt = IntStream.range(0, columns.size())
                .filter(i -> columns.get(i).getName().contains(field))
                .findFirst();
        order.add(new Order(indexOpt.getAsInt(), "DESC"));
        return;
    }

    /**
     * führt die Query aus
     * @param stmt                      die Such Query
     * @param operationContext          der Operation Context
     * @return                          eine Liste mit Cmis Objekten
     */
    public ItemIterable<CmisObject> getCmisObjects(QueryStatement stmt,
                                                   final OperationContext operationContext) {
        ItemIterable<CmisObject> result;
        long time;
        final DiscoveryService discoveryService = this.session.getBinding().getDiscoveryService();
        final ObjectFactory of = this.session.getObjectFactory();
        time = System.currentTimeMillis();
        final ObjectList resultList = discoveryService.query(session.getRepositoryInfo().getId(), stmt.toString(),
                false, operationContext.isIncludeAllowableActions(), operationContext.getIncludeRelationships(),
                operationContext.getRenditionFilterString(), BigInteger.valueOf(Integer.MAX_VALUE),
                BigInteger.valueOf(0), null);
        logger.debug("Time for Execution of discoveryService.query() " + (System.currentTimeMillis() - time) + " ms");
        final long totalNumItems = resultList.getNumItems().longValue();

        result = new CollectionIterable<CmisObject>(new AbstractPageFetcher<CmisObject>(operationContext.getMaxItemsPerPage()) {

            @Override
            protected Page<CmisObject> fetchPage(long skipCount) {
                // fetch the data
                /*ObjectList resultList = discoveryService.query(session.getRepositoryInfo().getId(), stmt.toString(),
                        false, operationContext.isIncludeAllowableActions(), operationContext.getIncludeRelationships(),
                        operationContext.getRenditionFilterString(), BigInteger.valueOf(this.maxNumItems),
                        BigInteger.valueOf(skipCount), null);*/

                // convert query results
                long i = 0;
                long k = 0;
                List<CmisObject> page = new ArrayList<CmisObject>();
                if (resultList.getObjects() != null) {

                    for (ObjectData objectData : resultList.getObjects()) {
                        if (i >= skipCount) {
                            if (objectData == null) {
                                continue;
                            }

                            page.add(session.getObject(session.createObjectId(objectData.getId()), operationContext));

                            //of.convertObject(objectData, operationContext)
                            k++;
                            if (k >= maxNumItems)
                                break;
                        }
                        i++;
                    }
                }

                return new Page<CmisObject>(page, totalNumItems,
                        resultList.hasMoreItems());
            }
        });
        return result;
    }


    /**
     * liefert einen Knotens
     * @param path      der Pfad zum Knoten
     * @return          der Knoten als CMISObject
     */
    public CmisObject getNode(String path) throws ArchivException {
        try {
            OperationContext operationContext = getOperationContext();
            CmisObject cmisObject = this.session.getObjectByPath(path, operationContext);
            logger.trace("getNode with " + path + " found " + cmisObject.getId());
            return cmisObject;
        } catch (CmisObjectNotFoundException e) {
            return null;
        }
    }

    /**
     * sucht ein Objekt nach seiner ObjektId
     * @param  nodeId                die Id des Objektes
     * @return das CmisObject
     */
    public CmisObject getNodeById(String nodeId) {
        try {
            OperationContext operationContext = getOperationContext();
            CmisObject cmisObject = this.session.getObject(this.session.createObjectId(nodeId), operationContext);
            logger.trace("getNodeById with " + nodeId + " found " + cmisObject.getId());
            return cmisObject;
        } catch (CmisObjectNotFoundException e) {
            return null;
        }
    }

    /**
     * sucht ein Objekt nach seiner ObjektId und einem Versionslabel.
     * @param nodeId         die Id des Objektes
     * @param versionLabel   das Versionslabel
     * @return
     */
    public CmisObject getNodeById(String nodeId, String versionLabel) {
        if (nodeId.contains(";"))
            nodeId = nodeId.substring(0, nodeId.lastIndexOf(";") - 1);
        nodeId = nodeId + ";" + versionLabel;
        return getNodeById(nodeId);
    }
    
    /**
     * sucht Dokumente
     * @param queryString           die Abfragequery
     * @param order                 Die Spalte nach der sortiuert werden soll
     * @param columns               die Sortierreihenfolge: ASC oder DESC
     * @return                      die gefundenen Dokumente
     * //TODO Das hier unterstüzt keine Aliase ala SELECT * from cmis:document AS D!!!
     */
    public ItemIterable<CmisObject>     findDocument(String queryString,
                                                 List<Order> order,
                                                 List<Column> columns) throws ArchivException {

        OperationContext operationContext = getOperationContext();

        StringBuilder query = new StringBuilder(queryString);
        query.append(" ORDER BY ");
        buildOrder(order, columns, query,0);
        QueryStatement stmt = session.createQueryStatement(query.toString());

        return getCmisObjects(stmt, operationContext);

    }

    /**
     * führt eine Query durch
     * @param query   der Select als String
     * @return        eine Liste mit den jeweiligen Properties
     * @throws ArchivException
     */
    public List<QueryResult> query(String query) throws ArchivException {

        List<QueryResult> erg = new ArrayList<>();
        ItemIterable<QueryResult> results =  this.session.query(query, false);
        for (Iterator<QueryResult> iterator = results.iterator(); iterator.hasNext(); ) {
            erg.add(iterator.next());
        }
        return erg;
    }

    /**
     * liefert den Inhalt eines Dokumentes
     * @param document              das Dokument
     * @return                      der Inhalt als Bytearray
     * @throws IOException
     */
    public byte[] getDocumentContent(Document document)  throws IOException{
        byte fileBytes[] = null;
        InputStream inputStream = document.getContentStream().getStream();
        inputStream.reset();
        fileBytes = IOUtils.toByteArray(inputStream);
        return fileBytes;
    }

    /**
     * liefert den Inhalt eines Dokumentes als String
     * @param document              das Dokument
     * @return                      der Inhalt als String
     * @throws IOException
     */
    public String getDocumentContentAsString(Document document)  throws IOException{
        return getContentAsString(document.getContentStream());
    }

    /**
     * lädt ein Document hoch
     * @param folder                Der Folder, in den das Dokument geladen werden soll
     * @param file                  Die Datei, die hochgeladen werden soll
     * @param typ                   Der Typ der Datei
     * @param versioningState       der Versionsstatus @see VersioningState
     * @return                      die Id des neuen Documentes als String
     * @throws IOException
     */
    public String uploadDocument(Folder folder,
                                 File file,
                                 String typ,
                                 VersioningState versioningState) throws IOException, ArchivException {

        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        byte[] bytes = new byte[(int) file.length()];
        dis.readFully(bytes);

        Map<String, String> newDocProps = new HashMap<String, String>();
        newDocProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        newDocProps.put(PropertyIds.NAME, file.getName());

        List<Ace> addAces = new LinkedList<>();
        List<Ace> removeAces = new LinkedList<>();
        List<Policy> policies = new LinkedList<>();
        ContentStream contentStream = new ContentStreamImpl(file.getAbsolutePath(), null, "application/pdf",
                new ByteArrayInputStream(bytes));
        Document doc = folder.createDocument(newDocProps, contentStream,
                versioningState, policies, removeAces, addAces, this.session.getDefaultContext());
        return doc.getId();
    }

    /**
     * erzeugt ein neues Dokument
     * @param parentFolder              der Folder, in dem das Dokument angelegt werden soll
     * @param documentName              der Name des neuen Dokumentes
     * @param documentContent           der Inhalt des Dokumentes
     * @param documentType              der Typ des Dokumentes
     * @param properties                die Properties
     * @param versioningState           der Versionsstatus @see VersioningState
     * @return newDocument              das neue Dokument
     */
    public Document createDocument(Folder parentFolder,
                                   String documentName,
                                   byte documentContent[],
                                   String documentType,
                                   Map<String, Object> properties,
                                   VersioningState versioningState) throws ArchivException {

        logger.trace("Create Document: " + documentName + " Type: " + documentType + " in Folder " + parentFolder.getName() + " Version: " + versioningState.value());

        Document newDocument;

        if (documentContent == null)
            throw new IllegalArgumentException("Content darf nicht null sein!");

        if (properties == null)
            properties = new HashMap<>();

        if (!properties.containsKey(PropertyIds.OBJECT_TYPE_ID))
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        else if (!((String) properties.get(PropertyIds.OBJECT_TYPE_ID)).toUpperCase().startsWith("D:") && !((String) properties.get(PropertyIds.OBJECT_TYPE_ID)).toLowerCase().contains("cmis:document"))
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document," + properties.get(PropertyIds.OBJECT_TYPE_ID));

        properties.put(PropertyIds.NAME, documentName);

        InputStream stream = new ByteArrayInputStream(documentContent);
        ContentStream contentStream = new ContentStreamImpl(documentName, BigInteger.valueOf(documentContent.length), documentType, stream);

        newDocument = parentFolder.createDocument(convertProperties(properties), contentStream, versioningState);
        
        return newDocument;
    }



    /**
     * verschiebt ein Dokument
     * @param fileableCmisObject    der zu verschiebende Knoten
     * @param oldFolder             der alte Folder in dem der Knoten liegt
     * @param newFolder             der Folder, in das der Knoten verschoben werden soll
     *
     * @return                     der verschobene Knoten
     */
    public FileableCmisObject moveNode(FileableCmisObject fileableCmisObject,
                               Folder oldFolder,
                               Folder newFolder) {

        FileableCmisObject object = fileableCmisObject.move(oldFolder, newFolder);
        logger.trace("Object " + fileableCmisObject.getId() + " moved from " + oldFolder.getId() + " to folder " + newFolder.getId());
        return object;
    }



    /**
     * erstellt einen Folder
     *
     * @param targetFolder              der Folder, in dem der neue Folder angelegt werden soll.
     * @param properties                Map mit den Properties
     * @return                          der neue Folder
     */
    public Folder createFolder(Folder targetFolder,
                               Map<String, Object> properties ) throws ArchivException {

        logger.trace("createFolder: " + targetFolder.getPath() + " Properties: " + properties);

        if (!properties.containsKey(PropertyIds.OBJECT_TYPE_ID))
            properties.put(PropertyIds.OBJECT_TYPE_ID, EnumBaseObjectTypeIds.CMIS_FOLDER.value());
        else if (!((String) properties.get(PropertyIds.OBJECT_TYPE_ID)).toUpperCase().startsWith("D:") && !((String) properties.get(PropertyIds.OBJECT_TYPE_ID)).toLowerCase().contains(EnumBaseObjectTypeIds.CMIS_FOLDER.value()))
            properties.put(PropertyIds.OBJECT_TYPE_ID, EnumBaseObjectTypeIds.CMIS_FOLDER.value() + "," + properties.get(PropertyIds.OBJECT_TYPE_ID));

        return targetFolder.createFolder(properties);
    }

    /**
     * füllt den Inhalt eines Dokumentes
     * @param document          das Dokument
     * @param contentStream     der Contentstream mit dem neuen Inhalt
     * @param overwrite         legt fest, ob der Inhalt überschrieben werden soll
     * @param refresh           legt fest, ob das Dokument nach der Operation gefresht werden soll
     * @return
     */
    public CmisObject setContent(Document document,  ContentStream contentStream, boolean overwrite, boolean refresh){
        return session.getObject(document.setContentStream(contentStream, overwrite, refresh));
    }

    /**
     * aktualisiert den Inhalt eines Dokumentes
     * @param  document                  das zu aktualisierende Dokument
     * @param  documentContent           der neue Inhalt. Falls der Content <null> ist, dann werden nur die Properties upgedated.
     * @param  documentType              der Typ des Dokumentes
     * @param  properties                die Properties
     * @param  versionState              bestimmt die Versionierung @seeVersionState
     * @param  versionComment            falls Dokument versionierbar, dann kann hier eine Kommentar zur Version mitgegeben werden
     * @return document                  das geänderte Dokument
     */
    public Document updateDocument(Document document,
                                   byte documentContent[],
                                   String documentType,
                                   Map<String, Object> properties,
                                   VersioningState versionState,
                                   String versionComment) throws ArchivException {

        ContentStream contentStream = null;
        List<String> asp = null;

        if (documentContent != null) {
            InputStream stream = new ByteArrayInputStream(documentContent);
            contentStream = new ContentStreamImpl(document.getName(), BigInteger.valueOf(documentContent.length), documentType, stream);
        }

        CmisObject obj = document;

        properties = convertProperties(properties);

        asp = (List<String>) properties.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);

        //Aspekte hinzufügen
        if (properties != null && properties.size() > 0 ) {
            obj.updateProperties(properties, asp, null, true);
            properties.clear();
        }

        if (versionState.equals(VersioningState.MAJOR) || versionState.equals(VersioningState.MINOR)) {

            obj = checkOutDocument((Document) obj);
            if (obj != null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
                obj = checkInDocument((Document) obj, versionState.equals(VersioningState.MAJOR), properties, contentStream, versionComment);
                session.clear();

            } else {

                if (contentStream != null) {
                    obj = setContent(document, contentStream, true, true);
                    session.clear();
                }
                if (properties != null && properties.size() > 0 )
                    obj = session.getObject(obj.updateProperties(properties, true));
            }
        } else {
            // Update ohne Versionierung (das funktioniert wohl nur genau einmal denn Alfresco lässt bei Documenten mit Versionierung kein
            // Update ohnen diese zu.
            obj = document;
            if (contentStream != null) {
                obj = setContent(document, contentStream, true, true);
                session.clear();
            }

            if (properties != null && properties.size() > 0)
                obj = session.getObject(obj.updateProperties(properties, true));


        }
        return (Document) obj;
    }

    /**
     * aktualisiert die Metadaten eines Dokumentes
     * @param  obj                       das zu aktualisierende Objekt
     * @param  properties                die Properties
     * @return CmisObject                das geänderte Objekt
     */

    public CmisObject updateProperties(CmisObject obj,
                                       Map<String, Object> properties) throws ArchivException {

        if (properties != null && properties.size() == 0)
                 throw new IllegalArgumentException("keine Properties zum Updaten!");
        if (!properties.containsKey(PropertyIds.OBJECT_TYPE_ID))
            properties.put(PropertyIds.OBJECT_TYPE_ID, obj.getPropertyValue(PropertyIds.OBJECT_TYPE_ID));

        obj = session.getObject(obj.updateProperties(convertProperties(properties), true), getOperationContext());
        logger.trace("updateProperties for node " + obj.getId());
        return obj;
    }

    /**
     * prüft, ob ein Dokument versionierbar ist
     * @param  doc                       das Document
     * @return                           true, wenn das Document versionierbar ist
     */
    public boolean isDocumentVersionable(Document doc) {
     return (((DocumentType)(doc.getType())).isVersionable());
    }

    /**
     * checked ein Dokument aus
     * @param  document                 das auszucheckende Dokument
     * @return obj                      das Objekt, oder null falls es nicht auszuchecken ist.
     */
    public CmisObject checkOutDocument(Document document) {
        if (isDocumentVersionable(document)) {
            CmisObject cmisObject = session.getObject(document.checkOut());
            logger.trace("Object " + cmisObject.getId() + " checked out!");
            return cmisObject;
        }
        else
            return null;
    }

    /**
     * checked ein Dokument ein
     * @param document          das einzucheckende Dokument
     * @param major             Major Version
     * @param properties        die properties zum Dokument
     * @param contentStream     der Content des Dokumentes
     * @param checkinComment    der Versionskommentar
     * @return                  das eingecheckte Dokument
     */
    public CmisObject checkInDocument(Document document,
                                      boolean major,
                                      Map<String, Object> properties,
                                      ContentStream contentStream,
                                      String checkinComment) {

        if (isDocumentVersionable(document)) {
            CmisObject cmisObject = session.getObject(document.checkIn(major, convertProperties(properties), contentStream, checkinComment), getOperationContext());
            logger.trace("Object " + cmisObject.getId() + " checked in with Version " + cmisObject.getPropertyValue("versionLabel"));
            return cmisObject;
        }
        else
            return null;
    }

    /**
     * cancel den Chckout eines Dokumentes
     * @param document                  das Dokument
     */
    public void cancelCheckout(Document document) {
        document.cancelCheckOut();
        return;
    }

    /**
     * liefert die Kommentare zu einem Knoten
     * @param obj           der Knoten/Folder als Cmis Objekt
     * @return              ein Map mit den Kommentaren
     * @throws IOException
     */
    public CommentPaging getComments(CmisObject obj) throws IOException, AuthenticationException, URISyntaxException {

        String id = VerteilungHelper.normalizeObjectId(obj.getId());
        URL url = new URL(this.server + (this.server.endsWith("/") ? "" : "/") + NEW_API_NODES_URL + id + "/comments");
        HttpEntity<String> request = new HttpEntity(getHttpHeaders());
        ResponseEntity<CommentPaging> response = restTemplate.exchange(url.toURI(), HttpMethod.GET, request, CommentPaging.class);
        return response.getBody();
    }

    /**
     * fügt einen Kommentar hinzu
     * @param obj                   der Knoten/Folder als Cmis Objekt
     * @param commentContent        der neue Kommentar
     * @return                      eine Map mit dem neuen Kommentar
     * @throws IOException
     */
    public Map addComment(CmisObject obj, String commentContent) throws IOException, AuthenticationException, URISyntaxException {
        String id = VerteilungHelper.normalizeObjectId(obj.getId());
        URL url = new URL(this.server + (this.server.endsWith("/") ? "" : "/") + NEW_API_NODES_URL + id + "/comments");
        Comment comment = new Comment();
        comment.setContent(commentContent);
        HttpEntity<String> request = new HttpEntity(comment, getHttpHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(url.toURI(), HttpMethod.POST, request, Map.class);
        return response.getBody();
    }

    /**
     * bereitet die Typen der Properties auf
     * @param properties  die Property Werte
     * @return            die Properties mit den richtigen Typen
     */
    private Map<String, Object> convertProperties(Map<String, Object> properties) {

        HashMap<String, Object> props = new HashMap<>();
        Map<String, PropertyDefinition<?>> definitions = new HashMap<>();
        List<String> types = new ArrayList<>();
        if (properties!= null && properties.size() > 0) {
            // Typen suchen
            types.add((String) properties.get(PropertyIds.OBJECT_TYPE_ID));
            if (properties.containsKey(PropertyIds.SECONDARY_OBJECT_TYPE_IDS))
                types.addAll((List<String>) properties.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS));
            for (String type : types) {
                definitions.putAll(this.session.getTypeDefinition(type).getPropertyDefinitions());
            }

            for (String key : properties.keySet()) {
                PropertyDefinition<?> definition = definitions.get(key);
                //TODO Hier fehlt eventuell noch das parsen auf die anderen Datentypen
                if (definition instanceof PropertyDateTimeDefinition) {
                    Date date = new Date();
                    if (properties.get(key) instanceof Long)
                        date.setTime((Long) properties.get(key));
                    else if (properties.get(key) instanceof String) {
                        try {
                            Long value = Long.parseLong((String) properties.get(key));
                            date.setTime(value);
                        } catch (NumberFormatException ignored) {}
                    }
                    props.put(key, date);
                } else if (definition instanceof PropertyDecimalDefinition) {
                    if (properties.get(key) instanceof String)
                        props.put(key, ((String) properties.get(key)).isEmpty() ? 0 : Double.parseDouble((String) properties.get(key)));
                    if (properties.get(key) instanceof BigDecimal || properties.get(key) instanceof Double || properties.get(key) instanceof Float || properties.get(key) instanceof Byte || properties.get(key) instanceof Short || properties.get(key) instanceof Integer || properties.get(key) instanceof Long)
                        props.put(key, properties.get(key));
                } else if (definition instanceof PropertyBooleanDefinition) {
                    if (properties.get(key) instanceof String)
                        props.put(key, Boolean.parseBoolean((String) properties.get(key)));
                    if (properties.get(key) instanceof Boolean)
                        props.put(key, properties.get(key));
                } else {
                    props.put(key, properties.get(key));
                }
            }
        }
        return props;
    }

    /**
     * prüft, ob eine Regel für einen Knoten existiert
     *
     * @param name          der Name der Regel
     * @param folderId      die Id des Folders
     * @return              true, wenn eine Regel mit dem Namen vorhanden ist
     * @throws IOException
     * @throws AuthenticationException
     * @throws URISyntaxException
     */
    public boolean hasRule(String name, String folderId) throws IOException, AuthenticationException, URISyntaxException {

        URL url = new URL(this.server + (this.server.endsWith("/") ? "" : "/") + NODES_URL + folderId + "/ruleset/rules");
        HttpEntity<String> request = new HttpEntity<>(getHttpHeaders());
        ResponseEntity<RuleResponse> result = restTemplate.exchange(url.toURI(), HttpMethod.GET, request, RuleResponse.class) ;
        return result.getBody().hasRule(name);
    }

    /**
     * legt eine neue Regel für einen Folder an
     *
     * @param folderId      die Id des Folders
     * @param scriptId      die Id des Scriprdarei
     * @param name          der Name der Regel
     * @param description   die Beschreibung der Regel
     * @return Map          eine Map mit der Rule
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public Map createRule(String folderId, String scriptId, String name, String description) throws MalformedURLException, URISyntaxException {

        Rule rule = new Rule();
        rule.setTitle(name);
        rule.setDescription(description);
        rule.setDisabled(false);
        rule.setRuleType(Arrays.asList("inbound"));
        rule.setApplyToChildren(false);
        rule.setExecuteAsynchronously(false);
        Action action = new Action();
        action.setActionDefinitionName("composite-action");
        action.setExecuteAsync(false);
        Action ruleAction = new Action();
        ruleAction.setActionDefinitionName( "script");
        HashMap<String, String> parameterValues = new HashMap<>();
        parameterValues.put("script-ref", "workspace://SpacesStore/" + VerteilungHelper.normalizeObjectId(scriptId));
        ruleAction.setParameterValues(parameterValues);
        ruleAction.setExecuteAsync(false);
        Condition ruleCondition = new Condition();
        ruleCondition.setConditionDefinitionName("no-condition");
        ruleCondition.setInvertCondition(false);
        List<Action> ruleActions = new ArrayList<>();
        ruleActions.add(ruleAction);
        action.setActions(ruleActions);
        List<Condition> ruleConditions = new ArrayList<>();
        ruleConditions.add(ruleCondition);
        action.setConditions(ruleConditions);
        List<Action> ruleActions1 = new ArrayList<>();
        ruleActions1.add(action);
        rule.setAction(action);
        URL url = new URL("http://localhost:80/alfresco/service/api/node/workspace/SpacesStore/" + VerteilungHelper.normalizeObjectId(folderId) + "/ruleset/rules");
        HttpEntity<String> request = new HttpEntity(rule, getHttpHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(url.toURI(), HttpMethod.POST, request, Map.class);
        return response.getBody();
    }

    /**
     * liefert den Http header für die Restcalls und sorgt für die Authentifizierung am Server
     *
     * @return      der HttpHeader
     */
    private HttpHeaders getHttpHeaders() {
        return new HttpHeaders() {{
            String auth = user + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }

    /**
     * baut einen OperationContext auf
     * @return der OperationContext
     */
    private OperationContext getOperationContext() {
        if (operationContext == null) {
            operationContext = this.session.createOperationContext();
            operationContext.setIncludeAllowableActions(false);
            operationContext.setIncludePolicies(false);
            operationContext.setIncludeAcls(false);
            operationContext.setRenditionFilterString("cmis:thumbnail,image/*");
        }
        return operationContext;
    }
}
