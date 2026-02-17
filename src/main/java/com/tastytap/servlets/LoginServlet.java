package com.tastytap.servlets;

import com.tastytap.dao.UsuarioDao;
import com.tastytap.modelo.Usuario;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String nombre = request.getParameter("nombre_login");
        String pass = request.getParameter("pass_login");

        Usuario user = UsuarioDao.validarLogin(nombre, pass);

        if(user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogueado", user);
            out.print("{\"status\":\"success\", \"message\":\"Bienvenido " + user.getNombre() + "\"}");
        } else {
            out.print("{\"status\":\"error\", \"message\":\"Usuario o contraseña incorrectos.\"}");
        }
    }
}