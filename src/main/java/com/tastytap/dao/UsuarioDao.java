package com.tastytap.dao;

import com.tastytap.config.ConexionDB;
import com.tastytap.modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UsuarioDao {

    public static boolean registrar(Usuario u, String email, String telefono) {
        // Consultas ajustadas a tu esquema SQL
        String sqlUser = "INSERT INTO usuarios (nombre, contrasena, edad, rol_id) VALUES (?, ?, ?, ?)";
        String sqlEmail = "INSERT INTO correos (usuario_id, email, tipo, verificado) VALUES (?, ?, ?, ?)";
        String sqlTel = "INSERT INTO telefonos (usuario_id, numero, tipo, es_principal, verificado) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return false;
            
            con.setAutoCommit(false); // Iniciamos transacción

            try (PreparedStatement psUser = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                // 1. Insertar Usuario
                psUser.setString(1, u.getNombre());
                psUser.setString(2, BCrypt.hashpw(u.getPass(), BCrypt.gensalt()));
                psUser.setInt(3, u.getEdad());
                psUser.setInt(4, u.getRol_id());
                psUser.executeUpdate();

                ResultSet rs = psUser.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);

                    // 2. Insertar Correo (Campos: usuario_id, email, tipo, verificado)
                    try (PreparedStatement psEmail = con.prepareStatement(sqlEmail)) {
                        psEmail.setInt(1, userId);
                        psEmail.setString(2, email);
                        psEmail.setString(3, "Personal"); // tipo
                        psEmail.setInt(4, 1);            // verificado (tinyint 1)
                        psEmail.executeUpdate();
                    }

                    // 3. Insertar Teléfono (Campos: usuario_id, numero, tipo, es_principal, verificado)
                    try (PreparedStatement psTel = con.prepareStatement(sqlTel)) {
                        psTel.setInt(1, userId);
                        psTel.setString(2, telefono);
                        psTel.setString(3, "Movil");     // tipo
                        psTel.setInt(4, 1);              // es_principal (tinyint 1)
                        psTel.setInt(5, 1);              // verificado (tinyint 1)
                        psTel.executeUpdate();
                    }
                }
                
                con.commit(); // Si todo sale bien, guardamos
                return true;
            } catch (SQLException e) {
                con.rollback(); // Si algo falla, deshacemos todo
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Usuario validar(String nombre, String passPlana) {
        String sql = "SELECT * FROM usuarios WHERE nombre = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String passHashed = rs.getString("contrasena");
                    // Verificamos la contraseña con BCrypt
                    if (BCrypt.checkpw(passPlana, passHashed)) {
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setNombre(rs.getString("nombre"));
                        u.setRol_id(rs.getInt("rol_id"));
                        return u;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}