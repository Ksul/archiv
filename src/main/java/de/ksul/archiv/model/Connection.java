package de.ksul.archiv.model;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 22.11.16
 * Time: 18:08
 */
public interface Connection {

    public String getServer();

    public void setServer(String server);

    public String getBinding();

    public void setBinding(String binding);

    public String getUser();

    public void setUser(String user);

    public String getPassword();

    public void setPassword(String password);

    public int getTimeout();

    public void setTimeout(int timeout);
}
