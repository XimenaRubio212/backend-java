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
 *  ProductosServlet — /api/productos
 * ============================================================
 *
 *  GET  /api/productos       → Lista los productos del proveedor
 *  POST /api/productos       → Agrega un nuevo producto
 *  PUT  /api/productos       → Actualiza un producto existente
 *  DELETE /api/productos     → Desactiva (no borra) un producto
 *
 *  REQUIERE TOKEN → Solo proveedores (rol 3) pueden operar aquí.
 *
 *  Los productos se asocian al emprendimiento del proveedor.
 *  Si el proveedor no tiene emprendimiento, se le indica.
 * ============================================================
 */
@WebServlet("/api/productos")
public class ProductosServlet extends HttpServlet {

    private final Gson gson = new Gson();

    // ----------------------------------------------------------
    // GET → Listar productos del proveedor autenticado
    // ----------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        int usuarioId = (int) req.getAttribute("usuarioId");
        int rolId     = (int) req.getAttribute("rolId");

        // Solo proveedores (rol 3)
        if (rolId != 3) {
            resp.setStatus(403);
            out.print(gson.toJson(error("Acceso denegado. Solo para proveedores.")));
            return;
        }

        // Obtener productos del emprendimiento del proveedor
        String sql = """
            SELECT p.id, p.nombre, p.descripcion, p.precio,
                   p.activo, p.capacidad_diaria, p.vendido_hoy,
                   c.nombre AS categoria
            FROM productos p
            INNER JOIN emprendimientos e ON e.id = p.emprendimiento_id
            INNER JOIN categorias c ON c.id = p.categoria_id
            WHERE e.usuario_id = ?
            ORDER BY p.nombre
            """;

        JsonArray lista = new JsonArray();

        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JsonObject producto = new JsonObject();
                    producto.addProperty("id", rs.getInt("id"));
                    producto.addProperty("nombre", rs.getString("nombre"));
                    producto.addProperty("descripcion", rs.getString("descripcion"));
                    producto.addProperty("precio", rs.getDouble("precio"));
                    producto.addProperty("activo", rs.getInt("activo") == 1);
                    producto.addProperty("capacidadDiaria", rs.getInt("capacidad_diaria"));
                    producto.addProperty("vendidoHoy", rs.getInt("vendido_hoy"));
                    producto.addProperty("categoria", rs.getString("categoria"));
                    lista.add(producto);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error en ProductosServlet GET: " + e.getMessage());
            resp.setStatus(500);
            out.print(gson.toJson(error("Error al obtener productos.")));
            return;
        }

        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("status", "success");
        respuesta.addProperty("total", lista.size());
        respuesta.add("data", lista);
        out.print(gson.toJson(respuesta));
    }

    // ----------------------------------------------------------
    // POST → Agregar un nuevo producto
    // ----------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        int usuarioId = (int) req.getAttribute("usuarioId");
        int rolId     = (int) req.getAttribute("rolId");

        if (rolId != 3) {
            resp.setStatus(403);
            out.print(gson.toJson(error("Acceso denegado.")));
            return;
        }

        // Leer parámetros del formulario
        String nombre      = req.getParameter("nombre");
        String descripcion = req.getParameter("descripcion");
        String precioStr   = req.getParameter("precio");
        String categoriaStr = req.getParameter("categoria_id");
        String capacidadStr = req.getParameter("capacidad_diaria");

        // Validaciones
        if (nombre == null || nombre.isBlank()) {
            out.print(gson.toJson(error("El nombre del producto es requerido.")));
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            out.print(gson.toJson(error("El precio debe ser un número positivo.")));
            return;
        }

        int categoriaId;
        try {
            categoriaId = Integer.parseInt(categoriaStr);
        } catch (NumberFormatException e) {
            out.print(gson.toJson(error("Categoría inválida.")));
            return;
        }

        int capacidad;
        try {
            capacidad = Integer.parseInt(capacidadStr);
            if (capacidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            capacidad = 50; // Valor por defecto
        }

        // Obtener el emprendimiento del proveedor
        String sqlEmp = "SELECT id FROM emprendimientos WHERE usuario_id = ? AND activo = 1 LIMIT 1";
        String sqlInsert = """
            INSERT INTO productos (emprendimiento_id, categoria_id, nombre, descripcion,
                                   precio, activo, capacidad_diaria, vendido_hoy)
            VALUES (?, ?, ?, ?, ?, 1, ?, 0)
            """;

        try (Connection conn = ConexionDB.MetodoConectar()) {

            // Buscar el emprendimiento activo del proveedor
            int emprendimientoId;
            try (PreparedStatement psEmp = conn.prepareStatement(sqlEmp)) {
                psEmp.setInt(1, usuarioId);
                try (ResultSet rs = psEmp.executeQuery()) {
                    if (!rs.next()) {
                        resp.setStatus(400);
                        out.print(gson.toJson(error("No tienes un emprendimiento activo.")));
                        return;
                    }
                    emprendimientoId = rs.getInt("id");
                }
            }

            // Insertar el producto
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, emprendimientoId);
                ps.setInt(2, categoriaId);
                ps.setString(3, nombre.trim());
                ps.setString(4, descripcion != null ? descripcion.trim() : "");
                ps.setDouble(5, precio);
                ps.setInt(6, capacidad);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    int nuevoId = rs.next() ? rs.getInt(1) : 0;
                    JsonObject respuesta = new JsonObject();
                    respuesta.addProperty("status", "success");
                    respuesta.addProperty("message", "Producto '" + nombre + "' agregado correctamente.");
                    respuesta.addProperty("id", nuevoId);
                    out.print(gson.toJson(respuesta));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al agregar producto: " + e.getMessage());
            resp.setStatus(500);
            out.print(gson.toJson(error("Error al guardar el producto.")));
        }
    }

    // ----------------------------------------------------------
    // DELETE → Desactivar un producto (activo = 0)
    // No se borra físicamente para preservar el historial de pedidos
    // ----------------------------------------------------------
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        int usuarioId = (int) req.getAttribute("usuarioId");
        int rolId     = (int) req.getAttribute("rolId");

        if (rolId != 3) {
            resp.setStatus(403);
            out.print(gson.toJson(error("Acceso denegado.")));
            return;
        }

        String idStr = req.getParameter("id");
        int productoId;
        try {
            productoId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            out.print(gson.toJson(error("ID de producto inválido.")));
            return;
        }

        // Solo puede desactivar sus propios productos (JOIN con emprendimiento)
        String sql = """
            UPDATE productos p
            INNER JOIN emprendimientos e ON e.id = p.emprendimiento_id
            SET p.activo = 0
            WHERE p.id = ? AND e.usuario_id = ?
            """;

        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productoId);
            ps.setInt(2, usuarioId);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JsonObject respuesta = new JsonObject();
                respuesta.addProperty("status", "success");
                respuesta.addProperty("message", "Producto desactivado correctamente.");
                out.print(gson.toJson(respuesta));
            } else {
                resp.setStatus(404);
                out.print(gson.toJson(error("Producto no encontrado o no te pertenece.")));
            }

        } catch (SQLException e) {
            resp.setStatus(500);
            out.print(gson.toJson(error("Error al desactivar el producto.")));
        }
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