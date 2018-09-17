package de.ksul.archiv.repository;

import org.alfresco.opencmis.dictionary.*;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.repo.dictionary.*;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.DynamicallySizedThreadPoolExecutor;
import org.alfresco.util.TraceableThreadFactory;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

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

    public enum Typ{
        DOCUMENT, FOLDER, ITEM, SECONDARY
    }
    private DictionaryDAOImpl dictionaryDao = new DictionaryDAOImpl();
    private DictionaryComponent dictionaryComponent = new DictionaryComponent();
    private DictionaryNamespaceComponent namespaceComponent = new DictionaryNamespaceComponent();
    private CMISMapping cmisMapping  = new CMISMapping();
    private TenantService tenantService = new SingleTServiceImpl();


    public PropertyDefinitionBuilder() {


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
        dictionaryDao.putModel(M2Model.createModel(getClass().getClassLoader().getResourceAsStream("static/model/archivModel.xml")));

        cmisMapping.afterPropertiesSet();

        dictionaryComponent.setDictionaryDAO(dictionaryDao);
        cmisMapping.setDictionaryService(dictionaryComponent);

        namespaceComponent.setNamespaceDAO(dictionaryDao);
        cmisMapping.setNamespaceService(namespaceComponent);
        cmisMapping.setCmisVersion(CmisVersion.CMIS_1_1);


    }

    public DictionaryDAO getDictionaryDao() {
        return dictionaryDao;
    }

    public PropertyDefinition getPropertyDefinition(String name) {
       // return dictionaryDao.getDictionaryRegistry(tenantService.getCurrentUserDomain()).getProperty(QName.createQName(name, dictionaryDao));
        return null;
    }

    public Map<String, PropertyDefinition<?>> getPropertyDefinitionMap(String name, Typ typ) {

        QName qName;

        String[] n = name.split(":");
        qName = QName.createQName(n[0], n[1], dictionaryDao);


        Map<String, PropertyDefinition<?>> propertyDefinitionMap = new HashMap<>();
        ClassDefinition classDefinition;
        if (typ.equals(Typ.SECONDARY))
            classDefinition = dictionaryDao.getDictionaryRegistry(tenantService.getCurrentUserDomain()).getAspect(qName);
        else
            classDefinition = dictionaryDao.getDictionaryRegistry(tenantService.getCurrentUserDomain()).getType(qName);
        AbstractTypeDefinitionWrapper wrapper;
        switch (typ) {
            case DOCUMENT: {
                wrapper = new DocumentTypeDefinitionWrapper(cmisMapping, null, null, classDefinition.getName().toPrefixString(), dictionaryComponent, classDefinition);
                break;
            }
            case FOLDER: {
                wrapper = new FolderTypeDefintionWrapper(cmisMapping, null, null, classDefinition.getName().toPrefixString(), dictionaryComponent, classDefinition);
                break;
            }
            case ITEM:  {
                wrapper = new ItemTypeDefinitionWrapper(cmisMapping, null, null, classDefinition.getName().toPrefixString(), dictionaryComponent, classDefinition);
                break;
            }
            case SECONDARY:{
                wrapper = new SecondaryTypeDefinitionWrapper(cmisMapping, null, null, classDefinition.getName().toPrefixString(), dictionaryComponent, classDefinition);
                break;
            }
            default: {
                throw new CmisRuntimeException("kein Typ angegeben!");
            }
        }
        Collection<PropertyDefinitionWrapper> propertyDefinitions = wrapper.getProperties(false);

        for (PropertyDefinitionWrapper definition: propertyDefinitions){
            propertyDefinitionMap.put(definition.getPropertyDefinition().getId(), definition.getPropertyDefinition());
        }

        return propertyDefinitionMap;
    }




}
