package com.tastytap.servlets;

import com.google.gson.*;
import com.tastytap.config.ConexionDB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * ============================================================
 *  HistorialServlet — GET /api/historial
 * ============================================================
 *
 *  Devuelve el historial de pedidos del cliente autenticado.
 *
 *  REQUIERE TOKEN → Solo el cliente ve SUS pedidos.
 *  El userId viene del token JWT, no del URL. Así un cliente
 *  no puede ver el historial de otro cliente aunque adivine su ID.
 *
 *  Respuesta JSON:
 *  {
 *    "status": "success",
 *    "data": [
 *      {
 *        "pedidoId": 1,
 *        "fecha": "2025-01-15 14:30:00",
 *        "estado": "entregado",
 *        "total": 25000.00,
 *        "detalles": [
 *          { "producto": "Hamburguesa Clásica", "cantidad": 2, "subtotal": 20000 },
 *          { "producto": "Agua", "cantidad": 1, "subtotal": 5000 }
 *        ]
 *      }
 *    ]
 *  }
 * ============================================================
 */
@WebServlet("/api/historial")
public class HistorialServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // Obtener el ID del usuario desde el token (puesto por FiltroAutenticacion)
        int usuarioId = (int) req.getAttribute("usuarioId");
        int rolId     = (int) req.getAttribute("rolId");

        // Verificar que sea cliente (rol 2)
        // Un proveedor o admin no debería acceder a esta ruta
        if (rolId != 2) {
            resp.setStatus(403);
            out.print(gson.toJson(error("Acceso denegado. Esta ruta es solo para clientes.")));
            return;
        }

        // Consulta: todos los pedidos del cliente con sus detalles
        String sqlPedidos = """
            SELECT p.id, p.fecha_hora, p.estado, p.total
            FROM pedidos p
            WHERE p.cliente_id = ?
            ORDER BY p.fecha_hora DESC
            """;

        String sqlDetalles = """
            SELECT pr.nombre AS producto, dp.cantidad, dp.subtotal, dp.descuento_aplicado
            FROM detalles_pedidos dp
            INNER JOIN productos pr ON pr.id = dp.producto_id
            WHERE dp.pedido_id = ?
            """;

        JsonArray listaPedidos = new JsonArray();

        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement psPedidos = conn.prepareStatement(sqlPedidos)) {

            psPedidos.setInt(1, usuarioId);

            try (ResultSet rsPedidos = psPedidos.executeQuery()) {
                while (rsPedidos.next()) {
                    JsonObject pedido = new JsonObject();
                    int pedidoId = rsPedidos.getInt("id");

                    pedido.addProperty("pedidoId", pedidoId);
                    pedido.addProperty("fecha", rsPedidos.getString("fecha_hora"));
                    pedido.addProperty("estado", rsPedidos.getString("estado"));
                    pedido.addProperty("total", rsPedidos.getDouble("total"));

                    // Obtener los detalles de cada pedido
                    JsonArray detalles = new JsonArray();
                    try (PreparedStatement psDetalles = conn.prepareStatement(sqlDetalles)) {
                        psDetalles.setInt(1, pedidoId);
                        try (ResultSet rsDetalles = psDetalles.executeQuery()) {
                            while (rsDetalles.next()) {
                                JsonObject detalle = new JsonObject();
                                detalle.addProperty("producto", rsDetalles.getString("producto"));
                                detalle.addProperty("cantidad", rsDetalles.getInt("cantidad"));
                                detalle.addProperty("subtotal", rsDetalles.getDouble("subtotal"));
                                detalle.addProperty("descuento", rsDetalles.getDouble("descuento_aplicado"));
                                detalles.add(detalle);
                            }
                        }
                    }

                    pedido.add("detalles", detalles);
                    listaPedidos.add(pedido);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error en HistorialServlet: " + e.getMessage());
            resp.setStatus(500);
            out.print(gson.toJson(error("Error al obtener el historial.")));
            return;
        }

        // Respuesta exitosa
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("status", "success");
        respuesta.addProperty("total_pedidos", listaPedidos.size());
        respuesta.add("data", listaPedidos);

        out.print(gson.toJson(respuesta));
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private JsonObject error(String mensaje) {
        JsonObject obj = new JsonObject();
        obj.addProperty("status", "error");
        obj.addProperty("message", mensaje);
        return obj;
    }
}