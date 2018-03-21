package de.ksul.archiv.model;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 22.11.16
 * Time: 18:08
 */
public interface Connection {

    String getServer();

    void setServer(String server);

    String getBinding();

    void setBinding(String binding);

    String getUser();

    void setUser(String user);

    String getPassword();

    void setPassword(String password);

    int getTimeout();

    void setTimeout(int timeout);
}
