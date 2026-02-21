package com.tastytap;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import java.io.File;

public class TastyTapApplication {
    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        // Configuración del contexto
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext("", docBase);

        // Registro de Servlets manualmente (Si el @WebServlet no los toma solos)
        // Esto asegura que todo lo que ves en "rojo" sea mapeado por Tomcat
        Tomcat.addServlet(context, "LoginServlet", "com.tastytap.controller.LoginServlet");
        context.addServletMappingDecoded("/login", "LoginServlet");

        Tomcat.addServlet(context, "RegistroServlet", "com.tastytap.controller.RegistroServlet");
        context.addServletMappingDecoded("/registro", "RegistroServlet");

        Tomcat.addServlet(context, "ProductosServlet", "com.tastytap.controller.ProductosServlet");
        context.addServletMappingDecoded("/productos", "ProductosServlet");

        Tomcat.addServlet(context, "PedidosServlet", "com.tastytap.controller.PedidosServlet");
        context.addServletMappingDecoded("/pedidos", "PedidosServlet");

        System.out.println(">>> TastyTap Backend iniciado en http://localhost:8080");
        
        tomcat.start();
        tomcat.getServer().await();
    }
}