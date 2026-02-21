package com.tastytap.controller;

import com.google.gson.Gson;
import com.tastytap.dao.UsuarioDao;
import com.tastytap.modelo.Usuario;
import com.tastytap.util.TokenUtil;
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
 * Servlet encargado de la autenticación de usuarios.
 * Implementa RF02 (Login y Redirección) y RF32 (Verificación obligatoria).
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

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
            // 1. Leer credenciales del cuerpo JSON
            BufferedReader reader = request.getReader();
            Map<String, String> credentials = gson.fromJson(reader, Map.class);
            
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || password == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseData.put("error", "Email y contraseña son requeridos.");
                out.print(gson.toJson(responseData));
                return;
            }

            // 2. Validar en la base de datos a través del DAO
            // El DAO ya verifica: Existencia, BCrypt y correo_verificado (RF32)
            Usuario usuario = usuarioDao.validarLogin(email, password);

            if (usuario != null) {
                // 3. Generar Token JWT (Contiene ID y Rol)
                String token = TokenUtil.generarToken(usuario);

                // 4. Preparar respuesta con datos para la redirección (RF02)
                response.setStatus(HttpServletResponse.SC_OK);
                responseData.put("token", token);
                responseData.put("nombre", usuario.getNombre());
                responseData.put("rol", usuario.getRol());
                responseData.put("message", "Inicio de sesión exitoso.");
                
                // NOTA PARA EL FRONTEND (RF02): 
                // Si rol == 'CLIENTE' -> Redirigir a /tienda
                // Si rol == 'PROVEEDOR' -> Redirigir a /registro-negocio (si es primera vez)
            } else {
                // Si llegamos aquí, o la clave es mal, o el correo no está verificado (RF32)
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseData.put("error", "Credenciales incorrectas o cuenta no verificada.");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseData.put("error", "Error en el servidor: " + e.getMessage());
        }

        out.print(gson.toJson(responseData));
        out.flush();
    }
}