package com.tastytap.filtros;

import com.tastytap.util.TokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * ============================================================
 *  FiltroAutenticacion — Guardián de todas las rutas /api/*
 * ============================================================
 *
 *  ¿Cómo funciona un filtro en Jakarta Servlet?
 *  Un filtro se ejecuta ANTES de que la petición llegue al Servlet.
 *  Es como un guardia en la puerta:
 *
 *  Cliente → [FiltroAutenticacion] → [Servlet] → Respuesta
 *
 *  Si el token es válido: deja pasar (chain.doFilter)
 *  Si el token no es válido: devuelve 401 Unauthorized y bloquea
 *
 *  RUTAS EXCLUIDAS (no necesitan token):
 *  - /api/login      → El usuario todavía no tiene token
 *  - /api/registro   → El usuario todavía no tiene token
 * ============================================================
 */
@WebFilter("/api/*") // Se aplica a TODAS las rutas que empiecen con /api/
public class FiltroAutenticacion implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        // --- CABECERAS CORS ---
        // Necesarias para que el navegador permita peticiones desde el frontend
        httpResp.setHeader("Access-Control-Allow-Origin", "*");
        httpResp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // --- PREFLIGHT (OPTIONS) ---
        // El navegador envía primero una petición OPTIONS para preguntar
        // si se permiten las cabeceras. Siempre respondemos que sí.
        if ("OPTIONS".equalsIgnoreCase(httpReq.getMethod())) {
            httpResp.setStatus(HttpServletResponse.SC_OK);
            return; // No continuar con el filtro
        }

        // --- RUTAS PÚBLICAS (no requieren token) ---
        String uri = httpReq.getRequestURI();
        if (uri.endsWith("/api/login") || uri.endsWith("/api/registro")) {
            chain.doFilter(request, response); // Dejar pasar sin validar
            return;
        }

        // --- VALIDAR TOKEN ---
        // 1. Obtener el header Authorization de la petición
        String authHeader = httpReq.getHeader("Authorization");

        // 2. Extraer el token del header (quita el prefijo "Bearer ")
        String token = TokenUtil.extraerTokenDelHeader(authHeader);

        // 3. Si no hay token, rechazar con 401
        if (token == null) {
            httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            httpResp.setContentType("application/json");
            httpResp.getWriter().write(
                "{\"status\":\"error\", \"message\":\"Token no proporcionado. Inicia sesión.\"}"
            );
            return; // Bloquear — no llega al Servlet
        }

        // 4. Validar que el token sea auténtico y no esté expirado
        Claims claims = TokenUtil.validarToken(token);

        if (claims == null) {
            httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            httpResp.setContentType("application/json");
            httpResp.getWriter().write(
                "{\"status\":\"error\", \"message\":\"Token inválido o expirado. Inicia sesión de nuevo.\"}"
            );
            return; // Bloquear
        }

        // 5. Si el token es válido, guardar los datos del usuario en la petición
        // para que el Servlet los pueda usar sin volver a parsear el token
        httpReq.setAttribute("usuarioId", Integer.parseInt(claims.getSubject()));
        httpReq.setAttribute("rolId", ((Number) claims.get("rol")).intValue());

        // 6. Todo OK → dejar pasar al Servlet
        chain.doFilter(request, response);
    }
}