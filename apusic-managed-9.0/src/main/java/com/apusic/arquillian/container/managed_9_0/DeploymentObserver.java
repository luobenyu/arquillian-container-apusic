package com.apusic.arquillian.container.managed_9_0;


import com.apusic.arquillian.annotation.ApusicDeployment;
import com.apusic.arquillian.container.ArchiveDeployConfiguration;
import com.apusic.arquillian.container.DeploymentConfiguration;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.impl.client.deployment.event.GenerateDeployment;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;

import java.lang.reflect.Method;

/**
 * @author Patrick Huang
 */
public class DeploymentObserver {

    @Inject
    @ClassScoped
    private InstanceProducer<DeploymentConfiguration> deployConfig;

    public void callback(@Observes GenerateDeployment event) {
        DeploymentConfiguration dc = deployConfig.get();
        if (dc == null) dc = new DeploymentConfiguration();

        Method[] methods= event.getTestClass().getMethods(Deployment.class);
        for (Method method: methods) {
            ApusicDeployment deployment = method.getAnnotation(ApusicDeployment.class);
            if (deployment != null) {
                ArchiveDeployConfiguration ac = new ArchiveDeployConfiguration();
                ac.setName(deployment.name());
                ac.setVirtualHost(deployment.virtualHost());
                ac.setStartType(deployment.startType());
                ac.setBaseContext(deployment.baseContext());
                ac.setGlobalSession(deployment.globalSession());
                dc.addConfiguration(ac);
            }
        }

        deployConfig.set(dc);
    }

}
