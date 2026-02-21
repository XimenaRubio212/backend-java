package com.tastytap.filtros;

import com.tastytap.util.TokenUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro de seguridad que intercepta las peticiones y valida el token JWT.
 * Cumple con RNF01 (Seguridad), RF01 (Visitantes), RF02 (Login) y RF27 (Roles).
 */
@WebFilter("/*") // Intercepta todas las rutas
public class FiltroAutenticacion implements Filter {

    // Lista de rutas públicas (No requieren Token)
    // RF01 y RF04 permiten que visitantes vean el catálogo y se registren
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/login", 
            "/registro", 
            "/productos", // Permitir ver catálogo (GET)
            "/public"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getServletPath();
        String method = httpRequest.getMethod();

        // 1. Manejo de CORS (Indispensable para conectar con el Frontend)
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");

        if ("OPTIONS".equalsIgnoreCase(method)) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 2. Permitir acceso a rutas públicas o ver catálogo (RF04)
        boolean isPublicPath = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        if (isPublicPath) {
            chain.doFilter(request, response);
            return;
        }

        // 3. Validar Token JWT para rutas protegidas
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (TokenUtil.validarToken(token)) {
                // Extraer información del token para usarla en los Servlets
                String rol = TokenUtil.getRolFromToken(token);
                Integer userId = TokenUtil.getIdFromToken(token);

                // Guardar en el request para que el Servlet sepa quién es el usuario (RF27)
                httpRequest.setAttribute("userId", userId);
                httpRequest.setAttribute("userRol", rol);

                chain.doFilter(request, response);
                return;
            }
        }

        // 4. Si no hay token o es inválido, bloquear acceso
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.getWriter().write("{\"error\": \"Acceso denegado. Se requiere autenticación.\"}");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}