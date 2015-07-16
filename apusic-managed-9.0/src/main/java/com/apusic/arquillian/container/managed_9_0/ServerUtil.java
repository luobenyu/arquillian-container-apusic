package com.apusic.arquillian.container.managed_9_0;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Chmod;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


/**
 * 包括启动服务器， 停止服务器
 *
 * @author zhengdl
 */
public class ServerUtil {
    private static final Logger log = Logger.getLogger(ServerUtil.class);

    private static final Map<ApusicManagedConfiguration, Object[]> serverIOMap = new HashMap<ApusicManagedConfiguration, Object[]>();

    /**
     * 启动Apusic Server
     *
     * @param as
     */
    public static void startup(ApusicManagedConfiguration as) {
        if (serverIOMap.containsKey(as)) {
            throw new RuntimeException(as + " 已经启动!!");
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
            log.info("启动服务器:" + apusicpath);
            // Process p;
            p = Runtime.getRuntime().exec(apusicpath);
            InputStream out = p.getInputStream();
            InputStream err = p.getErrorStream();
            StreamPumper outPumper = new StreamPumper(out, System.out);
            StreamPumper errPumper = new StreamPumper(err, System.err);
            outPumper.start();
            errPumper.start();
            serverIOMap.put(as, new Object[]{p, outPumper, errPumper});
            // 等待2分钟，或检测到服务器就绪
            int i = 120;
            while (i > 0 && !outPumper.isServerStarted()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                i--;
            }
            if (outPumper.isServerStarted()) {
                log.info("已启动服务器......");
            } else if (i <= 0) {
                log.warn("2分钟程序未检测服务器输出\"服务器就绪\"，请人工确认服务器是否启动");
                throw new Exception();
            }
        } catch (Exception e) {
            log.error("启动服务器失败:" + apusicpath + ":" + e.getMessage(), e);
            if(p != null){
                p.destroy();
            }
            throw new RuntimeException("启动服务器失败:", e);
        }

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
            log.info("关闭服务器:" + apusicpath);
            Process p = Runtime.getRuntime().exec(apusicpath);
            InputStream out = p.getInputStream();
            InputStream err = p.getErrorStream();
            StreamPumper outPumper = new StreamPumper(out, System.out);
            StreamPumper errPumper = new StreamPumper(err, System.err);
            outPumper.start();
            errPumper.start();
            // 等待2分钟，或检测到服务器关闭
            int i = 120;
            while (i > 0) {
                if (startServerOutPumper != null && startServerOutPumper.isServerStopped()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                i--;
            }
            if (startServerOutPumper != null && startServerOutPumper.isServerStopped()) {
                log.info("已经关闭服务器...");
            } else if (i <= 0) {
                log.warn("2分钟程序未检测服务器输出\"服务器已停止\"，请人工确认服务器是否关闭");
            }
            if (serverProcess != null) {
                serverProcess.destroy();
            }
        } catch (Exception e) {
            log.error("关闭服务器失败:", e);
            throw new RuntimeException("关闭服务器失败:" + apusicpath, e);
        }
    }


    public static boolean checkForWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().startsWith("win");
    }

    private static class StreamPumper extends Thread {
        private InputStream in;

        private OutputStream out;

        private boolean shutdown = false;

        private volatile boolean serverStarted = false;
        private volatile boolean serverStopped = false;

        StreamPumper(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        public void shutdown() {
            shutdown = true;
        }

        public boolean isServerStarted() {
            return serverStarted;
        }

        public boolean isServerStopped() {
            return serverStopped;
        }

        public void run() {
            BufferedReader br = null;
            BufferedWriter bw = null;
            try {
                br = new BufferedReader(new InputStreamReader(in, Charset.forName("GBK")));
                bw = new BufferedWriter(new OutputStreamWriter(out, Charset.forName("GBK")));
                String readLine = br.readLine();
                while (!shutdown && readLine != null) {
                    bw.write(readLine);
                    bw.newLine();
                    bw.flush();
                    if (readLine.contains("apusic.server.Main")) {
                        if (readLine.contains("服务器就绪")) {
                            log.info("服务器就绪");
                            serverStarted = true;
                        } else if (readLine.contains("服务器已停止")) {
                            log.info("服务器已停止");
                            serverStopped = true;
                        }
                    }
                    readLine = br.readLine();
                }
            } catch (IOException e) {
            }
        }
    }

}
