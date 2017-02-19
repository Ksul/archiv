package de.schulte.archiv.request;

import de.schulte.archiv.model.Connection;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 22.11.16
 * Time: 18:38
 */
public class ConnectionRequest implements Connection {

    private String server;
    private String binding;
    private String user;
    private String password;
    private int timeout;

    @Override
    public String getServer() {
        return server;
    }

    @Override
    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public String getBinding() {
        return binding;
    }

    @Override
    public void setBinding(String binding) {
        this.binding = binding;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
