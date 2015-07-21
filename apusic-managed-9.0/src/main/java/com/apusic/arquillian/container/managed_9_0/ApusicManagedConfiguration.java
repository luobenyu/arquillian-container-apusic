package com.apusic.arquillian.container.managed_9_0;

import com.apusic.arquillian.container.CommonApusicConfiguration;

/**
 * @author Patrick Huang
 */
public class ApusicManagedConfiguration extends CommonApusicConfiguration {
    private String domain = "mydomain";// 默认部署的domain

    private boolean useCMD = true;

    private String testPage ="index.jsp";

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

    public String getTestPage() {
        return testPage;
    }

    public void setTestPage(String testPage) {
        this.testPage = testPage;
    }

}
