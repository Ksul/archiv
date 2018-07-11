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

    private static RepositoryInfo repositoryInfo;

    public RepositoryInfoMock(Repository repository) {
        if (repositoryInfo == null)
            repositoryInfo = getMock(repository);
    }

    public RepositoryInfo getRepositoryInfo() {
        return repositoryInfo;
    }

    private RepositoryInfo getMock(Repository repository){

        RepositoryInfo info = mock(RepositoryInfo.class);
        when(info.getId()).thenReturn("0");
        when(info.getRootFolderId()).thenAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                  return TreeNode.getRootId();
            }
        });
        return info;
    }
}
