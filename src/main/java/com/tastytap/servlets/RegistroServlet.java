package com.tastytap.servlets;

import com.tastytap.dao.UsuarioDao;
import com.tastytap.modelo.Usuario;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/registro-usuario")
public class RegistroServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // CABECERAS CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();

        try {
            // 1. Capturar los datos
            String nombre = req.getParameter("nombre_registro");
            String edadStr = req.getParameter("edad_registro");
            String pass = req.getParameter("pass_registro");

            // Validación básica para evitar errores nulos antes de parsear
            if (nombre == null || edadStr == null || pass == null) {
                throw new NumberFormatException("Faltan parámetros");
            }

            // 2. Convertir edad y crear objeto
            int edad = Integer.parseInt(edadStr);
            Usuario nuevoUsuario = new Usuario(nombre, edad, pass, 2); // Rol 2 = Cliente
            
            // 3. Llamar al DAO
            boolean exito = UsuarioDao.insertar(nuevoUsuario);

            // 4. Responder con JSON (Lógica del primer código)
            if (exito) {
                out.print("{\"status\":\"success\", \"message\":\"¡Registro exitoso!\"}");
            } else {
                out.print("{\"status\":\"error\", \"message\":\"El nombre de usuario ya existe o hubo un error.\"}");
            }
            
        } catch (NumberFormatException e) {
            // Manejo de errores de datos (Lógica del primer código)
            out.print("{\"status\":\"error\", \"message\":\"Datos inválidos.\"}");
        } catch (Exception e) {
            // Manejo de errores generales
            out.print("{\"status\":\"error\", \"message\":\"Error interno del servidor.\"}");
        }
    }
    
    // ✅ AGREGAR ESTE MÉTODO para manejar preflight de CORS
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}