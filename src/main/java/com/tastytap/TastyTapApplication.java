package com.tastytap;

import com.tastytap.controller.*;
import com.tastytap.filtros.FiltroAutenticacion;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import java.io.File;

public class TastyTapApplication {
    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        // Directorio temporal para Tomcat
        String baseDir = new File("target/tomcat-temp").getAbsolutePath();
        Context context = tomcat.addContext("", baseDir);

        // --- CONFIGURACIÓN DE FILTROS ---
        // Registramos el filtro de forma manual para asegurar el orden de ejecución
        FilterDef authFilterDef = new FilterDef();
        authFilterDef.setFilterName("FiltroAutenticacion");
        authFilterDef.setFilterClass(FiltroAutenticacion.class.getName());
        context.addFilterDef(authFilterDef);

        FilterMap authFilterMap = new FilterMap();
        authFilterMap.setFilterName("FiltroAutenticacion");
        authFilterMap.addURLPattern("/*"); // El filtro decide internamente qué es público y qué no
        context.addFilterMap(authFilterMap);

        // --- REGISTRO DE SERVLETS (API) ---
        // Autenticación
        registrarServlet(context, "LoginServlet", new LoginServlet(), "/api/auth/login");
        registrarServlet(context, "RegistroServlet", new RegistroServlet(), "/api/auth/registro");

        // Productos
        registrarServlet(context, "ProductosServlet", new ProductosServlet(), "/api/productos/*");

        // Pedidos y Pagos
        registrarServlet(context, "PedidosServlet", new PedidosServlet(), "/api/pedidos/*");
        registrarServlet(context, "PagosServlet", new PagosServlet(), "/api/pagos/*");

        // Usuario
        registrarServlet(context, "PerfilServlet", new PerfilServlet(), "/api/perfil/*");
        registrarServlet(context, "HistorialServlet", new HistorialServlet(), "/api/historial/*");

        // Admin
        registrarServlet(context, "AdminServlet", new AdminServlet(), "/api/admin/*");

        System.out.println("==============================================");
        System.out.println("🚀 TASTYTAP API RUNNING ON: http://localhost:8080");
        System.out.println("👉 Login Endpoint: http://localhost:8080/api/auth/login");
        System.out.println("==============================================");

        tomcat.start();
        tomcat.getServer().await();
    }

    private static void registrarServlet(Context context, String name, jakarta.servlet.Servlet servlet, String mapping) {
        Tomcat.addServlet(context, name, servlet);
        context.addServletMappingDecoded(mapping, name);
    }
}