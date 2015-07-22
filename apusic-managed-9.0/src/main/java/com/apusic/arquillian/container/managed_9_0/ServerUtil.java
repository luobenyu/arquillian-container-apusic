package com.apusic.arquillian.container.managed_9_0;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Chmod;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Including start and stop  server
 *
 * @author zhengdl
 */
public class ServerUtil {
    private static final Logger log = Logger.getLogger(ServerUtil.class.getName());

    private static final Map<ApusicManagedConfiguration, Object[]> serverIOMap = new HashMap<ApusicManagedConfiguration, Object[]>();

    private static final ResourceBundle bundle =ResourceBundle.getBundle(ServerUtil.class.getPackage().getName() + ".LocalStrings");

    /**
     * Startup Apusic Server
     *
     * @param as
     */
    public static void startup(ApusicManagedConfiguration as) {
        if (serverIOMap.containsKey(as)) {
            throw new RuntimeException(as + " " + bundle.getString("SERVER_ALREADY_STARTUP"));
        }
        String apusicpath = null;
        StringBuilder sb = new StringBuilder().append(as.getApusic_home()).append(File.separatorChar)
                .append("domains").append(File.separatorChar)
                .append(as.getDomain()).append(File.separatorChar)
                .append("bin");
        Process p = null;
        try {
            if (checkForWindows()) {
                if (as.isUseCMD()) {
                    apusicpath = sb.append(File.separatorChar).append("startapusic.cmd").toString();
                } else {
                    apusicpath = sb.append(File.separatorChar).append("apusic.exe -Xms512m -Xmx1024m -XX:MaxPermSize=512m").toString();
                }
            } else {
                apusicpath = sb.append(File.separatorChar).append("startapusic").toString();
                new File(apusicpath).setExecutable(true);
            }
            log.info(bundle.getString("STARTING_SERVER") + apusicpath);
            String[] envp = null;
            if (as.getConfigFile() != null) {
                File configFile = new File(as.getConfigFile());
                if (!configFile.exists())
                    throw new IllegalArgumentException(bundle.getString("CONFIG_FILE_NOT_EXIT") + as.getConfigFile());
                Map<String,String> envs = System.getenv();
                List<String> envsList = new ArrayList<String>();
                for(String key : envs.keySet()){
                    envsList.add(key + "=" + envs.get(key));
                }
                envsList.add("apusic.config=" + as.getConfigFile());
                envp = envsList.toArray(new String[0]);
            }
            p = Runtime.getRuntime().exec(apusicpath, envp);
            InputStream out = p.getInputStream();
            InputStream err = p.getErrorStream();
            StreamPumper outPumper = new StreamPumper(out, System.out);
            StreamPumper errPumper = new StreamPumper(err, System.err);
            outPumper.start();
            errPumper.start();
            serverIOMap.put(as, new Object[]{p, outPumper, errPumper});
            // waiting 2 minutes or server is started
            int i = 120;
            while (i > 0 ) {
                if(isServerStarted(as))
                    break;
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                i--;
            }
            if (i > 0) {
                log.info(bundle.getString("SERVER_IS_READY"));
            } else if (i <= 0) {
                log.warn(bundle.getString("STARTUP_SERVER_NOT_RESPONSE"));
                throw new Exception();
            }
        } catch (Exception e) {
            log.error(bundle.getString("FAIL_TO_START_SERVER") + apusicpath + ":" + e.getMessage(), e);
            if(p != null){
                p.destroy();
            }
            throw new RuntimeException(bundle.getString("FAIL_TO_START_SERVER") , e);
        }

    }

    private static boolean isServerStarted(ApusicManagedConfiguration as) throws MalformedURLException {
        String testPage = as.getTestPage();
        if(!testPage.startsWith("/"))
            testPage = "/" + testPage;
        URL testURl  = new URL("http",as.getHost(),as.getPort(),as.getTestPage());

        HttpURLConnection connection= null;
        try {
            connection = (HttpURLConnection)testURl.openConnection();
            connection.getResponseCode();
            return true;
        } catch (IOException e) {
           return  false;
        }
    }

    private static boolean isServerStopped(ApusicManagedConfiguration as) {
        SocketAddress host = new InetSocketAddress(as.getHost(),as.getPort());
        Socket socket = new Socket();
        try {
            socket.connect(host);
        } catch (IOException e) {
            return  true;
        }
        return false;
    }

    /**
     * 停止运行的server
     *
     * @param as
     */
    public static void shutdown(ApusicManagedConfiguration as) {
        Process serverProcess = null;
        StreamPumper startServerOutPumper = null;
        StreamPumper startServerErrPumper = null;
        if (serverIOMap.containsKey(as)) {
            Object[] objs = serverIOMap.get(as);
            serverIOMap.remove(as);
            serverProcess = (Process) objs[0];
            startServerOutPumper = (StreamPumper) objs[1];
            startServerErrPumper = (StreamPumper) objs[2];
        }
        String apusicpath = null;
        StringBuilder sb = new StringBuilder().append(as.getApusic_home()).append(File.separatorChar).append("domains").append(File.separatorChar).append(as.getDomain()).append(File.separatorChar).append("bin");
        try {
            if (checkForWindows()) {
                apusicpath = sb.append(File.separatorChar).append("stopapusic.cmd ").append(as.getUser()).append(" ").append(as.getPassword()).append(" iiop://").append(as.getHost()).append(":").append(as.getPort()).toString();
            } else {
                apusicpath = sb.append(File.separatorChar).append("stopapusic").toString();
                Project pro = new Project();
                pro.setName("apusicChmod");
                Chmod mod = new Chmod();
                mod.setPerm("+x");
                mod.setDir(new File(apusicpath));
                mod.setProject(pro);
                mod.execute();
                apusicpath = sb.append(" ").append(as.getUser()).append(" ").append(as.getPassword()).append(" iiop://").append(as.getHost()).append(":").append(as.getPort()).toString();
            }
            log.info(bundle.getString("SHUTTING_DOWN_SERVER")  + apusicpath);
            Process p = Runtime.getRuntime().exec(apusicpath);
            InputStream out = p.getInputStream();
            InputStream err = p.getErrorStream();
            StreamPumper outPumper = new StreamPumper(out, System.out);
            StreamPumper errPumper = new StreamPumper(err, System.err);
            outPumper.start();
            errPumper.start();
            // Wait 2 Min
            int i = 120;
            while (i > 0) {
                if (isServerStopped(as)) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                i--;
            }
            if (i > 0) {
                log.info(bundle.getString("SERVER_IS_SHUTDOWN") );
            } else if (i <= 0) {
                log.warn(bundle.getString("SHUTDOWN_SERVER_NOT_RESPONSE") );
                if (serverProcess != null) {
                    log.info(bundle.getString("FORCE_TO_SHUTDOWN"));
                    serverProcess.destroy();
                    serverProcess.waitFor();
                }
            }
        } catch (Exception e) {
            log.error(bundle.getString("FAIL_TO_SHUTDOWN_SERVER"), e);
            throw new RuntimeException(bundle.getString("FAIL_TO_SHUTDOWN_SERVER") + apusicpath, e);
        }
    }


    public static boolean checkForWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().startsWith("win");
    }

    private static class StreamPumper extends Thread {
        private InputStream in;

        private OutputStream out;

        StreamPumper(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        public void run() {
            BufferedReader br = null;
            BufferedWriter bw = null;
            try {
                String encoding = System.getProperty("file.encoding");
                br = new BufferedReader(new InputStreamReader(in,encoding));
                bw = new BufferedWriter(new OutputStreamWriter(out,encoding));
                String readLine = null;
                while ((readLine=br.readLine()) != null) {
                    bw.write(readLine);
                    bw.newLine();
                    bw.flush();
                }
            } catch (IOException e) {
            }
        }
    }

}
