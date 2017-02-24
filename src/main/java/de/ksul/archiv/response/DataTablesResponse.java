package de.ksul.archiv.response;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 18.11.16
 * Time: 22:42
 */
public class DataTablesResponse<T> extends RestResponse {
    /**
     * The draw counter that this object is a response to - from the draw parameter sent as part of the data request. Note that it is strongly recommended for
     * security reasons that you cast this parameter to an integer, rather than simply echoing back to the client what it sent in the draw parameter, in order
     * to prevent Cross Site Scripting (XSS) attacks.
     */
    private int draw;

    /**
     * Total records, before filtering (i.e. the total number of records in the database)
     * <p/>
     * (NB: I changed this to long)
     */
    private long recordsTotal;

    /**
     * Total records, after filtering (i.e. the total number of records after filtering has been applied - not just the number of records being returned for this
     * page of data).
     * <p/>
     * (NB: I changed this to long)
     */
    private long recordsFiltered;

    private boolean moreItems;

    private String parent;

    public DataTablesResponse() {}


    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public long getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(long recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public long getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public boolean hasMoreItems() {
        return moreItems;
    }

    public void setMoreItems(boolean moreItems) {
        this.moreItems = moreItems;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}

