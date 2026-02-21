package com.tastytap.servlets;

import com.google.gson.*;
import com.tastytap.dao.UsuarioDao;
import com.tastytap.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * ============================================================
 *  PerfilServlet — /api/perfil
 * ============================================================
 *
 *  GET  /api/perfil       → Devuelve los datos del perfil
 *  PUT  /api/perfil       → Actualiza nombre, edad, email, teléfono
 *  POST /api/perfil/pass  → Cambia la contraseña
 *
 *  REQUIERE TOKEN JWT en el header:
 *    Authorization: Bearer <token>
 *
 *  El FiltroAutenticacion ya validó el token antes de llegar aquí.
 *  El userId viene como atributo del request (puesto por el filtro).
 * ============================================================
 */
@WebServlet("/api/perfil")
public class PerfilServlet extends HttpServlet {

    private final Gson gson = new Gson();

    // ----------------------------------------------------------
    // GET /api/perfil → Obtener datos del perfil
    // ----------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // El filtro ya validó el token y puso el userId en el request
        int usuarioId = (int) req.getAttribute("usuarioId");

        // Buscar el usuario en la BD
        Usuario u = UsuarioDao.obtenerPorId(usuarioId);

        if (u == null) {
            resp.setStatus(404);
            out.print(gson.toJson(error("Usuario no encontrado.")));
            return;
        }

        // Construir respuesta sin exponer la contraseña
        JsonObject data = new JsonObject();
        data.addProperty("id", u.getId());
        data.addProperty("nombre", u.getNombre());
        data.addProperty("edad", u.getEdad());
        data.addProperty("rol_id", u.getRol_id());
        data.addProperty("rol_nombre", u.getNombreRol());
        data.addProperty("email", u.getEmail() != null ? u.getEmail() : "");
        data.addProperty("telefono", u.getTelefono() != null ? u.getTelefono() : "");

        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("status", "success");
        respuesta.add("data", data);

        out.print(gson.toJson(respuesta));
    }

    // ----------------------------------------------------------
    // PUT /api/perfil → Actualizar datos del perfil
    // ----------------------------------------------------------
    // NOTA: Los formularios HTML no soportan PUT nativamente.
    // El frontend envía POST con el parámetro _method=PUT,
    // o bien usamos fetch() con method: 'PUT' directamente.
    // Aquí aceptamos ambos con el doPost que re-enruta.
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // Obtener ID del usuario desde el token (puesto por el filtro)
        int usuarioId = (int) req.getAttribute("usuarioId");

        // Leer parámetros
        String nombre   = req.getParameter("nombre");
        String edadStr  = req.getParameter("edad");
        String email    = req.getParameter("email");
        String telefono = req.getParameter("telefono");

        // Validaciones básicas
        if (nombre == null || nombre.isBlank()) {
            resp.setStatus(400);
            out.print(gson.toJson(error("El nombre no puede estar vacío.")));
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadStr);
            if (edad < 13 || edad > 120) {
                resp.setStatus(400);
                out.print(gson.toJson(error("Edad inválida.")));
                return;
            }
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            out.print(gson.toJson(error("La edad debe ser un número.")));
            return;
        }

        // Construir el objeto Usuario con los nuevos datos
        Usuario u = new Usuario();
        u.setId(usuarioId);
        u.setNombre(nombre.trim());
        u.setEdad(edad);
        u.setEmail(email);
        u.setTelefono(telefono);

        // Actualizar en la BD
        boolean exito = UsuarioDao.actualizarPerfil(u);

        if (exito) {
            JsonObject respuesta = new JsonObject();
            respuesta.addProperty("status", "success");
            respuesta.addProperty("message", "Perfil actualizado correctamente.");
            out.print(gson.toJson(respuesta));
        } else {
            resp.setStatus(500);
            out.print(gson.toJson(error("No se pudo actualizar el perfil.")));
        }
    }

    // ----------------------------------------------------------
    // POST /api/perfil → Re-enruta PUT desde formularios HTML
    // ----------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Algunos clientes envían POST con _method=PUT para simular PUT
        String method = req.getParameter("_method");
        if ("PUT".equalsIgnoreCase(method)) {
            doPut(req, resp);
        } else {
            resp.setStatus(405);
            resp.getWriter().print(gson.toJson(error("Método no permitido.")));
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