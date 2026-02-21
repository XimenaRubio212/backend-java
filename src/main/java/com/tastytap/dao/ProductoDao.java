package com.tastytap.dao;

import com.tastytap.config.ConexionDB;
import com.tastytap.modelo.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Producto.
 * Implementa RF04 (Filtros), RF05 (Inventario), RF06 (Gestión) y RF14 (Imágenes).
 */
public class ProductoDao {

    /**
     * RF04: Lista productos aplicando filtros en cascada (Ciudad -> Emprendimiento -> Categoría).
     * Nota: Se asume que la ciudad pertenece al emprendimiento.
     */
    public List<Producto> listarPorCascada(String ciudad, int emprendimientoId, String categoria) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.* FROM productos p " +
                     "JOIN emprendimientos e ON p.emprendimiento_id = e.id " +
                     "WHERE e.ciudad = ? AND p.emprendimiento_id = ? AND p.categoria = ? AND p.activo = 1";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, ciudad);
            ps.setInt(2, emprendimientoId);
            ps.setString(3, categoria);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(extraerProducto(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * RF04: Lista productos filtrados solo por ciudad.
     */
    public List<Producto> listarPorCiudad(String ciudad) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.* FROM productos p " +
                     "JOIN emprendimientos e ON p.emprendimiento_id = e.id " +
                     "WHERE e.ciudad = ? AND p.activo = 1";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ciudad);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(extraerProducto(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * RF05: Permite al proveedor ver y gestionar sus propios productos.
     */
    public List<Producto> listarPorEmprendimiento(int empId) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos WHERE emprendimiento_id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(extraerProducto(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * RF06 / RF14: Inserta un nuevo producto.
     * La lógica de "imagen obligatoria" se valida antes en el Servlet.
     */
    public boolean insertar(Producto p) {
        String sql = "INSERT INTO productos (nombre, descripcion, precio, categoria, stock, url_imagen, activo, emprendimiento_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setString(4, p.getCategoria());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getUrlImagen());
            ps.setBoolean(7, p.isActivo());
            ps.setInt(8, p.getEmprendimientoId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * RF05: Actualización rápida de inventario (Stock y Precio en tiempo real).
     */
    public boolean actualizarInventario(int id, int nuevoStock, double nuevoPrecio) {
        String sql = "UPDATE productos SET stock = ?, precio = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setDouble(2, nuevoPrecio);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * RF06: Eliminar o desactivar un producto del catálogo.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos";
        try (Connection con = ConexionDB.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(extraerProducto(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Método auxiliar para mapear el ResultSet
    private Producto extraerProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecio(rs.getDouble("precio"));
        p.setCategoria(rs.getString("categoria"));
        p.setStock(rs.getInt("stock"));
        p.setUrlImagen(rs.getString("url_imagen"));
        p.setActivo(rs.getBoolean("activo"));
        p.setEmprendimientoId(rs.getInt("emprendimiento_id"));
        return p;
    }
}