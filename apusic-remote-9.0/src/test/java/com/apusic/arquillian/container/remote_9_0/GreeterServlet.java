package com.apusic.arquillian.container.remote_9_0;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Patrick Huang on 15-5-21.
 */
public class GreeterServlet extends HttpServlet{
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        resp.getOutputStream().print("OK");
    }
}
