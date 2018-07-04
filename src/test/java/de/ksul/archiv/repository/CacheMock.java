package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.runtime.cache.Cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 10:45
 */
public class CacheMock {

    private static Cache cache;

    public CacheMock() {
        if (cache == null)
            cache = getMock();
    }

    public Cache getCache() {
        return cache;
    }

    private Cache getMock() {

        Cache cache = mock(Cache.class);
        when(cache.containsId(any(), any())).thenReturn(false);
        when(cache.containsPath(any(), any())).thenReturn(false);
        when((cache.getById(any(), any()))).thenReturn(null);
        when(cache.getByPath(any(), any())).thenReturn(null);
        return cache;
    }
}
