package de.ksul.archiv.repository;

import de.ksul.archiv.configuration.ArchivProperties;
import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.CMISStrictDictionaryService;
import org.alfresco.opencmis.dictionary.PropertyDefinitionWrapper;
import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.dictionary.*;
import org.alfresco.repo.i18n.MessageServiceImpl;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.DynamicallySizedThreadPoolExecutor;
import org.alfresco.util.TraceableThreadFactory;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 12.09.18
 * Time: 16:58
 */
public class PropertyDefinitionBuilder {

    private DictionaryDAOImpl dictionaryDao = new DictionaryDAOImpl();
    private DictionaryComponent dictionaryComponent = new DictionaryComponent();
    private DictionaryNamespaceComponent namespaceComponent = new DictionaryNamespaceComponent();
    private CMISMapping cmisMapping  = new CMISMapping();
    private TenantService tenantService = new SingleTServiceImpl();
    private CMISDictionaryService cmisDictionaryService = new CMISStrictDictionaryService();
    private MessageServiceImpl messageService = new MessageServiceImpl();
    private ArchivProperties archivProperties;


    @Autowired
    public PropertyDefinitionBuilder(ArchivProperties archivProperties) {
        this.archivProperties = archivProperties;
        init();
    }

    public CMISDictionaryService getCmisDictionaryService() {
        return cmisDictionaryService;
    }

    public DictionaryComponent getDictionaryComponent() {
        return dictionaryComponent;
    }

    public void init() {

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
        for (String model : archivProperties.getTesting().getModels())
            dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream(model)));

        cmisMapping.afterPropertiesSet();

        dictionaryComponent.setDictionaryDAO(dictionaryDao);
        cmisMapping.setDictionaryService(dictionaryComponent);

        namespaceComponent.setNamespaceDAO(dictionaryDao);
        cmisMapping.setNamespaceService(namespaceComponent);
        cmisMapping.setCmisVersion(CmisVersion.CMIS_1_1);


        ((CMISStrictDictionaryService) cmisDictionaryService).setSingletonCache(new DefaultSimpleCache<>());
        ((CMISStrictDictionaryService) cmisDictionaryService).setCmisMapping(cmisMapping);
        ((CMISStrictDictionaryService) cmisDictionaryService).setDictionaryDAO(dictionaryDao);
        ((CMISStrictDictionaryService) cmisDictionaryService).setTenantService(tenantService);

        messageService.setTenantService(tenantService);
        messageService.setLoadedResourceBundlesCache(new DefaultSimpleCache<>());
        messageService.setMessagesCache(new DefaultSimpleCache<>());
        messageService.setResourceBundleBaseNamesCache(new DefaultSimpleCache<>());
        dictionaryComponent.setMessageLookup(messageService);
        ((CMISStrictDictionaryService) cmisDictionaryService).setDictionaryService(dictionaryComponent);
        ((CMISStrictDictionaryService) cmisDictionaryService).init();

    }

    public DictionaryDAO getDictionaryDao() {
        return dictionaryDao;
    }

    public TypeDefinition getTypeDefinition(String name) {
        TypeDefinitionWrapper typeDefinitionWrapper = cmisDictionaryService.findType(name);
        return typeDefinitionWrapper.getTypeDefinition(true);
    }

    public Map<String, PropertyDefinition<?>> getPropertyDefinitionMap(String name) {

        TypeDefinitionWrapper typeDefinitionWrapper = cmisDictionaryService.findType(name);
        if (typeDefinitionWrapper == null)
            throw new CmisRuntimeException("type " + name + " not found!");
        Collection<PropertyDefinitionWrapper> propertyDefinitions = typeDefinitionWrapper.getProperties(false);

        Map<String, PropertyDefinition<?>> propertyDefinitionMap = new HashMap<>();
        for (PropertyDefinitionWrapper definition : propertyDefinitions) {
            propertyDefinitionMap.put(definition.getPropertyDefinition().getId(), definition.getPropertyDefinition());
        }

        return propertyDefinitionMap;
    }

    public boolean isSubtypeOf(String nodeType, String typeName)
    {
        if (nodeType.startsWith("D:"))
            nodeType = nodeType.substring(2);
        if (typeName.startsWith("D:"))
            typeName = typeName.substring(2);
        QName nodeTypeQName = QName.createQName(nodeType, dictionaryDao);
        QName typeQName = QName.createQName(typeName, dictionaryDao);

        return dictionaryComponent.isSubClass(nodeTypeQName, typeQName);
    }





}
