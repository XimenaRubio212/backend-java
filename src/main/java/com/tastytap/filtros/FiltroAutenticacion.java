package com.tastytap.filtros;

import com.tastytap.util.TokenUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FiltroAutenticacion implements Filter {

    // Rutas exactas que no requieren Token (deben coincidir con el mapping del main)
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/login", 
            "/api/auth/registro", 
            "/api/productos"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getServletPath();
        String method = httpRequest.getMethod();

        // 1. CONFIGURACIÓN ROBUSTA DE CORS
        // Esto permite que tu Frontend (en cualquier puerto) consuma la API
        httpResponse.setHeader("Access-Control-Allow-Origin", "*"); // En producción cambia * por tu dominio
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // Manejo de peticiones pre-flight (OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 2. LOG DE RUTAS (Opcional, para depurar en consola)
        System.out.println("Request: " + method + " " + path);

        // 3. VALIDACIÓN DE RUTAS PÚBLICAS
        // Verificamos si la ruta actual empieza con alguna de las públicas
        boolean isPublicPath = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        
        // Especial para GET /api/productos (Permitir ver catálogo sin login)
        if (isPublicPath || (path.startsWith("/api/productos") && "GET".equalsIgnoreCase(method))) {
            chain.doFilter(request, response);
            return;
        }

        // 4. VALIDACIÓN DE TOKEN JWT
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (TokenUtil.validarToken(token)) {
                    httpRequest.setAttribute("userId", TokenUtil.getIdFromToken(token));
                    httpRequest.setAttribute("userRol", TokenUtil.getRolFromToken(token));
                    chain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error validando token: " + e.getMessage());
            }
        }

        // 5. RESPUESTA DE ERROR EN JSON (No texto plano)
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.setContentType("application/json");
        httpResponse.getWriter().write("{\"error\": \"No autorizado\", \"mensaje\": \"Inicie sesión para continuar\"}");
    }

    @Override public void init(FilterConfig filterConfig) {}
    @Override public void destroy() {}
}