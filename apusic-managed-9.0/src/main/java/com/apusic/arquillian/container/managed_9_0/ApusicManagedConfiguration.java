package com.apusic.arquillian.container.managed_9_0;

import com.apusic.arquillian.container.CommonApusicConfiguration;

/**
 * @author Patrick Huang
 */
public class ApusicManagedConfiguration extends CommonApusicConfiguration {
    private String domain = "mydomain";// 默认部署的domain

    private boolean useCMD = true;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isUseCMD() {
        return useCMD;
    }

    public void setUseCMD(boolean useCMD) {
        this.useCMD = useCMD;
    }
}
