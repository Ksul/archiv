package de.ksul.archiv.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.ksul.archiv.model.Column;
import de.ksul.archiv.model.Order;
import de.ksul.archiv.model.Search;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 18.11.16
 * Time: 21:50
 */
public class DataTablesRequest  {

    private String folderId;

    /**
     * mögliche Werte: -1 nur Folder  0: beides  1: nur Dokumente
     */
    @Min(-1)
    @Max(1)
    private int withFolder;

    private String cmisQuery;

    /**
     * Draw counter. This is used by DataTables to ensure that the Ajax returns from server-side processing requests are drawn in sequence by DataTables
     * (Ajax requests are asynchronous and thus can return out of sequence). This is used as part of the draw return parameter (see below).
     */
    @Min(0)
    private int draw;

    /**
     * Paging first record indicator. This is the start point in the current data set (0 index based - i.e. 0 is the first record).
     */
    @Min(0)
    private int start;

    /**
     * Number of records that the table can display in the current draw. It is expected that the number of records returned will be equal to this number, unless
     * the server has fewer records to return. Note that this can be -1 to indicate that all records should be returned (although that negates any benefits of
     * server-side processing!)
     */
    @Min(-1)
    private int length;

    /**
     * @see Search
     */
    @JsonProperty("search")
    private Search search;

    /**
     * @see Order
     */
    @JsonProperty("order")
    private List<Order> orders;

    /**
     * @see Column
     */
    @JsonProperty("columns")
    private List<Column> columns;


    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public int getWithFolder() {
        return withFolder;
    }

    public void setWithFolder(int withFolder) {
        this.withFolder = withFolder;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    /**
     * Gets the Paging first record indicator. This is the start point in the current data set (0 index based - i.e. 0 is the first record).
     * @return int the indicator
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets the start point in the current data set (0 index based - i.e. 0 is the first record).
     * @param start  start point
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Gets the number of records that the table can display in the current draw. It is expected that the number of records returned will be equal to this number, unless
     * the server has fewer records to return. Note that this can be -1 to indicate that all records should be returned (although that negates any benefits of
     * server-side processing!)
     * @return int length
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets the number of records that the table can display in the current draw. It is expected that the number of records returned will be equal to this number, unless
     * the server has fewer records to return. Note that this can be -1 to indicate that all records should be returned (although that negates any benefits of
     * server-side processing!)
     * @param length  the length
     */
    public void setLength(int length) {
        this.length = length;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public String getCmisQuery() {
        return cmisQuery;
    }

    public void setCmisQuery(String cmisQuery) {
        this.cmisQuery = cmisQuery;
    }


}







