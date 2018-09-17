package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumBaseObjectTypeIds;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 18.07.18
 * Time: 17:33
 */
public class RepositoryServiceMock {

    RepositoryService repositoryService;

    public RepositoryServiceMock() {
    }

    public RepositoryService getRepositoryService() {
        if (repositoryService == null)
            repositoryService = getMock();
        return repositoryService;
    }

    private RepositoryService getMock() {

        RepositoryService repositoryService = mock(RepositoryService.class);
        when(repositoryService.getTypeDefinition(anyString(), anyString(), any())).then(new Answer<TypeDefinition>() {
            @Override
            public TypeDefinition answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String string = (String) args[1];
                if (string.equalsIgnoreCase(EnumBaseObjectTypeIds.CMIS_DOCUMENT.value()))
                    return MockUtils.getInstance().getDocumentType();
                else if (string.equalsIgnoreCase(EnumBaseObjectTypeIds.CMIS_FOLDER.value()))
                    return MockUtils.getInstance().getFolderType();
                else if (string.equalsIgnoreCase(EnumBaseObjectTypeIds.CMIS_ITEM.value()))
                    return MockUtils.getInstance().getItemType();
                else if (string.contains("my:archivContent"))
                    return MockUtils.getInstance().getArchivType();
                else if (string.startsWith("P:"))
                    return MockUtils.getInstance().getSecondaryType(string.substring(2));
                else
                    return MockUtils.getInstance().getDocumentType();
            }

        });
        return repositoryService;

    }
}
