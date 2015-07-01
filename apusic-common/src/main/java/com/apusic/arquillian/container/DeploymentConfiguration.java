package com.apusic.arquillian.container;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick Huang
 */
public class DeploymentConfiguration {
    private Map<String, ArchiveDeployConfiguration> configs= new HashMap<String, ArchiveDeployConfiguration>();

    public void addConfiguration(ArchiveDeployConfiguration config) {
        configs.put(config.getName(), config);
    }

    public ArchiveDeployConfiguration getConfiguration(String name) {
        return  configs.get(name);
    }
}
