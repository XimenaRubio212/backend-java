package com.tastytap.servlets;

import com.tastytap.dao.UsuarioDao;
import com.tastytap.modelo.Usuario;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Esta dirección "/registro" es la que usaremos en el HTML
@WebServlet("/registro-usuario")
public class UsuarioServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Capturar los datos que vienen del formulario HTML
        String nombre = request.getParameter("nombre_registro");
        String edadStr = request.getParameter("edad_registro");
        String pass = request.getParameter("pass_registro");
        
        try {
            int edad = Integer.parseInt(edadStr);
            
            // 2. Crear el objeto Usuario (Rol 2 es "cliente" según tu DB)
            Usuario nuevoUsuario = new Usuario(nombre, edad, pass, 2);
            
            // 3. Llamar al DAO para insertar en la base de datos
            boolean insertado = UsuarioDao.insertar(nuevoUsuario);
            
            if (insertado) {
                // Si funciona, lo mandamos al login (Ajusta la ruta según tu proyecto)
                response.sendRedirect("views/public/login.html?registro=exito");
            } else {
                response.sendRedirect("views/public/registro.html?error=db");
            }
            
        } catch (NumberFormatException e) {
            response.sendRedirect("views/public/registro.html?error=datos");
        }
    }
}