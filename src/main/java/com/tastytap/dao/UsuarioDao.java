package com.tastytap.dao;

import com.tastytap.config.ConexionDB;
import com.tastytap.modelo.Usuario;
import java.sql.*;

public class UsuarioDao {

    private static final String SQL_SELECT_ALL = "SELECT * FROM usuarios";
    private static final String SQL_SELECT_BY_NAME = "SELECT * FROM usuarios WHERE nombre = ?";

    // Devuelve TODOS los usuarios como texto formateado (sin colecciones, sin arrays)
    public static String obtenerTodosComoTexto() {
        StringBuilder resultado = new StringBuilder();
        boolean hayRegistros = false;
        
        try (Connection conn = ConexionDB.MetodoConectar();
            PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                hayRegistros = true;
                Usuario u = new Usuario();
                u.setNombre(rs.getString("nombre"));
                u.setEdad(rs.getInt("edad"));
                u.setPass(rs.getString("contrasena"));
                resultado.append(u.toString()).append("\n");
            }
        } catch (SQLException e) {
            return "Error al consultar usuarios: " + e.getMessage();
        }
        
        if (!hayRegistros) {
            return "No hay usuarios registrados.";
        }

    return resultado.toString().trim(); // ← Solo DEVUELVE el texto, no imprime
}

    // Buscar un usuario por nombre (retorna un solo objeto)
    public static Usuario buscarPorNombre(String nombreBusqueda) {
        Usuario usuario = null;
        
        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_BY_NAME)) {
            
            pstmt.setString(1, nombreBusqueda);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario();
                    usuario.setNombre(rs.getString("nombre"));
                    usuario.setEdad(rs.getInt("edad"));
                    usuario.setPass(rs.getString("contrasena"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        }
        return usuario;
    }

    public static boolean insertar(Usuario u) {
    String sql = "INSERT INTO usuarios (nombre, edad, contrasena) VALUES (?, ?, ?)";
    try (Connection conn = ConexionDB.MetodoConectar();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, u.getNombre());
        pstmt.setInt(2, u.getEdad());
        pstmt.setString(3, u.getPass());
        
        return pstmt.executeUpdate() > 0; // Si devuelve > 0, se insertó con éxito
    } catch (SQLException e) {
        System.err.println("Error al insertar: " + e.getMessage());
        return false;
    }
}

    public static boolean actualizar(Usuario u) {
    String sql = "UPDATE usuarios SET edad = ?, contrasena = ? WHERE nombre = ?";
    try (Connection conn = ConexionDB.MetodoConectar();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, u.getEdad());
        pstmt.setString(2, u.getPass());
        pstmt.setString(3, u.getNombre());
        
        return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
        return false;
    }
}

    public static boolean eliminar(String nombre) {
    String sql = "DELETE FROM usuarios WHERE nombre = ?";
    try (Connection conn = ConexionDB.MetodoConectar();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, nombre);
        return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
        return false;
    }
}
}