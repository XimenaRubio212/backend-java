package com.tastytap.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tastytap.dao.UsuarioDao;
import com.tastytap.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * ============================================================
 *  RegistroServlet — POST /api/registro
 * ============================================================
 *
 *  Registra un nuevo usuario con validaciones completas:
 *
 *  VALIDACIONES:
 *  1. Todos los campos requeridos presentes
 *  2. La edad es un número válido y >= 13
 *  3. Las contraseñas coinciden (validación server-side, aunque
 *     el frontend ya lo valida también)
 *  4. La contraseña tiene mínimo 6 caracteres
 *  5. El rol es válido (1, 2 o 3)
 *  6. El nombre de usuario no está ya en uso
 *
 *  PARÁMETROS del formulario:
 *  - nombre_registro   Nombre de usuario
 *  - edad_registro     Edad (número)
 *  - pass_registro     Contraseña
 *  - pass_confirmar    Confirmación de contraseña
 *  - rol_id            1=admin, 2=cliente, 3=proveedor
 *  - email_registro    Email (opcional)
 *  - tel_registro      Teléfono (opcional)
 *
 *  NO requiere token (ruta pública).
 * ============================================================
 */
@WebServlet("/api/registro")
public class RegistroServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // --------------------------------------------------
            // 1. LEER PARÁMETROS
            // --------------------------------------------------
            String nombre       = req.getParameter("nombre_registro");
            String edadStr      = req.getParameter("edad_registro");
            String pass         = req.getParameter("pass_registro");
            String passConfirmar = req.getParameter("pass_confirmar");
            String rolStr       = req.getParameter("rol_id");
            String email        = req.getParameter("email_registro");  // Opcional
            String telefono     = req.getParameter("tel_registro");    // Opcional

            // --------------------------------------------------
            // 2. VALIDACIONES
            // --------------------------------------------------

            // 2.1 Campos requeridos
            if (nombre == null || nombre.isBlank()) {
                out.print(gson.toJson(error("El nombre es requerido.")));
                return;
            }
            if (pass == null || pass.isBlank()) {
                out.print(gson.toJson(error("La contraseña es requerida.")));
                return;
            }
            if (passConfirmar == null || passConfirmar.isBlank()) {
                out.print(gson.toJson(error("Debes confirmar tu contraseña.")));
                return;
            }

            // 2.2 Las contraseñas deben ser idénticas
            // Esta validación es crítica: aunque el JS la hace en el frontend,
            // SIEMPRE debe hacerse también en el backend por seguridad.
            if (!pass.equals(passConfirmar)) {
                out.print(gson.toJson(error("Las contraseñas no coinciden.")));
                return;
            }

            // 2.3 Mínimo 6 caracteres en la contraseña
            if (pass.length() < 6) {
                out.print(gson.toJson(error("La contraseña debe tener al menos 6 caracteres.")));
                return;
            }

            // 2.4 Edad válida
            int edad;
            try {
                edad = Integer.parseInt(edadStr);
                if (edad < 13 || edad > 120) {
                    out.print(gson.toJson(error("La edad debe estar entre 13 y 120 años.")));
                    return;
                }
            } catch (NumberFormatException e) {
                out.print(gson.toJson(error("La edad debe ser un número válido.")));
                return;
            }

            // 2.5 Rol válido (1, 2 o 3)
            int rolId;
            try {
                rolId = Integer.parseInt(rolStr);
                if (rolId < 1 || rolId > 3) {
                    out.print(gson.toJson(error("Rol inválido. Debe ser 1, 2 o 3.")));
                    return;
                }
            } catch (NumberFormatException e) {
                // Si no envían rol, se asigna cliente por defecto
                rolId = 2;
            }

            // 2.6 Verificar si el nombre ya está en uso
            if (UsuarioDao.existeNombre(nombre.trim())) {
                out.print(gson.toJson(error("El nombre de usuario '" + nombre + "' ya está en uso.")));
                return;
            }

            // --------------------------------------------------
            // 3. CREAR Y REGISTRAR EL USUARIO
            // La contraseña se hashea con BCrypt dentro del DAO.
            // --------------------------------------------------
            Usuario nuevo = new Usuario(nombre.trim(), edad, pass, rolId);
            boolean exito = UsuarioDao.registrar(nuevo, email, telefono);

            // --------------------------------------------------
            // 4. RESPONDER
            // --------------------------------------------------
            if (exito) {
                JsonObject respuesta = new JsonObject();
                respuesta.addProperty("status", "success");
                respuesta.addProperty("message", "¡Registro exitoso! Ya puedes iniciar sesión.");
                out.print(gson.toJson(respuesta));
            } else {
                out.print(gson.toJson(error("No se pudo registrar el usuario. Intenta de nuevo.")));
            }

        } catch (Exception e) {
            System.err.println("❌ Error en RegistroServlet: " + e.getMessage());
            resp.setStatus(500);
            out.print(gson.toJson(error("Error interno del servidor.")));
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