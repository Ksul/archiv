package de.ksul.archiv.response;

import de.ksul.archiv.model.Response;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 17.11.16
 * Time: 16:24
 */
public class RestResponse implements Response {

    private boolean success;

    private Throwable error;

    private Object data;


    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public Throwable getError() {
        return error;
    }

    @Override
    public void setError(Throwable error) {
        this.error = error;
    }

    @Override
    public boolean hasError() {
        return error != null;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }
}
