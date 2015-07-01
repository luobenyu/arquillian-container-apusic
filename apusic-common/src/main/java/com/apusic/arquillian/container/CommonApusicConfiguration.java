package com.apusic.arquillian.container;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

/**
 * @author Patrick Huang
 */
public class CommonApusicConfiguration implements ContainerConfiguration{

    private String apusic_home;
    private String host= "localhost";
    private int port=6888;
    private String user;
    private String password;

    public void validate() throws ConfigurationException {
        if (user==null || user.equals(""))
            throw new ConfigurationException("User name cannot be empty");
        if (password==null || password.equals(""))
            throw new ConfigurationException("Password cannot be empty");
    }

    public String getSeverString() {
        return "iiop://"+host+":"+port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
