package com.tastytap.dao;

import com.tastytap.config.ConexionDB;
import com.tastytap.modelo.Pedido;
import com.tastytap.modelo.DetallePedido;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object para Pedidos.
 * Implementa RF08, RF09, RF10, RF12, RF23, RF24, RF25, RF31, RF36 y RF43.
 */
public class PedidoDao {

    /**
     * RF23 / RNF07: Crea un pedido completo con transacción.
     */
    public boolean crearPedidoCompleto(Pedido pedido) {
        String sqlPedido = "INSERT INTO pedidos (cliente_id, emprendimiento_id, total, estado, fecha_hora) VALUES (?, ?, ?, ?, NOW())";
        String sqlDetalle = "INSERT INTO detalles_pedido (pedido_id, producto_id, cantidad, precio_unitario_fijo) VALUES (?, ?, ?, ?)";

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            try (PreparedStatement psP = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                psP.setInt(1, pedido.getClienteId());
                psP.setInt(2, pedido.getEmprendimientoId());
                psP.setDouble(3, pedido.getTotal());
                psP.setString(4, pedido.getEstado());
                psP.executeUpdate();

                try (ResultSet rs = psP.getGeneratedKeys()) {
                    if (rs.next()) pedido.setId(rs.getInt(1));
                }
            }

            try (PreparedStatement psD = con.prepareStatement(sqlDetalle)) {
                for (DetallePedido item : pedido.getDetalles()) {
                    psD.setInt(1, pedido.getId());
                    psD.setInt(2, item.getProductoId());
                    psD.setInt(3, item.getCantidad());
                    psD.setDouble(4, item.getPrecioUnitarioFijo());
                    psD.addBatch();
                }
                psD.executeBatch();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /**
     * RF36: Sube la ruta del comprobante y cambia estado a validación.
     */
    public boolean subirComprobante(int pedidoId, String urlArchivo) {
        String sql = "UPDATE pedidos SET comprobante_url = ?, estado = 'EN_VALIDACION' WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, urlArchivo);
            ps.setInt(2, pedidoId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * RF24 / RF25: Validación manual de pago. Si el monto coincide, reduce inventario (RF05).
     */
    public boolean validarPagoManual(int pedidoId, double montoRecibido) {
        Pedido pedido = buscarPorId(pedidoId);
        if (pedido == null) return false;

        // RF25: Bloqueo por discrepancia
        if (Math.abs(montoRecibido - pedido.getTotal()) > 0.01) {
            return false; 
        }

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            // 1. Cambiar estado del pedido
            String sqlUpdate = "UPDATE pedidos SET estado = 'PAGADO', fecha_pago = NOW() WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                ps.setInt(1, pedidoId);
                ps.executeUpdate();
            }

            // 2. RF05: Reducir Inventario automáticamente
            String sqlStock = "UPDATE productos SET stock = stock - ? WHERE id = ?";
            List<DetallePedido> detalles = obtenerDetalles(pedidoId);
            try (PreparedStatement psS = con.prepareStatement(sqlStock)) {
                for (DetallePedido d : detalles) {
                    psS.setInt(1, d.getCantidad());
                    psS.setInt(2, d.getProductoId());
                    psS.addBatch();
                }
                psS.executeBatch();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        }
    }

    /**
     * RF12 / RF31: Obtiene estadísticas para el Administrador.
     */
    public Map<String, Object> obtenerEstadisticasGlobales() {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT COUNT(*) as total_pedidos, SUM(total) as recaudacion FROM pedidos WHERE estado = 'PAGADO'";
        
        try (Connection con = ConexionDB.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                stats.put("totalPedidos", rs.getInt("total_pedidos"));
                stats.put("recaudacionTotal", rs.getDouble("recaudacion"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    // --- Métodos de Listado ---

    public List<Pedido> listarPorCliente(int id) {
        return listarGenerico("SELECT * FROM pedidos WHERE cliente_id = ? ORDER BY fecha_hora DESC", id);
    }

    public List<Pedido> listarPorProveedor(int id) {
        String sql = "SELECT p.* FROM pedidos p JOIN emprendimientos e ON p.emprendimiento_id = e.id WHERE e.proveedor_id = ? ORDER BY p.fecha_hora DESC";
        return listarGenerico(sql, id);
    }

    public Pedido buscarPorId(int id) {
        String sql = "SELECT * FROM pedidos WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extraerPedido(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private List<Pedido> listarGenerico(String sql, int id) {
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(extraerPedido(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<DetallePedido> obtenerDetalles(int pedidoId) {
        List<DetallePedido> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalles_pedido WHERE pedido_id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DetallePedido d = new DetallePedido();
                d.setProductoId(rs.getInt("producto_id"));
                d.setCantidad(rs.getInt("cantidad"));
                d.setPrecioUnitarioFijo(rs.getDouble("precio_unitario_fijo"));
                lista.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private Pedido extraerPedido(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id"));
        p.setClienteId(rs.getInt("cliente_id"));
        p.setEmprendimientoId(rs.getInt("emprendimiento_id"));
        p.setTotal(rs.getDouble("total"));
        p.setEstado(rs.getString("estado"));
        p.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
        return p;
    }
}