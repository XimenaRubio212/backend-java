package com.tastytap.dao;

import com.google.gson.JsonElement;
import com.tastytap.config.ConexionDB;
import com.tastytap.modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
// import java.util.ArrayList;
// import java.util.List;

/**
 * Data Access Object para la entidad Usuario.
 * Centraliza toda la lógica de persistencia cumpliendo con RF01, RF02, RF03, RF16, RF32 y RF34.
 */
public class UsuarioDao {

    /**
     * RF01 / RF18: Registra un nuevo usuario con contraseña encriptada.
     */
    public boolean registrar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, edad, password, rol, correo_principal, correo_respaldo, " +
                     "telefono_principal, correo_verificado, preferencias_json, fecha_creacion) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setInt(2, usuario.getEdad());
            // RNF01: Encriptación obligatoria
            ps.setString(3, BCrypt.hashpw(usuario.getPassword(), BCrypt.gensalt()));
            ps.setString(4, usuario.getRol());
            ps.setString(5, usuario.getCorreoPrincipal());
            ps.setString(6, usuario.getCorreoRespaldo());
            ps.setString(7, usuario.getTelefonoPrincipal());
            ps.setBoolean(8, false); // Inicia desverificado (RF32)
            ps.setString(9, usuario.getPreferenciasJson()); // RF34

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * RF02 / RF32: Valida credenciales y bloquea si no está verificado.
     */
    public Usuario validarLogin(String email, String password) {
        String sql = "SELECT * FROM usuarios WHERE correo_principal = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                if (BCrypt.checkpw(password, rs.getString("password"))) {
                    // Restricción RF32: No puede iniciar sesión sin verificar correo
                    if (!rs.getBoolean("correo_verificado")) {
                        return null; 
                    }
                    return extraerUsuario(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RF03: Busca un usuario por ID para cargar perfil o validar cambios.
     */
    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extraerUsuario(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RF20: Busca por email para evitar duplicados en el registro.
     */
    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE correo_principal = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extraerUsuario(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RF03: Actualiza los datos permitidos del perfil.
     * La lógica de qué campos ignorar según el rol se maneja en el Servlet.
     */
    public boolean actualizarPerfil(Usuario u) {
        String sql = "UPDATE usuarios SET nombre = ?, correo_principal = ?, edad = ?, " +
                     "correo_respaldo = ?, telefono_principal = ?, fecha_actualizacion = NOW() WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getCorreoPrincipal());
            ps.setInt(3, u.getEdad());
            ps.setString(4, u.getCorreoRespaldo());
            ps.setString(5, u.getTelefonoPrincipal());
            ps.setInt(6, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * RF32: Activa la cuenta tras la verificación por correo.
     */
    public boolean verificarCorreo(int id) {
        String sql = "UPDATE usuarios SET correo_verificado = true WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * RF34: Guarda las preferencias personalizadas del cliente.
     */
    public boolean actualizarPreferencias(int id, String json) {
        String sql = "UPDATE usuarios SET preferencias_json = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, json);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * RF16: Permite al administrador cambiar el rol de un usuario.
     */
    public boolean cambiarRol(int id, String nuevoRol) {
        String sql = "UPDATE usuarios SET rol = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoRol);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // Helper para mapear ResultSet a Objeto Usuario
    private Usuario extraerUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setEdad(rs.getInt("edad"));
        u.setRol(rs.getString("rol"));
        u.setCorreoPrincipal(rs.getString("correo_principal"));
        u.setCorreoRespaldo(rs.getString("correo_respaldo"));
        u.setCorreoVerificado(rs.getBoolean("correo_verificado"));
        u.setTelefonoPrincipal(rs.getString("telefono_principal"));
        u.setPreferenciasJson(rs.getString("preferencias_json"));
        return u;
    }

    public JsonElement listarTodos() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listarTodos'");
    }
}