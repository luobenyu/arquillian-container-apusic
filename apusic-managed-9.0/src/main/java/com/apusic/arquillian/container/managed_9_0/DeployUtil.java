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

/**
 * 部署应用或单个JSP
 * @author zhengdl
 *
 */
public class DeployUtil {
	private static final Logger log = Logger.getLogger(DeployUtil.class);
	/**
	 * 
	 * @param app
	 */
    public static void deploy(String serverUrl, File app) {
        com.apusic.tools.appctl.Main appctl = new Main(System.out);
        try {
        	log.info("部署应用:" + app.getAbsolutePath());
            appctl.run(new String[]{"-s", serverUrl, "-p", "admin", "install", app.getName(), app.getAbsolutePath()});
		} catch (Exception e) {
			log.error("部署失败：" + app.getName(), e);
		}
    }
    /**
     * 
     * @param appFullPath
     */
    public static void deploy(String serverUrl, String appFullPath) {
        deploy(serverUrl, new File(appFullPath));
    }
    /**
     * 
     * @param appName
     */
    public static void undeploy(String appName) {
        com.apusic.tools.appctl.Main appctl = new Main(System.out);
    	log.info("卸载应用:" + appName);
        appctl.run(new String[]{"-p", "admin", "uninstall", appName});
    }
}
