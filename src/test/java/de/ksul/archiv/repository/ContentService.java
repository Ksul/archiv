package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;

import java.util.TreeMap;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 03.07.18
 * Time: 12:07
 */
public class ContentService {

    private TreeMap<String, ContentStream> contents = new TreeMap<>();
    private TreeMap<String, String> ids = new TreeMap<>();

    public String createContent( String id, ContentStream content, boolean overwrite) {
        if (ids.containsKey(id) && !overwrite)
            throw new CmisContentAlreadyExistsException();
        String uuid = UUID.randomUUID().toString();
        contents.put(uuid, content);
        ids.put(id, uuid);
        return uuid;
    }

    public ContentStream getContent(String uuid) {
        if (contents.containsKey(uuid))
            return contents.get(uuid);
        else
            return null;
    }

    public ContentStream getContentForObjectId(String objectId) {
        if (ids.containsKey(objectId))
            return getContent(ids.get(objectId));
        else
            return null;
    }

    public boolean containsContent(String id) {
        return id.contains(id);
    }



}
