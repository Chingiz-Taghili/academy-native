package com.academy;

import com.academy.servlet.StudentServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

public class MainApplication {
    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();
        //Müvəqqəti yükləmə yeri
        Context context = tomcat.addContext("/", System.getProperty("java.io.tmpdir"));
        // Servleti qeydiyyatdan keçirtmək
        Tomcat.addServlet(context, "studentServlet", new StudentServlet());
        context.addServletMappingDecoded("/students/*", "studentServlet");

        tomcat.start();
        tomcat.getServer().await();
    }
}