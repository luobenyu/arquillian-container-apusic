package com.apusic.arquillian.container;

/**
 * @author  Patrick Huang
 */
public class ArchiveDeployConfiguration {
    public static final String DEFAULT_VIRTUALHOST="default";
    public static final String DEFAULT_STARTTYPE="auto";
    private String name;
    private String virtualHost;
    private String baseContext;
    private boolean globalSession;
    private String startType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getBaseContext() {
        return baseContext;
    }

    public void setBaseContext(String baseContext) {
        this.baseContext = baseContext;
    }

    public boolean isGlobalSession() {
        return globalSession;
    }

    public void setGlobalSession(boolean globalSession) {
        this.globalSession = globalSession;
    }

    public String getStartType() {
        return startType;
    }

    public void setStartType(String startType) {
        this.startType = startType;
    }


    @Override
    public String toString() {
        return "ArchiveDeployConfiguration{" +
                "name='" + name + '\'' +
                ", virtualHost='" + virtualHost + '\'' +
                ", baseContext='" + baseContext + '\'' +
                ", globalSession=" + globalSession +
                ", startType='" + startType + '\'' +
                '}';
    }

}
