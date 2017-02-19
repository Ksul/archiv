package de.schulte.archiv.model;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 18.11.16
 * Time: 22:38
 */
public class Order {

    /**
     * Column to which ordering should be applied. This is an index reference to the columns array of information that is also submitted to the server.
     */
    private int column;


    /**
     * Ordering dir for this column. It will be <code>asc</code> or <code>desc</code> to indicate ascending ordering or descending ordering,
     * respectively.
     */
    private String dir;

    public Order(){}

    public Order(int column, String dir) {
        this.column = column;
        this.dir = dir;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
}
