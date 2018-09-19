package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivProperties;
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
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.dictionary.constraint.NumericRangeConstraint;
import org.alfresco.repo.dictionary.constraint.StringLengthConstraint;
import org.alfresco.repo.i18n.MessageServiceImpl;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.DynamicallySizedThreadPoolExecutor;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.ISO9075;
import org.alfresco.util.TraceableThreadFactory;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
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
        TreeNode<FileableCmisObject> node = Repository.getInstance().insert(root, MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, "/" , "Test.pdf", MockUtils.getInstance().getDocumentType("cmis:document"), VerteilungConstants.DOCUMENT_TYPE_PDF), MockUtils.getInstance().createFileStream("classpath:Test.pdf",  VerteilungConstants.DOCUMENT_TYPE_PDF), "1.0", false);
        RecognizeEndpoints.setDocument(node);
        engine.eval("document = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').document;");
        invocable.invokeMethod(rec, "run");
        MatcherAssert.assertThat(Repository.getInstance().findTreeNodeForPath("/Archiv/Unbekannt/2011/Mai/Test.pdf"), Matchers.notNullValue());
    }

    @Test
    public void testScript() throws Exception {
        TreeNode<FileableCmisObject> node = Repository.getInstance().insert(root, MockUtils.getInstance().createFileableCmisObject(Repository.getInstance(), null, "/" , "Test.txt", MockUtils.getInstance().getDocumentType("cmis:document"), VerteilungConstants.DOCUMENT_TYPE_PDF), MockUtils.getInstance().createStream("Caffee Fausto   Rechnung Nr 12345 Bertrag 79€ 01.01.2010",  VerteilungConstants.DOCUMENT_TYPE_TEXT), "1.0", false);
        RecognizeEndpoints.setDocument(node);
        engine.eval("document = Java.type('de.ksul.archiv.repository.script.RecognizeEndpoints').document;");
        invocable.invokeMethod(rec, "run");
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

    private PropertyDefinition createPropertydefinition (CMISMapping cmisMapping, org.alfresco.service.cmr.dictionary.PropertyDefinition propDef) {

        PropertyType datatype = cmisMapping.getDataType(propDef.getDataType());
        if (datatype == null)
        {
            return null;
        }

        AbstractPropertyDefinition<?> result = null;

        switch (datatype)
        {
            case BOOLEAN:
                result = new PropertyBooleanDefinitionImpl();
                break;
            case DATETIME:
                result = new PropertyDateTimeDefinitionImpl();
                break;
            case DECIMAL:
                result = new PropertyDecimalDefinitionImpl();
                break;
            case HTML:
                result = new PropertyHtmlDefinitionImpl();
                break;
            case ID:
                result = new PropertyIdDefinitionImpl();
                break;
            case INTEGER:
                result = new PropertyIntegerDefinitionImpl();
                break;
            case STRING:
                result = new PropertyStringDefinitionImpl();
                break;
            case URI:
                result = new PropertyUriDefinitionImpl();
                break;
            default:
                throw new RuntimeException("Unknown datatype! Spec change?");
        }

        String id = cmisMapping.buildPrefixEncodedString(propDef.getName());

        if (id.equals(PropertyIds.OBJECT_TYPE_ID) || id.equals(PropertyIds.SOURCE_ID)
                || id.equals(PropertyIds.TARGET_ID))
        {
            // the CMIS spec requirement
            result.setUpdatability(Updatability.ONCREATE);
        } else
        {
            result.setUpdatability(propDef.isProtected() ? Updatability.READONLY : Updatability.READWRITE);
        }

        result.setId(id);
        result.setLocalName(propDef.getName().getLocalName());
        result.setLocalNamespace(propDef.getName().getNamespaceURI());
        result.setDisplayName(null);
        result.setDescription(null);
        result.setPropertyType(datatype);
        result.setCardinality(propDef.isMultiValued() ? Cardinality.MULTI : Cardinality.SINGLE);
        //result.setIsInherited(inherited);
        result.setIsRequired(propDef.isMandatory());

        result.setQueryName(ISO9075.encodeSQL(cmisMapping.buildPrefixEncodedString(propDef.getName())));
        result.setIsQueryable(propDef.isIndexed());
        result.setIsOrderable(false);

        if (result.isQueryable())
        {
            if (result.getCardinality() == Cardinality.SINGLE)
            {
                IndexTokenisationMode indexTokenisationMode = IndexTokenisationMode.TRUE;
                if (propDef.getIndexTokenisationMode() != null)
                {
                    indexTokenisationMode = propDef.getIndexTokenisationMode();
                }

                switch (indexTokenisationMode)
                {
                    case BOTH:
                    case FALSE:
                        result.setIsOrderable(true);
                        break;
                    case TRUE:
                    default:
                        if (propDef.getDataType().getName().equals(DataTypeDefinition.BOOLEAN)
                                || propDef.getDataType().getName().equals(DataTypeDefinition.DATE)
                                || propDef.getDataType().getName().equals(DataTypeDefinition.DATETIME)
                                || propDef.getDataType().getName().equals(DataTypeDefinition.DOUBLE)
                                || propDef.getDataType().getName().equals(DataTypeDefinition.FLOAT)
                                || propDef.getDataType().getName().equals(DataTypeDefinition.INT)
                                || propDef.getDataType().getName().equals(DataTypeDefinition.LONG)
                                || propDef.getDataType().getName().equals(DataTypeDefinition.PATH)
                        )
                        {
                            result.setIsOrderable(true);
                        }
                }
            }
        }

        if (result instanceof PropertyIntegerDefinitionImpl)
        {
            if (propDef.getDataType().getName().equals(DataTypeDefinition.INT))
            {
                ((PropertyIntegerDefinitionImpl) result).setMinValue(BigInteger.valueOf(Integer.MIN_VALUE));
                ((PropertyIntegerDefinitionImpl) result).setMaxValue(BigInteger.valueOf(Integer.MAX_VALUE));
            }
            if (propDef.getDataType().getName().equals(DataTypeDefinition.LONG))
            {
                ((PropertyIntegerDefinitionImpl) result).setMinValue(BigInteger.valueOf(Long.MIN_VALUE));
                ((PropertyIntegerDefinitionImpl) result).setMaxValue(BigInteger.valueOf(Long.MAX_VALUE));
            }
        }

        for (ConstraintDefinition constraintDef : propDef.getConstraints())
        {
            Constraint constraint = constraintDef.getConstraint();
            if (constraint instanceof ListOfValuesConstraint)
            {
                addChoiceList((ListOfValuesConstraint) constraint, result);
            }

            if ((constraint instanceof StringLengthConstraint) && (result instanceof PropertyStringDefinitionImpl))
            {
                StringLengthConstraint slc = (StringLengthConstraint) constraint;
                ((PropertyStringDefinitionImpl) result).setMaxLength(BigInteger.valueOf(slc.getMaxLength()));
            }

            if (constraint instanceof NumericRangeConstraint)
            {
                NumericRangeConstraint nrc = (NumericRangeConstraint) constraint;
                if (result instanceof PropertyIntegerDefinitionImpl)
                {
                    ((PropertyIntegerDefinitionImpl) result)
                            .setMinValue(BigInteger.valueOf(((Double) nrc.getMinValue()).longValue()));
                    ((PropertyIntegerDefinitionImpl) result)
                            .setMaxValue(BigInteger.valueOf(((Double) nrc.getMaxValue()).longValue()));
                }
                if (result instanceof PropertyDecimalDefinitionImpl)
                {
                    ((PropertyDecimalDefinitionImpl) result).setMinValue(BigDecimal.valueOf(nrc.getMinValue()));
                    ((PropertyDecimalDefinitionImpl) result).setMaxValue(BigDecimal.valueOf(nrc.getMaxValue()));
                }
            }
        }

        return result;
    }

    /**
     * Adds choices to the property defintion.
     */
    private void addChoiceList(ListOfValuesConstraint lovc, PropertyDefinition<?> propDef)
    {
        if (propDef instanceof PropertyBooleanDefinitionImpl)
        {
            PropertyBooleanDefinitionImpl propDefImpl = (PropertyBooleanDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<Boolean>> choiceList = new ArrayList<Choice<Boolean>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<Boolean> choice = new ChoiceImpl<Boolean>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((Boolean) convertValueFromString(allowed,
                        PropertyType.BOOLEAN)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyDateTimeDefinitionImpl)
        {
            PropertyDateTimeDefinitionImpl propDefImpl = (PropertyDateTimeDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<GregorianCalendar>> choiceList = new ArrayList<Choice<GregorianCalendar>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<GregorianCalendar> choice = new ChoiceImpl<GregorianCalendar>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((GregorianCalendar) convertValueFromString(allowed,
                        PropertyType.DATETIME)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyDecimalDefinitionImpl)
        {
            PropertyDecimalDefinitionImpl propDefImpl = (PropertyDecimalDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<BigDecimal>> choiceList = new ArrayList<Choice<BigDecimal>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<BigDecimal> choice = new ChoiceImpl<BigDecimal>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((BigDecimal) convertValueFromString(allowed,
                        PropertyType.DECIMAL)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyHtmlDefinitionImpl)
        {
            PropertyHtmlDefinitionImpl propDefImpl = (PropertyHtmlDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<String>> choiceList = new ArrayList<Choice<String>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<String> choice = new ChoiceImpl<String>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((String) convertValueFromString(allowed, PropertyType.HTML)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyIdDefinitionImpl)
        {
            PropertyIdDefinitionImpl propDefImpl = (PropertyIdDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<String>> choiceList = new ArrayList<Choice<String>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<String> choice = new ChoiceImpl<String>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((String) convertValueFromString(allowed, PropertyType.ID)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyIntegerDefinitionImpl)
        {
            PropertyIntegerDefinitionImpl propDefImpl = (PropertyIntegerDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<BigInteger>> choiceList = new ArrayList<Choice<BigInteger>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<BigInteger> choice = new ChoiceImpl<BigInteger>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((BigInteger) convertValueFromString(allowed,
                        PropertyType.INTEGER)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyStringDefinitionImpl)
        {
            PropertyStringDefinitionImpl propDefImpl = (PropertyStringDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<String>> choiceList = new ArrayList<Choice<String>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<String> choice = new ChoiceImpl<String>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections
                        .singletonList((String) convertValueFromString(allowed, PropertyType.STRING)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyUriDefinitionImpl)
        {
            PropertyUriDefinitionImpl propDefImpl = (PropertyUriDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<String>> choiceList = new ArrayList<Choice<String>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<String> choice = new ChoiceImpl<String>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((String) convertValueFromString(allowed, PropertyType.URI)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        }
    }

    private <T> T convertValueFromString(String value, PropertyType datatype)
    {
        if (value == null)
        {
            return null;
        }

        try
        {
            switch (datatype)
            {
                case BOOLEAN:
                    return (T) Boolean.valueOf(value);
                case DATETIME:
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(ISO8601DateFormat.parse(value));
                    return (T) cal;
                case DECIMAL:
                    return (T) new BigDecimal(value);
                case HTML:
                    return (T) value;
                case ID:
                    return (T) value;
                case INTEGER:
                    return (T) new BigInteger(value);
                case STRING:
                    return (T) value;
                case URI:
                    return (T) value;
                default: ;
            }
        }
        catch (Exception e)
        {
            //logger.error("Failed to convert value " + value + " to " + datatype, e);
            return null;
        }

        throw new RuntimeException("Unknown datatype! Spec change?");
    }
}
