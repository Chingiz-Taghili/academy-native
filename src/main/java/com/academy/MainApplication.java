package com.academy;

import com.academy.servlet.StudentServlet;
import com.academy.servlet.TeacherServlet;
import com.academy.servlet.UniversityServlet;
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
        // Servlet-ləri qeydiyyatdan keçirtmək
        Tomcat.addServlet(context, "studentServlet", new StudentServlet());
        Tomcat.addServlet(context, "teacherServlet", new TeacherServlet());
        Tomcat.addServlet(context, "universityServlet", new UniversityServlet());
        context.addServletMappingDecoded("/students/*", "studentServlet");
        context.addServletMappingDecoded("/teachers/*", "teacherServlet");
        context.addServletMappingDecoded("/universities/*", "universityServlet");

        tomcat.start();
        tomcat.getServer().await();
    }
}