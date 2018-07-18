package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.runtime.SessionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.spi.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 10:40
 */
public class CmisBindingMock {

    private ObjectServiceMock objectServiceMock;
    private DiscoveryServiceMock discoveryServiceMock;
    private NavigationServiceMock navigationServiceMock;
    private VersionServiceMock versionServiceMock;
    private RepositoryServiceMock repositoryServiceMock;
    private CmisBinding binding;
    private Repository repository;
    private SessionImpl sessionImpl;

    public CmisBindingMock() {
        objectServiceMock = new ObjectServiceMock();
        discoveryServiceMock = new DiscoveryServiceMock();
        navigationServiceMock = new NavigationServiceMock();
        versionServiceMock = new VersionServiceMock();
        repositoryServiceMock = new RepositoryServiceMock();
    }

    public CmisBindingMock setSessionImpl(SessionImpl sessionImpl) {
        this.sessionImpl = sessionImpl;
        discoveryServiceMock.setSession(sessionImpl);
        return this;
    }

    public CmisBindingMock setRepository(Repository repository) {
        this.repository = repository;
        discoveryServiceMock.setRepository(repository);
        objectServiceMock.setRepository(repository);
        navigationServiceMock.setRepository(repository);
        versionServiceMock.setRepository(repository);
        return this;
    }

    public CmisBinding getBinding() {
        if (binding == null)
            binding = getMock();
        return binding;
    }

    public CmisBinding getMock() {

        CmisBinding binding;
        ObjectService objectService = objectServiceMock.getObjectService();
        NavigationService navigationService = navigationServiceMock.getNavigationService();
        DiscoveryService discoveryService = discoveryServiceMock.getDiscoveryService();
        VersioningService versioningService = versionServiceMock.getVersioningService();
        RepositoryService repositoryService = repositoryServiceMock.getRepositoryService();
        binding = mock(CmisBinding.class);
        when(binding.getObjectFactory()).thenReturn(new BindingsObjectFactoryImpl());
        when(binding.getObjectService()).thenReturn(objectService);
        when(binding.getNavigationService()).thenReturn(navigationService);
        when(binding.getDiscoveryService()).thenReturn(discoveryService);
        when(binding.getVersioningService()).thenReturn(versioningService);
        when(binding.getRepositoryService()).thenReturn(repositoryService);
        return binding;
    }
}
