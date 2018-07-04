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

    private static CmisBinding binding;

    public CmisBindingMock(Repository repository, SessionImpl sessionImpl) {
        if (binding == null)
            binding = getMock(repository, sessionImpl);
    }

    public CmisBinding getBinding() {
        return binding;
    }

    private CmisBinding getMock(Repository repository, SessionImpl sessionImpl) {
        ObjectService objectService =  new ObjectServiceMock(repository, sessionImpl).getObjectService();
        NavigationService navigationService = new NavigationServiceMock(repository).getNavigationService();
        DiscoveryService discoveryService = new DiscoveryServiceMock(repository, sessionImpl).getDiscoveryService();
        VersioningService versioningService = new VersionServiceMock(repository).getVersioningService();
        CmisBinding binding = mock(CmisBinding.class);
        when(binding.getObjectFactory()).thenReturn(new BindingsObjectFactoryImpl());
        when(binding.getObjectService()).thenReturn(objectService);
        when(binding.getNavigationService()).thenReturn(navigationService);
        when(binding.getDiscoveryService()).thenReturn(discoveryService);
        when(binding.getVersioningService()).thenReturn(versioningService);
        return binding;
    }
}
