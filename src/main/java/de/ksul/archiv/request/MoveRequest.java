package de.ksul.archiv.request;

import javax.validation.constraints.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 9/19/17
 * Time: 4:45 PM
 */
public class MoveRequest {

    @NotNull
    private String documentId;

    @NotNull
    private String destinationId;

    @NotNull
    private String currentLocationId;

    public MoveRequest() {
    }

    public MoveRequest(String documentId, String destinationId, String currentLocationId) {
        this.documentId = documentId;
        this.destinationId = destinationId;
        this.currentLocationId = currentLocationId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getCurrentLocationId() {
        return currentLocationId;
    }

    public void setCurrentLocationId(String currentLocationId) {
        this.currentLocationId = currentLocationId;
    }
}
