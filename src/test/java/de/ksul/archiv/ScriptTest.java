package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivTestConfiguration;
import de.ksul.archiv.repository.MockUtils;
import de.ksul.archiv.repository.Repository;
import de.ksul.archiv.repository.TreeNode;
import de.ksul.archiv.repository.script.RecognizeEndpoints;
import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.CMISStrictDictionaryService;
import org.alfresco.opencmis.dictionary.DocumentTypeDefinitionWrapper;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.dictionary.*;
import org.alfresco.repo.i18n.MessageServiceImpl;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.DynamicallySizedThreadPoolExecutor;
import org.alfresco.util.TraceableThreadFactory;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
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
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.MOCK, classes = {ArchivTestConfiguration.class, ArchivTestApplication.class})

@TestPropertySource(properties = {"ksul.archiv.testing.testData="})
@DirtiesContext
public class ScriptTest {

    private Invocable invocable;
    private Object rec;
    private ScriptEngine engine;
    private TreeNode<FileableCmisObject> root;


    @BeforeEach
    public void setUp() throws Exception {

        root = Repository.getInstance().getRoot();
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        RecognizeEndpoints.setRepository(Repository.getInstance());
        RecognizeEndpoints.setScript("/Data Dictionary/Scripts/recognition.js");
        rec = engine.eval("load(\"src/main/resources/static/js/recognition.js\");");

        invocable = (Invocable) engine;
        engine.eval("logger = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints.JSLogger');");

        engine.eval("script = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').script;");
        engine.eval("companyhome = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').companyhome;");
        engine.eval("classification = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').categoryhome;");
        engine.eval("commentService = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').commentService;");
        engine.eval("search = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').searchService;");

    }



    @Test
    public void testScriptForUnknown() throws Exception {
        TreeNode<FileableCmisObject> node = Repository.getInstance().insert(root, MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, "/" , "Test.pdf", MockUtils.getInstance().getDocumentType("cmis:document"), VerteilungConstants.DOCUMENT_TYPE_PDF), MockUtils.getInstance().createFileStream("classpath:Test.pdf",  VerteilungConstants.DOCUMENT_TYPE_PDF), "1.0", false);
        RecognizeEndpoints.setDocument(node);
        engine.eval("document = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').document;");
        invocable.invokeMethod(rec, "run");
        MatcherAssert.assertThat(Repository.getInstance().findTreeNodeForPath("/Archiv/Unbekannt/2011/Mai/Test.pdf"), Matchers.notNullValue());
    }

    @Test
    public void testScript() throws Exception {
        TreeNode<FileableCmisObject> node = Repository.getInstance().insert(root, MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, "/" , "Test.txt", MockUtils.getInstance().getDocumentType("cmis:document"), VerteilungConstants.DOCUMENT_TYPE_TEXT), MockUtils.getInstance().createStream("Caffee Fausto   Rechnung Nr 12345  01.01.2010 Betrag  79€",  VerteilungConstants.DOCUMENT_TYPE_TEXT), "1.0", false);
        RecognizeEndpoints.setDocument(node);
        engine.eval("document = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').document;");
        invocable.invokeMethod(rec, "run");
        node = Repository.getInstance().findTreeNodeForPath("/Archiv/Dokumente/Rechnungen/Rechnungen Caffé Fausto/Test.txt");
        MatcherAssert.assertThat(node, Matchers.notNullValue());
    }

    @Test
    public void testScriptForX() throws Exception {
        TreeNode<FileableCmisObject> node = Repository.getInstance().insert(root, MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, "/" , "Test1.pdf", MockUtils.getInstance().getDocumentType("cmis:document"), VerteilungConstants.DOCUMENT_TYPE_PDF), MockUtils.getInstance().createFileStream("classpath:Test1.pdf",  VerteilungConstants.DOCUMENT_TYPE_PDF), "1.0", false);
        RecognizeEndpoints.setDocument(node);
        engine.eval("document = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').document;");
        invocable.invokeMethod(rec, "run");
        MatcherAssert.assertThat(Repository.getInstance().findTreeNodeForPath("/Archiv/Unbekannt/2011/Mai/Test.pdf"), Matchers.notNullValue());
    }

    @Test
    public void testModel() throws Exception {

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
        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("alfresco/model/forumModel.xml")));
        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("static/model/archivModel.xml")));

      //  dictionaryDao.getDictionaryRegistry(tenantService.getCurrentUserDomain()).getModel(QName.createQName("{archiv.model}archivModel"))

     //   dictionaryDao.getDictionaryRegistry(tenantService.getCurrentUserDomain()).getModel(QName.createQName("{http://www.alfresco.org/model/cmis/1.0/cs01}cmismodel")).getType(QName.createQName("{http://www.alfresco.org/model/cmis/1.0/cs01}document")).getProperties();
        org.alfresco.service.cmr.dictionary.PropertyDefinition propertyDefinition = dictionaryDao.getDictionaryRegistry(tenantService.getCurrentUserDomain()).getModel(QName.createQName("{http://www.alfresco.org/model/cmis/1.0/cs01}cmismodel")).getType(QName.createQName("{http://www.alfresco.org/model/cmis/1.0/cs01}document")).getProperties().get(QName.createQName("{http://www.alfresco.org/model/cmis/1.0/cs01}objectId"));
        CMISMapping cmisMapping = new CMISMapping();
        cmisMapping.afterPropertiesSet();
        DictionaryComponent dictionaryComponent = new DictionaryComponent();
        dictionaryComponent.setDictionaryDAO(dictionaryDao);
        cmisMapping.setDictionaryService(dictionaryComponent);
        DictionaryNamespaceComponent namespaceComponent = new DictionaryNamespaceComponent();
        namespaceComponent.setNamespaceDAO(dictionaryDao);
        cmisMapping.setNamespaceService(namespaceComponent);
        cmisMapping.setCmisVersion(CmisVersion.CMIS_1_1);
        CMISDictionaryService cmisDictionaryService = new CMISStrictDictionaryService();
        ((CMISStrictDictionaryService) cmisDictionaryService).setSingletonCache(new DefaultSimpleCache<>());
        ((CMISStrictDictionaryService) cmisDictionaryService).setCmisMapping(cmisMapping);
        ((CMISStrictDictionaryService) cmisDictionaryService).setDictionaryDAO(dictionaryDao);
        ((CMISStrictDictionaryService) cmisDictionaryService).setTenantService(tenantService);
        MessageServiceImpl messageService = new MessageServiceImpl();
        messageService.setTenantService(tenantService);
        messageService.setLoadedResourceBundlesCache(new DefaultSimpleCache<>());
        messageService.setMessagesCache(new DefaultSimpleCache<>());
        messageService.setResourceBundleBaseNamesCache(new DefaultSimpleCache<>());
        dictionaryComponent.setMessageLookup(messageService);
        ((CMISStrictDictionaryService) cmisDictionaryService).setDictionaryService(dictionaryComponent);
        ((CMISStrictDictionaryService) cmisDictionaryService).init();
        cmisDictionaryService.findType("D:my:archivContent");
        ClassDefinition classDefinition = dictionaryDao.getDictionaryRegistry(tenantService.getCurrentUserDomain()).getModel(QName.createQName("{archiv.model}archivModel")).getType(QName.createQName("{archiv.model}archivContent"));
        DocumentTypeDefinitionWrapper wrapper = new DocumentTypeDefinitionWrapper(cmisMapping, null, null, "my:archivContent", dictionaryComponent, classDefinition );
        wrapper.getProperties(false);
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
    //    bootstrap.setModels(bootstrapModels);
        bootstrap.setDictionaryDAO(dictionaryDao);
        bootstrap.bootstrap();


    }

}
