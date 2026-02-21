package com.tastytap.util;

import com.tastytap.modelo.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad para la generación y validación de Tokens JWT.
 * Cumple con RNF01 (Seguridad), RF02 (Sesión) y RF16 (Roles).
 */
public class TokenUtil {

    // IMPORTANTE: En producción, esta clave debe venir de una variable de entorno.
    // Debe tener al menos 32 caracteres para cumplir con el algoritmo HS256.
    private static final String SECRET_STR = "Clave_Secreta_Muy_Segura_Para_TastyTap_2024_@!";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STR.getBytes(StandardCharsets.UTF_8));

    // Tiempo de expiración: 24 horas (en milisegundos)
    private static final long EXPIRATION_TIME = 86_400_000;

    /**
     * Genera un token JWT para un usuario autenticado.
     * Incluye ID, Correo y Rol (RF16).
     */
    public static String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(usuario.getCorreoPrincipal())
                .claim("id", usuario.getId())
                .claim("rol", usuario.getRol()) // RF02: Necesario para redirección por rol
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Valida si un token es auténtico y no ha expirado.
     */
    public static boolean validarToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Token inválido o expirado: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el correo (subject) del token.
     */
    public static String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrae el rol del usuario del token.
     * Fundamental para RF27 (Especificación de funciones por rol).
     */
    public static String getRolFromToken(String token) {
        return getClaims(token).get("rol", String.class);
    }

    /**
     * Extrae el ID del usuario del token.
     */
    public static Integer getIdFromToken(String token) {
        return getClaims(token).get("id", Integer.class);
    }

    private static Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}