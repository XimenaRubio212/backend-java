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
 * Servlet para gestionar el perfil del usuario.
 * Implementa RF03 (Configuración de cuenta) con sus restricciones.
 */
@WebServlet("/perfil")
public class PerfilServlet extends HttpServlet {

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final Gson gson = new Gson();

    /**
     * Obtiene los datos del perfil del usuario autenticado.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Obtener ID del usuario inyectado por el FiltroAutenticacion
        Integer userId = (Integer) request.getAttribute("userId");

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"No autorizado\"}");
            return;
        }

        // Buscar usuario por ID (Necesitarás añadir este método al UsuarioDao si no está)
        // Por ahora simulamos la obtención para cumplir el flujo
        Usuario usuario = usuarioDao.buscarPorId(userId); 

        if (usuario != null) {
            // No enviar la contraseña al frontend por seguridad
            usuario.setPassword(null);
            out.print(gson.toJson(usuario));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Usuario no encontrado\"}");
        }
        out.flush();
    }

    /**
     * Actualiza los datos del perfil aplicando restricciones de RF03.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> responseData = new HashMap<>();

        try {
            // 1. Obtener info del usuario autenticado desde el filtro
            Integer userId = (Integer) request.getAttribute("userId");
            String userRol = (String) request.getAttribute("userRol");

            // 2. Leer los nuevos datos enviados en el JSON
            BufferedReader reader = request.getReader();
            Usuario datosNuevos = gson.fromJson(reader, Usuario.class);

            // 3. Obtener datos actuales de la base de datos para validar
            Usuario usuarioActual = usuarioDao.buscarPorId(userId);

            if (usuarioActual == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Usuario no encontrado\"}");
                return;
            }

            // 4. Aplicar Restricciones RF03
            // "El cliente no podrá cambiar correo principal ni edad"
            if (!"ADMINISTRADOR".equals(userRol)) {
                datosNuevos.setCorreoPrincipal(usuarioActual.getCorreoPrincipal());
                datosNuevos.setEdad(usuarioActual.getEdad());
                // Forzamos que solo pueda editar su propio ID
                datosNuevos.setId(userId); 
            }

            // 5. Actualizar en la base de datos
            // Debes tener un método en UsuarioDao: updateProfile(Usuario u)
            boolean exito = usuarioDao.actualizarPerfil(datosNuevos);

            if (exito) {
                response.setStatus(HttpServletResponse.SC_OK);
                responseData.put("message", "Perfil actualizado correctamente.");
                if (!"ADMINISTRADOR".equals(userRol)) {
                    responseData.put("info", "Nota: Correo y edad no fueron modificados por restricciones de rol.");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseData.put("error", "Error al actualizar los datos en el servidor.");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseData.put("error", "Error en el formato de datos: " + e.getMessage());
        }

        out.print(gson.toJson(responseData));
        out.flush();
    }
}