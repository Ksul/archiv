package de.ksul.archiv.model;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 22.11.16
 * Time: 18:23
 */
public interface Response {

    public boolean isSuccess();

    public void setSuccess(boolean success);

    public Exception getError();

    public void setError(Exception error);

    public boolean hasError();

    public Object getData();

    public void setData(Object data);
}
