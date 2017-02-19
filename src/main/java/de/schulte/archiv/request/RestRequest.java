package de.schulte.archiv.request;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 09.11.16
 * Time: 15:16
 */
public class RestRequest {

    private String documentId;

    private String destinationId;

    private String currentLocationId;

    private String content;

    private String filePath;

    private String fileName;

    private String imageLink;

    private String cmisQuery;

    private String mimeType;



    private int withFolder;

    private boolean extract;

    private String versionState;

    private String versionComment;

    private String extraProperties;

    private String ticket;

    private String comment;

    private int length;





    //private String order[0][column];


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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getCmisQuery() {
        return cmisQuery;
    }

    public void setCmisQuery(String cmisQuery) {
        this.cmisQuery = cmisQuery;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getWithFolder() {
        return withFolder;
    }

    public void setWithFolder(int withFolder) {
        this.withFolder = withFolder;
    }

    public boolean getExtract() {
        return extract;
    }

    public void setExtract(boolean extract) {
        this.extract = extract;
    }

    public String getVersionState() {
        return versionState;
    }

    public void setVersionState(String versionState) {
        this.versionState = versionState;
    }

    public String getVersionComment() {
        return versionComment;
    }

    public void setVersionComment(String versionComment) {
        this.versionComment = versionComment;
    }

    public String getExtraProperties() {
        return extraProperties;
    }

    public void setExtraProperties(String extraProperties) {
        this.extraProperties = extraProperties;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

}
