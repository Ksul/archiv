package de.ksul.archiv.model;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 22.11.16
 * Time: 18:23
 */
public interface Response {

    boolean isSuccess();

    void setSuccess(boolean success);

    Exception getError();

    void setError(Exception error);

    boolean hasError();

    Object getData();

    void setData(Object data);
}
