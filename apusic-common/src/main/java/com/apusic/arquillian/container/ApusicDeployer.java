package com.apusic.arquillian.container;

import com.apusic.deploy.runtime.J2EEDeployer;
import com.apusic.deploy.runtime.J2EEDeployerMBean;
import com.apusic.jmx.MBeanProxy;
import com.apusic.jmx.adaptors.rmi.JNDINames;
import com.apusic.util.RemoteBufferImpl;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.j2ee.Management;
import javax.management.j2ee.ManagementHome;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Patrick Huang
 */
public class ApusicDeployer {
    private static Logger log= Logger.getLogger(ApusicDeployer.class.getName());
    private CommonApusicConfiguration configuration;
    private JMXConnector connector;
    private MBeanServerConnection mbeanServer;
    private J2EEDeployerMBean deployerMBean;
    private boolean connected;

    public ApusicDeployer(CommonApusicConfiguration configuration) {
        this.configuration= configuration;
    }

    public void connect() throws Exception {
        //LogManager.getLogManager().readConfiguration();
        Map<String, String> props= new HashMap<String, String>();
        props.put(Context.SECURITY_PRINCIPAL, configuration.getUser());
        props.put(Context.SECURITY_CREDENTIALS, configuration.getPassword());
        JMXServiceURL serviceURL = new JMXServiceURL("iiop", configuration.getHost(),
                configuration.getPort(), "/jndi/"+ JNDINames.CONNECTOR_SERVER_JNDI_NAME);
        log.info("Connecting to " + serviceURL);
        connector= JMXConnectorFactory.connect(serviceURL, props);
        mbeanServer= connector.getMBeanServerConnection();

        deployerMBean= getJ2EEDeployer(configuration);

        connected=true;
    }


    public void close() throws LifecycleException{
        connected= false;
        try {
            connector.close();
        }catch (Exception e) {}
        mbeanServer= null;

        deployerMBean= null;
    }

    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        if (!connected)
            throw new IllegalStateException("Deployer not connected");

        String deploymentName = getDeploymentName(archive);
        File deploymentArchive = toFile(archive);
        RemoteBufferImpl archiveData= null;
        ProtocolMetaData pmd= new ProtocolMetaData();

        try {
            archiveData = new RemoteBufferImpl(deploymentArchive);
            //TODO: virtual host
                    ObjectName appName= deployerMBean.deploy(deploymentName, archiveData, null, deploymentName, null);
            String moduleType= (String)mbeanServer.getAttribute(appName, "ModuleTypeString");
            if (moduleType.equals("war")) {
                pmd.addContext(buildContext(deploymentName, appName));
            }else {  //TODO: ear
                throw new UnsupportedOperationException("File other than war has not been supported yet.");
            }
        }catch (Exception e) {
             throw new DeploymentException("Deploying error", e);
        }finally {
            if (archiveData!=null) {
                try {
                    archiveData.close();
                }catch (IOException ioe) {}
            }
        }

        if (log.isLoggable(Level.FINE))
            log.fine("Got PMD of deployed " + archive.getName() + ": " + pmd);
        return pmd;
    }

    public void undeploy(Archive<?> archive) throws DeploymentException {
        if (!connected)
            throw new IllegalStateException("Deployer not connected");
        try {
            deployerMBean.undeploy(getDeploymentName(archive));
        }catch (Exception e) {
            throw new DeploymentException("Undeploy error: "+e.getMessage());
        }
    }


    private String getDeploymentName(Archive<?> archive) {
        String archiveFilename = archive.getName();
        int indexOfDot = archiveFilename.indexOf(".");
        if (indexOfDot != -1) {
            return archiveFilename.substring(0, indexOfDot);
        }
        return archiveFilename;
    }

    private  File toFile(final Archive<?> archive)
    {
        try
        {
            File root = File.createTempFile("arquillian_apusic", archive.getName());
            root.delete();
            root.mkdirs();

            File deployment = new File(root, archive.getName());
            deployment.deleteOnExit();
            archive.as(ZipExporter.class).exportTo(deployment, true);
            return deployment;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not export deployment to temp", e);
        }
    }

    private HTTPContext buildContext(String name, ObjectName appName) throws Exception{
        HTTPContext httpContext= new HTTPContext(name, configuration.getHost(), configuration.getPort());

        String[] webModules= (String[])mbeanServer.getAttribute(appName, "Modules");
        ObjectName webModule= new ObjectName(webModules[0]);

        //RECHECK: contextRoot and realContextRoot
        //String contextRoot= (String)mbeanServer.getAttribute(webModule, "ContextRoot");
        String realContextRoot=(String)mbeanServer.getAttribute(webModule, "RealContextRoot");
        String[] servlets= (String[])mbeanServer.getAttribute(webModule, "Servlets");
        for (String servlet: servlets) {
            httpContext.add(new Servlet(servlet, realContextRoot));
        }
        return httpContext;
    }



    private Management getMEJB(CommonApusicConfiguration configuration) throws Exception {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.apusic.naming.jndi.CNContextFactory");
        env.put(Context.PROVIDER_URL, configuration.getSeverString());
        env.put(Context.SECURITY_PRINCIPAL, configuration.getUser());
        env.put(Context.SECURITY_CREDENTIALS, configuration.getPassword());

        Context context = new InitialContext(env);
        ManagementHome home = (ManagementHome) javax.rmi.PortableRemoteObject.narrow(context.lookup("ejb/mgmt/MEJB"), ManagementHome.class);
        return home.create();
    }

    private J2EEDeployerMBean getJ2EEDeployer(CommonApusicConfiguration configuration) throws Exception{
        Management mejb= getMEJB(configuration);
        return (J2EEDeployerMBean) MBeanProxy.create(J2EEDeployerMBean.class, mejb, J2EEDeployer.OBJECT_NAME);
    }
}
