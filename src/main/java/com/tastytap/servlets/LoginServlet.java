package com.tastytap.servlets;

import com.tastytap.dao.UsuarioDao;
import com.tastytap.modelo.Usuario;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // --- CABECERAS CORS ---
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
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

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}