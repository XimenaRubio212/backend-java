package com.tastytap.servlets;

import com.google.gson.*;
import com.tastytap.config.ConexionDB;
import com.tastytap.dao.UsuarioDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================
 *  AdminServlet — /api/admin
 * ============================================================
 *
 *  Maneja todas las operaciones del panel de administración.
 *  Se distingue por el parámetro "seccion":
 *
 *  GET /api/admin?seccion=usuarios      → Lista todos los usuarios
 *  GET /api/admin?seccion=logs          → Logs del sistema
 *  GET /api/admin?seccion=mantenimiento → Estado del sistema (BD, etc.)
 *
 *  DELETE /api/admin?id=X               → Eliminar usuario por ID
 *
 *  REQUIERE TOKEN + ROL 1 (administrador).
 *  Si el rol no es 1, se retorna 403 Forbidden.
 * ============================================================
 */
@WebServlet("/api/admin")
public class AdminServlet extends HttpServlet {

    private final Gson gson = new Gson();

    // ----------------------------------------------------------
    // GET → Consultas del panel de administración
    // ----------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // Verificar que sea administrador
        int rolId = (int) req.getAttribute("rolId");
        if (rolId != 1) {
            resp.setStatus(403);
            out.print(gson.toJson(error("Acceso denegado. Solo para administradores.")));
            return;
        }

        // Determinar qué sección se solicita
        String seccion = req.getParameter("seccion");
        if (seccion == null) seccion = "usuarios";

        switch (seccion) {
            case "usuarios"      -> out.print(gson.toJson(getUsuarios()));
            case "logs"          -> out.print(gson.toJson(getLogs()));
            case "mantenimiento" -> out.print(gson.toJson(getMantenimiento()));
            default              -> {
                resp.setStatus(400);
                out.print(gson.toJson(error("Sección inválida: " + seccion)));
            }
        }
    }

    // ----------------------------------------------------------
    // DELETE → Eliminar un usuario
    // ----------------------------------------------------------
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        int rolId = (int) req.getAttribute("rolId");
        if (rolId != 1) {
            resp.setStatus(403);
            out.print(gson.toJson(error("Acceso denegado.")));
            return;
        }

        String idStr = req.getParameter("id");
        int targetId;
        try {
            targetId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            out.print(gson.toJson(error("ID inválido.")));
            return;
        }

        // Proteger: no permitir que el admin se elimine a sí mismo
        int adminId = (int) req.getAttribute("usuarioId");
        if (targetId == adminId) {
            out.print(gson.toJson(error("No puedes eliminarte a ti mismo.")));
            return;
        }

        boolean exito = UsuarioDao.eliminarPorId(targetId);
        if (exito) {
            JsonObject r = new JsonObject();
            r.addProperty("status", "success");
            r.addProperty("message", "Usuario eliminado correctamente.");
            out.print(gson.toJson(r));
        } else {
            resp.setStatus(404);
            out.print(gson.toJson(error("Usuario no encontrado.")));
        }
    }

    // ----------------------------------------------------------
    // SECCIÓN: USUARIOS
    // ----------------------------------------------------------
    private JsonObject getUsuarios() {
        String sql = """
            SELECT u.id, u.nombre, u.edad, r.nombre AS rol,
                   c.email, t.numero AS telefono
            FROM usuarios u
            INNER JOIN roles r ON r.id = u.rol_id
            LEFT JOIN correos c ON c.usuario_id = u.id AND c.tipo = 'principal'
            LEFT JOIN telefonos t ON t.usuario_id = u.id AND t.es_principal = 1
            ORDER BY u.id
            """;

        JsonArray lista = new JsonArray();

        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JsonObject u = new JsonObject();
                u.addProperty("id", rs.getInt("id"));
                u.addProperty("nombre", rs.getString("nombre"));
                u.addProperty("edad", rs.getInt("edad"));
                u.addProperty("rol", rs.getString("rol"));
                u.addProperty("email", rs.getString("email") != null ? rs.getString("email") : "");
                u.addProperty("telefono", rs.getString("telefono") != null ? rs.getString("telefono") : "");
                lista.add(u);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error en getUsuarios: " + e.getMessage());
            return error("Error al obtener usuarios.");
        }

        JsonObject r = new JsonObject();
        r.addProperty("status", "success");
        r.addProperty("total", lista.size());
        r.add("data", lista);
        return r;
    }

    // ----------------------------------------------------------
    // SECCIÓN: LOGS
    // Muestra estadísticas del sistema y actividad reciente
    // ----------------------------------------------------------
    private JsonObject getLogs() {
        JsonObject r = new JsonObject();
        r.addProperty("status", "success");

        JsonArray logs = new JsonArray();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Log 1: Total de usuarios por rol
        String sqlRoles = """
            SELECT r.nombre AS rol, COUNT(u.id) AS total
            FROM usuarios u
            INNER JOIN roles r ON r.id = u.rol_id
            GROUP BY r.nombre
            """;

        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement ps = conn.prepareStatement(sqlRoles);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JsonObject log = new JsonObject();
                log.addProperty("tipo", "INFO");
                log.addProperty("fecha", now);
                log.addProperty("mensaje", "Usuarios con rol '" + rs.getString("rol") + "': " + rs.getInt("total"));
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Error en getLogs: " + e.getMessage());
        }

        // Log 2: Pedidos del día
        String sqlPedidos = """
            SELECT COUNT(*) AS total, SUM(total) AS suma
            FROM pedidos
            WHERE DATE(fecha_hora) = CURDATE()
            """;

        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement ps = conn.prepareStatement(sqlPedidos);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                JsonObject log = new JsonObject();
                log.addProperty("tipo", "INFO");
                log.addProperty("fecha", now);
                log.addProperty("mensaje",
                    "Pedidos hoy: " + rs.getInt("total") +
                    " | Total vendido: $" + (rs.getDouble("suma")));
                logs.add(log);
            }
        } catch (SQLException e) {
            // Si no hay pedidos, no hay error crítico
        }

        // Log 3: Productos activos vs inactivos
        String sqlProductos = "SELECT activo, COUNT(*) AS total FROM productos GROUP BY activo";
        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement ps = conn.prepareStatement(sqlProductos);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String estado = rs.getInt("activo") == 1 ? "activos" : "inactivos";
                JsonObject log = new JsonObject();
                log.addProperty("tipo", "INFO");
                log.addProperty("fecha", now);
                log.addProperty("mensaje", "Productos " + estado + ": " + rs.getInt("total"));
                logs.add(log);
            }
        } catch (SQLException e) {
            // Ignorar si no hay productos
        }

        r.add("data", logs);
        return r;
    }

    // ----------------------------------------------------------
    // SECCIÓN: MANTENIMIENTO
    // Estado general del sistema
    // ----------------------------------------------------------
    private JsonObject getMantenimiento() {
        JsonObject r = new JsonObject();
        r.addProperty("status", "success");
        r.addProperty("fecha_consulta",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Test de conexión a la BD
        boolean bdOk = false;
        String bdVersion = "N/A";
        try (Connection conn = ConexionDB.MetodoConectar()) {
            bdOk = conn != null && !conn.isClosed();
            if (bdOk) {
                try (ResultSet rs = conn.createStatement().executeQuery("SELECT VERSION()")) {
                    if (rs.next()) bdVersion = rs.getString(1);
                }
            }
        } catch (SQLException e) {
            bdVersion = "Error: " + e.getMessage();
        }

        JsonObject bd = new JsonObject();
        bd.addProperty("estado", bdOk ? "✅ Conectado" : "❌ Sin conexión");
        bd.addProperty("version", bdVersion);
        r.add("base_datos", bd);

        // Info del servidor
        JsonObject servidor = new JsonObject();
        servidor.addProperty("java_version", System.getProperty("java.version"));
        servidor.addProperty("os", System.getProperty("os.name"));
        servidor.addProperty("memoria_libre_mb",
            Runtime.getRuntime().freeMemory() / (1024 * 1024) + " MB");
        servidor.addProperty("memoria_total_mb",
            Runtime.getRuntime().totalMemory() / (1024 * 1024) + " MB");
        r.add("servidor", servidor);

        return r;
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