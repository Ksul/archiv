package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivProperties;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.repository.MockUtils;
import de.ksul.archiv.repository.Repository;
import de.ksul.archiv.repository.TreeNode;
import de.ksul.archiv.repository.script.RecognizeEndpoints;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 28.08.18
 * Time: 10:18
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ArchivTestConfiguration.class})
@TestPropertySource(properties = {"ksul.archiv.test.testData="})
@DirtiesContext
public class ScriptTest {

    private Invocable invocable;
    private Object rec;
    private ScriptEngine engine;
    private TreeNode<FileableCmisObject> root;


    @BeforeEach
    public void setUp() throws Exception {

        root = Repository.getInstance().findTreeNodeForPath("/");
        TreeNode<FileableCmisObject> node = Repository.getInstance().insert(root, MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, "/","Archiv", MockUtils.getInstance().getFolderType(), null), false);
        Repository.getInstance().insert(node, MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, "/Archiv","Inbox", MockUtils.getInstance().getFolderType(), null), false);
        Repository.getInstance().insert(node, MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, "/Archiv","Unbekannt", MockUtils.getInstance().getFolderType(), null), false);
        TreeNode<FileableCmisObject> fehler = Repository.getInstance().insert(node, MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, "/Archiv","Fehler", MockUtils.getInstance().getFolderType(), null), false);
        Repository.getInstance().insert(fehler, MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, "/Archiv/Fehler","Doppelte", MockUtils.getInstance().getFolderType(), null), false);
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        RecognizeEndpoints.setRepository(Repository.getInstance());
        RecognizeEndpoints.setScript("/Data Dictionary/Scripts/recognition.js");
        rec = engine.eval("load(\"src/main/resources/static/js/recognition.js\");");

        invocable = (Invocable) engine;
        engine.eval("logger = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints.JSLogger');");

        engine.eval("script = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').script;");
        engine.eval("companyhome = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').companyhome;");

    }



    @Test
    public void testScriptForUnknown() throws Exception {
        TreeNode<FileableCmisObject> node = Repository.getInstance().insert(root, MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, "/" , "Test.pdf", MockUtils.getInstance().getDocumentType(), VerteilungConstants.DOCUMENT_TYPE_PDF), MockUtils.getInstance().createFileStream("classpath:Test.pdf",  VerteilungConstants.DOCUMENT_TYPE_PDF), "1.0", false);
        RecognizeEndpoints.setDocument(node);
        engine.eval("document = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').document;");
        invocable.invokeMethod(rec, "run");
        MatcherAssert.assertThat(Repository.getInstance().findTreeNodeForPath("/Archiv/Unbekannt/2011/Mai/Test.pdf"), Matchers.notNullValue());
    }

    @Test
    public void testScript() throws Exception {
        TreeNode<FileableCmisObject> node = Repository.getInstance().insert(root, MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, "/" , "Test.txt", MockUtils.getInstance().getDocumentType(), VerteilungConstants.DOCUMENT_TYPE_PDF), MockUtils.getInstance().createStream("Caffee Fausto   Rechnung Nr 12345 Bertrag 79€ 01.01.2010",  VerteilungConstants.DOCUMENT_TYPE_TEXT), "1.0", false);
        RecognizeEndpoints.setDocument(node);
        engine.eval("document = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').document;");
        invocable.invokeMethod(rec, "run");
    }
}
