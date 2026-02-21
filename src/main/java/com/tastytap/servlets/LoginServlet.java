package com.tastytap.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tastytap.dao.UsuarioDao;
import com.tastytap.modelo.Usuario;
import com.tastytap.util.TokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * ============================================================
 *  LoginServlet — POST /api/login
 * ============================================================
 *
 *  Recibe nombre + contraseña, valida con BCrypt,
 *  genera un token JWT y devuelve:
 *    - token: el JWT para que el frontend lo guarde
 *    - rol_id: número del rol
 *    - redirectUrl: la página a la que debe ir según su rol
 *    - data: objeto con datos básicos del usuario
 *
 *  El frontend (login.js) ya está listo para consumir esta
 *  respuesta y guardar el token en localStorage.
 *
 *  NO requiere token (es ruta pública, excluida en FiltroAutenticacion).
 * ============================================================
 */
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // 1. Leer los parámetros del formulario
        String nombre = req.getParameter("nombre_login");
        String pass   = req.getParameter("pass_login");

        // 2. Validar que llegaron los datos
        if (nombre == null || nombre.isBlank() || pass == null || pass.isBlank()) {
            resp.setStatus(400);
            out.print(gson.toJson(error("Nombre y contraseña son requeridos.")));
            return;
        }

        // 3. Validar credenciales con BCrypt (en UsuarioDao)
        Usuario usuario = UsuarioDao.validarLogin(nombre.trim(), pass);

        if (usuario == null) {
            // Credenciales incorrectas
            resp.setStatus(401);
            out.print(gson.toJson(error("Usuario o contraseña incorrectos.")));
            return;
        }

        // 4. Generar el token JWT con userId y rolId
        String token = TokenUtil.generarToken(usuario.getId(), usuario.getRol_id());

        // 5. Determinar la URL de redirección según el rol
        //    El frontend recibe esta ruta y hace window.location.href
        String redirectUrl = switch (usuario.getRol_id()) {
            case 1 -> "views/Admin/mantenimiento.html";      // Administrador
            case 2 -> "views/client/historial.html";         // Cliente
            case 3 -> "views/provider/mis-productos.html";   // Proveedor
            default -> "views/public/login.html";
        };

        // 6. Construir la respuesta JSON
        JsonObject data = new JsonObject();
        data.addProperty("id", usuario.getId());
        data.addProperty("nombre", usuario.getNombre());
        data.addProperty("rol_id", usuario.getRol_id());
        data.addProperty("rol_nombre", usuario.getNombreRol());
        data.addProperty("email", usuario.getEmail());
        data.addProperty("telefono", usuario.getTelefono());

        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("status", "success");
        respuesta.addProperty("message", "Bienvenido, " + usuario.getNombre() + "!");
        respuesta.addProperty("token", token);           // JWT para localStorage
        respuesta.addProperty("redirectUrl", redirectUrl); // Dónde redirigir
        respuesta.add("data", data);                     // Datos del usuario

        resp.setStatus(200);
        out.print(gson.toJson(respuesta));
    }

    // Manejar preflight CORS
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    /** Método auxiliar para construir respuestas de error de forma limpia */
    private JsonObject error(String mensaje) {
        JsonObject obj = new JsonObject();
        obj.addProperty("status", "error");
        obj.addProperty("message", mensaje);
        return obj;
    }
}