package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivProperties;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.repository.MockUtils;
import de.ksul.archiv.repository.Repository;
import de.ksul.archiv.repository.TreeNode;
import de.ksul.archiv.repository.script.RecognizeEndpoints;
import org.alfresco.repo.dictionary.*;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.util.DynamicallySizedThreadPoolExecutor;
import org.alfresco.util.TraceableThreadFactory;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

        root = Repository.getInstance().getRoot();
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
        engine.eval("classification = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').categoryhome;");

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

    @Test
    public void testModel() throws Exception {
//        List<String> bootstrapModels = new ArrayList<>();
//        bootstrapModels.add("alfresco/model/dictionaryModel.xml");
//        bootstrapModels.add("alfresco/model/systemModel.xml");
//        bootstrapModels.add("org/alfresco/repo/security/authentication/userModel.xml");
//        bootstrapModels.add("alfresco/model/contentModel.xml");
//       // bootstrapModels.add("alfresco/model/wcmModel.xml");
//        bootstrapModels.add("alfresco/model/applicationModel.xml");
//        bootstrapModels.add("alfresco/model/bpmModel.xml");
//        //bootstrapModels.add("alfresco/model/wcmAppModel.xml");
//        bootstrapModels.add("alfresco/model/cmisModel.xml");
//        bootstrapModels.add("alfresco/workflow/workflowModel.xml");
//        bootstrapModels.add("alfresco/model/siteModel.xml");

        TenantService tenantService = new SingleTServiceImpl();
        DictionaryDAOImpl dictionaryDao = new DictionaryDAOImpl();
        dictionaryDao.setTenantService(tenantService);
        CompiledModelsCache compiledModelsCache = new CompiledModelsCache();
        compiledModelsCache.setDictionaryDAO(dictionaryDao);
        compiledModelsCache.setTenantService(tenantService);
        compiledModelsCache.setRegistry(new DefaultAsynchronouslyRefreshedCacheRegistry());
        TraceableThreadFactory threadFactory = new TraceableThreadFactory();
        threadFactory.setThreadDaemon(true);
        threadFactory.setThreadPriority(Thread.NORM_PRIORITY);

        ThreadPoolExecutor threadPoolExecutor = new DynamicallySizedThreadPoolExecutor(20, 20, 90, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());
        compiledModelsCache.setThreadPoolExecutor(threadPoolExecutor);
        dictionaryDao.setDictionaryRegistryCache(compiledModelsCache);
        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("alfresco/model/dictionaryModel.xml")));
        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("alfresco/model/systemModel.xml")));
        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("alfresco/model/contentModel.xml")));

        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/security/authentication/userModel.xml")));
        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("alfresco/model/applicationModel.xml")));
        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("alfresco/model/bpmModel.xml")));
        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("alfresco/model/cmisModel.xml")));
        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("alfresco/workflow/workflowModel.xml")));
        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("alfresco/model/siteModel.xml")));
        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("src/main/resources/static/model/archivModel.xml")));

        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
    //    bootstrap.setModels(bootstrapModels);
        bootstrap.setDictionaryDAO(dictionaryDao);
        bootstrap.bootstrap();


    }
}
