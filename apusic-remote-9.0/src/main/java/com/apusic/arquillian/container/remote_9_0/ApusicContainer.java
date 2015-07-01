package com.apusic.arquillian.container.remote_9_0;

import com.apusic.arquillian.container.ApusicDeployer;
import com.apusic.arquillian.container.ArchiveDeployConfiguration;
import com.apusic.arquillian.container.DeploymentConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;


/**
 * @author Patrick Huang
 */
public class ApusicContainer implements DeployableContainer<ApusicRemoteConfiguration>{

    @Inject
    private Instance<DeploymentConfiguration> deployConfig;
    private ApusicRemoteConfiguration configuration;
    private ApusicDeployer deployer;

    public Class<ApusicRemoteConfiguration> getConfigurationClass() {
        return ApusicRemoteConfiguration.class;
    }

    public void setup(ApusicRemoteConfiguration apusicRemoteConfiguration) {
        this.configuration= apusicRemoteConfiguration;
        this.deployer= new ApusicDeployer(configuration);
    }

    public void start() throws LifecycleException {
        try {
            deployer.connect();
        }catch (Exception e) {
            throw new LifecycleException("Deployer cannot connect to server: "+e.getMessage());
        }
    }

    public void stop() throws LifecycleException {
        deployer.close();
    }

    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 3.0");
    }

    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        DeploymentConfiguration dc= deployConfig.get();
        ArchiveDeployConfiguration adc= dc.getConfiguration(archive.getName());
        if (adc!=null) {
            String virtualHost= adc.getVirtualHost();
            if (virtualHost.equals(ArchiveDeployConfiguration.DEFAULT_VIRTUALHOST))
                virtualHost= null;
            return deployer.deploy(archive, virtualHost, adc.getBaseContext(), adc.getStartType(),
                    adc.isGlobalSession());
        }else
            return deployer.deploy(archive);
    }

    public void undeploy(Archive<?> archive) throws DeploymentException {
        deployer.undeploy(archive);
    }

    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

