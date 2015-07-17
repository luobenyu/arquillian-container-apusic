package com.apusic.arquillian.container.managed_9_0;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hxm on 15-5-21.
 * @author Patrick Huang
 */

@RunWith(Arquillian.class)
@RunAsClient
public class GreeterTestCase {

    @ArquillianResource
    private URL baseURL;

    @Deployment(testable = false)
    public static WebArchive archive() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "greeter.war");
        war.addClasses(GreeterServlet.class);
        war.setWebXML("web.xml");
        return war;
    }

    @Test
    public void testGet() throws Exception{
        URL url= new URL(baseURL.toExternalForm()+"/g");
        HttpURLConnection connection= (HttpURLConnection)url.openConnection();
        int responseCode= connection.getResponseCode();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, responseCode);
    }
}
