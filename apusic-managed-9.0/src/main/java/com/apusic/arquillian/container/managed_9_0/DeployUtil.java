/*
 * $Id: DeployUtil.java,v 1.4 2008/09/16 08:52:12 zhengdele Exp $
 *
 * Copyright (c) 2000-2008 Apusic Systems, Inc.
 * All rights reserved.
 * 
 * Created by navy on 2008-5-14
 */

package com.apusic.arquillian.container.managed_9_0;

import com.apusic.tools.appctl.Main;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ResourceBundle;

/**
 * deploy application
 *
 * @author zhengdl
 */
public class DeployUtil {
    private static final Logger log = Logger.getLogger(DeployUtil.class.getName());
    private static final ResourceBundle bundle = ResourceBundle.getBundle(DeployUtil.class.getPackage().getName() + ".LocalStrings");

    /**
     * @param app
     */
    public static void deploy(String serverUrl, File app) {
        com.apusic.tools.appctl.Main appctl = new Main(System.out);
        try {
            if (log.isInfoEnabled())
                log.info(bundle.getString("DEPLOY_APPLICATION") + app.getAbsolutePath());
            appctl.run(new String[]{"-s", serverUrl, "-p", "admin", "install", app.getName(), app.getAbsolutePath()});
        } catch (Exception e) {
            log.error(bundle.getString("DEPLOY_FAIL") + app.getName(), e);
        }
    }

    /**
     * @param appFullPath
     */
    public static void deploy(String serverUrl, String appFullPath) {
        deploy(serverUrl, new File(appFullPath));
    }

    /**
     * @param appName
     */
    public static void undeploy(String appName) {
        com.apusic.tools.appctl.Main appctl = new Main(System.out);
        if (log.isInfoEnabled())
            log.info(bundle.getString("UNDEPLOY_APPLICATION") + appName);
        appctl.run(new String[]{"-p", "admin", "uninstall", appName});
    }
}
