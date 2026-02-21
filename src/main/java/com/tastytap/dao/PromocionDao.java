package com.tastytap.dao;

import com.tastytap.config.ConexionDB;
import com.tastytap.modelo.Promocion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromocionDao {

    /**
     * RF38: Obtiene las promociones vigentes para un producto específico.
     */
    public List<Promocion> obtenerPromocionesVigentesPorProducto(int productoId) {
        List<Promocion> lista = new ArrayList<>();
        String sql = "SELECT pr.* FROM promociones pr " +
                     "JOIN productos_promociones pp ON pr.id = pp.promocion_id " +
                     "WHERE pp.producto_id = ? AND pr.activa = 1 " +
                     "AND NOW() BETWEEN pr.fecha_inicio AND pr.fecha_fin";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Promocion p = new Promocion();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setPorcentajeDescuento(rs.getDouble("porcentaje_descuento"));
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}