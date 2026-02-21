package com.tastytap.dao;

import com.tastytap.config.ConexionDB;
import com.tastytap.modelo.Emprendimiento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmprendimientoDao {

    /**
     * RF29: Registra un emprendimiento vinculado obligatoriamente a un usuario proveedor.
     */
    public boolean registrar(Emprendimiento emp) {
        String sql = "INSERT INTO emprendimientos (nombre, ciudad, ubicacion, info_empresa, proveedor_id, estado, fecha_creacion) " +
                     "VALUES (?, ?, ?, ?, ?, 'ACTIVO', NOW())";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getCiudad());
            ps.setString(3, emp.getUbicacion());
            ps.setString(4, emp.getInfoEmpresa());
            ps.setInt(5, emp.getProveedorId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * RF04: Lista emprendimientos por ciudad para el catálogo jerárquico.
     */
    public List<Emprendimiento> listarPorCiudad(String ciudad) {
        List<Emprendimiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM emprendimientos WHERE ciudad = ? AND estado = 'ACTIVO'";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ciudad);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(extraerEmprendimiento(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * RF30: Permite al administrador cambiar el estado del negocio.
     */
    public boolean cambiarEstado(int id, String nuevoEstado) {
        String sql = "UPDATE emprendimientos SET estado = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * RF35: Busca un emprendimiento por ID para validar si está abierto antes de un pedido.
     */
    public Emprendimiento buscarPorId(int id) {
        String sql = "SELECT * FROM emprendimientos WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extraerEmprendimiento(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Emprendimiento extraerEmprendimiento(ResultSet rs) throws SQLException {
        Emprendimiento e = new Emprendimiento();
        e.setId(rs.getInt("id"));
        e.setNombre(rs.getString("nombre"));
        e.setCiudad(rs.getString("ciudad"));
        e.setUbicacion(rs.getString("ubicacion"));
        e.setInfoEmpresa(rs.getString("info_empresa"));
        e.setProveedorId(rs.getInt("proveedor_id"));
        e.setEstado(rs.getString("estado"));
        return e;
    }
}