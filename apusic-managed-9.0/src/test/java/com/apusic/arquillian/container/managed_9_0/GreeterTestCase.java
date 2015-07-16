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

//    @ArquillianResource()
//    private URL baseURL;

    @Deployment(testable = false,name = "dep1")
         @TargetsContainer("aas1")
         public static WebArchive archive1() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "greeter.war");
        war.addClasses(GreeterServlet.class);
        war.setWebXML("web.xml");
        return war;
    }

    @Deployment(testable = false,name = "dep2")
    @TargetsContainer("aas2")
    public static WebArchive archive2() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "greeter.war");
        war.addClasses(GreeterServlet.class);
        war.setWebXML("web.xml");
        return war;
    }

    @Test
    @OperateOnDeployment("dep1")
    public void testGet(@ArquillianResource() URL url1,
                        @ArquillianResource()
                        @OperateOnDeployment("dep2") URL url2) throws Exception{
        URL u1= new URL(url1.toExternalForm()+"/g");
        HttpURLConnection connection= (HttpURLConnection)u1.openConnection();
        int responseCode= connection.getResponseCode();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, responseCode);


        URL u2= new URL(url2.toExternalForm()+"/g");
        HttpURLConnection connection2= (HttpURLConnection)u2.openConnection();
        int responseCode2= connection2.getResponseCode();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, responseCode2);
    }

    @Test
    @OperateOnDeployment("dep2")
    @Ignore
    public void testGet(@ArquillianResource() URL baseURL) throws Exception{
        URL url= new URL(baseURL.toExternalForm()+"/g");
        HttpURLConnection connection= (HttpURLConnection)url.openConnection();
        int responseCode= connection.getResponseCode();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, responseCode);
    }
}
