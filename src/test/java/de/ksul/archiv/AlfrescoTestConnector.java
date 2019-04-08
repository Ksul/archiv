package de.ksul.archiv;

import de.ksul.archiv.model.comments.CommentPaging;
import de.ksul.archiv.repository.Repository;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.http.auth.AuthenticationException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 02.10.18
 * Time: 14:22
 */
public class AlfrescoTestConnector extends AlfrescoConnector{

    public AlfrescoTestConnector(Session session, String server, String binding, String user, String password, String companyHomeName, String dataDictionaryName, String scriptFolderName) {
        super(session, server, binding, user, password, companyHomeName, dataDictionaryName, scriptFolderName);
    }

    @Override
    public CommentPaging getComments(CmisObject obj) {
        Repository repository = Repository.getInstance();
        return repository.getComments(obj.getId());
    }

    @Override
    public Map addComment(CmisObject obj, String comment) throws IOException, AuthenticationException, URISyntaxException {
        return super.addComment(obj, comment);
    }
}
