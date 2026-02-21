package com.tastytap.controller;

import com.google.gson.Gson;
import com.tastytap.dao.UsuarioDao;
import com.tastytap.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet encargado del registro de nuevos usuarios.
 * Implementa RF01, RF18, RF20 y RNF01.
 */
@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> responseData = new HashMap<>();

        try {
            // 1. Leer el cuerpo de la petición (JSON)
            BufferedReader reader = request.getReader();
            Usuario nuevoUsuario = gson.fromJson(reader, Usuario.class);

            // 2. Validaciones de negocio (RF01 y RF18)
            if (nuevoUsuario == null || nuevoUsuario.getCorreoPrincipal() == null || nuevoUsuario.getPassword() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseData.put("error", "Datos incompletos: correo y contraseña son obligatorios.");
                out.print(gson.toJson(responseData));
                return;
            }

            if (nuevoUsuario.getEdad() < 18) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseData.put("error", "Debes ser mayor de edad para registrarte.");
                out.print(gson.toJson(responseData));
                return;
            }

            // 3. Verificar si el correo ya existe (RF20 - Unicidad)
            if (usuarioDao.buscarPorEmail(nuevoUsuario.getCorreoPrincipal()) != null) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                responseData.put("error", "El correo principal ya está registrado.");
                out.print(gson.toJson(responseData));
                return;
            }

            // 4. Asignar rol por defecto si no viene especificado (RF02/RF16)
            if (nuevoUsuario.getRol() == null || nuevoUsuario.getRol().isEmpty()) {
                nuevoUsuario.setRol("CLIENTE");
            }

            // 5. Intentar registrar en la base de datos
            boolean exito = usuarioDao.registrar(nuevoUsuario);

            if (exito) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                responseData.put("message", "Usuario registrado con éxito. Por favor, verifica tu correo.");
                // Nota: Aquí se dispararía el envío de correo de verificación (RF32)
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseData.put("error", "No se pudo completar el registro en la base de datos.");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseData.put("error", "Error procesando la solicitud: " + e.getMessage());
        }

        out.print(gson.toJson(responseData));
        out.flush();
    }
}