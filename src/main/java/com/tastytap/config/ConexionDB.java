package com.tastytap.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    // Datos de conexión
    private static String URL = "jdbc:mysql://localhost:3306/tastytap";
    private static String USER = "root"; 
    private static String PASS = "D*1005107316";
    
    public static Connection MetodoConectar() {
        try {
            // Cargar el driver (opcional en versiones modernas de JDBC pero recomendado)
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✅ Conexión establecida correctamente");
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Error al conectar: " + e.getMessage());
            return null;
        }
    }
}
