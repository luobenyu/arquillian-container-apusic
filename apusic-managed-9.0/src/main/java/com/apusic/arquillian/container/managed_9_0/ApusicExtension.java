package com.apusic.arquillian.container.managed_9_0;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * Arquillian adapter for Apusic 9.0
 * @author Patrick Huang
 */
public class ApusicExtension implements LoadableExtension{
    public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, ApusicContainer.class);
        builder.observer(DeploymentObserver.class);
    }
}
