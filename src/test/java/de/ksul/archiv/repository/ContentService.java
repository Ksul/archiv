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

    private TreeMap<UUID, ContentStream> contents = new TreeMap<>();
    private TreeMap<String, UUID> ids = new TreeMap<>();

    public UUID createContent( String id, ContentStream content, boolean overwrite) {
        if (ids.containsKey(id) && !overwrite)
            throw new CmisContentAlreadyExistsException();
        UUID uuid = UUID.randomUUID();
        contents.put(uuid, content);
        ids.put(id, uuid);
        return uuid;
    }

    public ContentStream getContent(UUID uuid) {
        if (contents.containsKey(uuid))
            return contents.get(uuid);
        else
            return null;
    }

    public ContentStream getContent(String id) {
        if (ids.containsKey(id))
            return getContent(ids.get(id));
        else
            return null;
    }

    public boolean containsContent(String id) {
        return id.contains(id);
    }



}
