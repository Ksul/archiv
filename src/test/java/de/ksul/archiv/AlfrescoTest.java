package de.ksul.archiv;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.hamcrest.Matchers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.01.14
 * Time: 16:05
 */
public abstract class AlfrescoTest {


     protected AlfrescoConnector con;

    public void setUp() throws Exception {
        if (needsProxy()) {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", "http-proxy.lvm.de");
            System.getProperties().put("proxyPort", "8080");
        } else {
            System.getProperties().put("proxySet", "false");
            System.getProperties().put("proxyHost", "");
            System.getProperties().put("proxyPort", "");
        }
        shutDown();
    }

    public void shutDown() throws Exception {
        CmisObject cmisObject = con.getNode("/TestFolder/TestDocument.txt");
        if (cmisObject != null && cmisObject instanceof Document) {
            if (((Document) cmisObject).isVersionSeriesCheckedOut())
                ((Document) cmisObject).cancelCheckOut();
            cmisObject.delete(true);
        }
        cmisObject = con.getNode("/TestFolder/TestDocument");
        if (cmisObject != null && ((Document) cmisObject).isVersionSeriesCheckedOut())
            ((Document) cmisObject).cancelCheckOut();
        if (cmisObject != null && cmisObject instanceof Document)
            cmisObject.delete(true);
        cmisObject = con.getNode("/" + con.getDataDictionaryName() + "/" + con.getScriptFolderName() + "/backup.js.sample");
        if (cmisObject != null && cmisObject instanceof Document) {
            if (((Document) cmisObject).isVersionSeriesCheckedOut())
                ((Document) cmisObject).cancelCheckOut();
        }
        cmisObject = con.getNode("/TestDocument.txt");
        if (cmisObject != null && ((Document) cmisObject).isVersionSeriesCheckedOut())
            ((Document) cmisObject).cancelCheckOut();
        if (cmisObject != null && cmisObject instanceof Document)
            cmisObject.delete(true);
        cmisObject = con.getNode("/Archiv/ArchivControllerTest.pdf");
        if (cmisObject != null && ((Document) cmisObject).isVersionSeriesCheckedOut())
            ((Document) cmisObject).cancelCheckOut();
        if (cmisObject != null && cmisObject instanceof Document)
            cmisObject.delete(true);
        cmisObject = con.getNode("/TestFolder");
        if (cmisObject != null && cmisObject instanceof Folder)
            ((Folder) cmisObject).deleteTree(true, UnfileObject.DELETE, true);
        cmisObject = con.getNode("/FolderTest");
        if (cmisObject != null && cmisObject instanceof Folder)
            ((Folder) cmisObject).deleteTree(true, UnfileObject.DELETE, true);
    }

    /**
     * liest eine Datei
     * @param   name            der Name der Datei
     * @return  byte[]          der Inhalt der Datei als byte Array
     * @throws IOException
     */
    protected byte[] readFile(String name) throws IOException {

        File sourceFile = new File(name);
        FileInputStream in = new FileInputStream(sourceFile);
        byte[] buffer = new byte[(int) sourceFile.length()];
        in.read(buffer);
        return buffer;
    }

    /**
     * prüft, ob ein Proxy zur Verbindung ins Internet beötigt wird
     * @return
     */
    protected boolean needsProxy() {

        try {
            return InetAddress.getByName("www-proxy").isReachable(5000);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * baut einen Folder auf
     * @param  name                        der Name des Folders
     * @return CmisObject                  der Folder als CmisObject
     * @throws ArchivException
     */
    public CmisObject buildTestFolder(String name, CmisObject folder) throws ArchivException {
        if (folder == null)
            folder = con.getNode("/");
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        Map<String, Object> props = new HashMap<>();
        List<String> aspects = new ArrayList<>();
        aspects.add("P:cm:titled");
        props.put("cm:title", "");
        props.put(PropertyIds.NAME, name);
        props.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects);
        folder = con.createFolder((Folder) folder, props);
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        assertThat(folder.getName(), Matchers.equalTo(name));
        return folder;
    }


    /**
     * baut ein Document auf
     * @param  name                     der Name
     * @param  folder                   der Folder, in dem es erstellt werden soll
     * @return CmisObject               das Dokument als CmisObjeect
     * @throws ArchivException
     */
    public CmisObject buildDocument(String name, CmisObject folder) throws ArchivException {
        Document document;
        assertThat(folder instanceof Folder, Matchers.is(true));
        List<String> aspects = new ArrayList<>();
        Map<String, Object> props = new HashMap<>();
        props.put("my:person", "Klaus");
        props.put("my:documentDate", Long.toString(new Date().getTime()));
        props.put("cm:title", "");
        props.put(PropertyIds.OBJECT_TYPE_ID, "D:my:archivContent");
        props.put(PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
        aspects.add("P:cm:titled");
        aspects.add("P:my:amountable");
        aspects.add("P:my:idable");
        props.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects);
        document = con.createDocument((Folder) folder, name, new byte[]{}, VerteilungConstants.DOCUMENT_TYPE_TEXT, props, VersioningState.MINOR);
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf( Document.class));
        assertThat(document.getName(), Matchers.is(name));
        return document;
    }

    /**
     * liefert einen Vergleichsstring mit dem Inhalt aus dem TestPDF
     * @return  der Vergleichsstring
     */
    public String getPDFCompareString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Herr");
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append("Klaus Schulte");
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append("Bredeheide 33");
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append("48161 Münster");
        return stringBuilder.toString();
    }
}
