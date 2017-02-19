package de.schulte.archiv.repository;

import org.apache.chemistry.opencmis.client.api.Session;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 12/4/16
 * Time: 3:00 PM
 */
public interface CMISSessionGenerator {
    Session generateSession();
}
