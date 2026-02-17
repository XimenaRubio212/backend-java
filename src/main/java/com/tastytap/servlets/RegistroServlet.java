package com.tastytap.servlets;

import com.tastytap.dao.UsuarioDao;
import com.tastytap.modelo.Usuario;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Mantenemos la URL del segundo código como solicitaste
@WebServlet("/registro-usuario")
public class RegistroServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Implementamos la lógica de respuesta del primer código (JSON)
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 1. Capturar los datos
            String nombre = request.getParameter("nombre_registro");
            String edadStr = request.getParameter("edad_registro");
            String pass = request.getParameter("pass_registro");

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
}