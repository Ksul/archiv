package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 11:08
 */
public class RepositoryInfoMock {

    private RepositoryInfo repositoryInfo;
    private Repository repository;

    public RepositoryInfoMock() {
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public RepositoryInfo getRepositoryInfo() {
        if (repositoryInfo == null)
            repositoryInfo = getMock();
        return repositoryInfo;
    }

    private RepositoryInfo getMock(){

        RepositoryInfo info = mock(RepositoryInfo.class);
        when(info.getId()).thenReturn("0");
        when(info.getRootFolderId()).thenAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                  return repository.getRootId();
            }
        });
        return info;
    }
}
