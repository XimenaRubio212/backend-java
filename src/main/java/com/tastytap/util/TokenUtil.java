package com.tastytap.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

/**
 * ============================================================
 *  TokenUtil — Utilidad para JWT (JSON Web Tokens)
 * ============================================================
 *
 *  ¿Qué es un JWT?
 *  Un token es un texto firmado que el servidor entrega al usuario
 *  después de hacer login. Ejemplo:
 *
 *  eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.signature
 *       HEADER              PAYLOAD          FIRMA
 *
 *  El usuario lo guarda en localStorage y lo envía en cada petición.
 *  El servidor valida la firma para confirmar que es auténtico.
 *
 *  VENTAJA: No hay que guardar sesiones en el servidor.
 *  El token contiene todo lo necesario: userId, rol, expiración.
 * ============================================================
 */
public class TokenUtil {

    // -------------------------------------------------------
    // CLAVE SECRETA
    // -------------------------------------------------------
    // Esta clave firma el token. Si alguien la conoce, puede
    // fabricar tokens falsos. En producción se debe mover a
    // una variable de entorno (System.getenv("JWT_SECRET")).
    // Debe tener mínimo 256 bits (32 caracteres).
    private static final String SECRET_STRING =
        "TastyTap2025$SuperClaveSecretaParaJWT!XyZ";

    // Convertimos el String a una clave criptográfica HMAC-SHA256
    private static final SecretKey SECRET_KEY =
        Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    // -------------------------------------------------------
    // EXPIRACIÓN: 8 horas en milisegundos
    // Después de este tiempo, el token deja de ser válido
    // y el usuario tendrá que hacer login de nuevo.
    // -------------------------------------------------------
    private static final long EXPIRACION_MS = 8 * 60 * 60 * 1000L;

    // -------------------------------------------------------
    // GENERAR TOKEN
    // -------------------------------------------------------
    /**
     * Crea un token JWT para el usuario que acaba de hacer login.
     *
     * @param usuarioId  ID del usuario en la BD
     * @param rolId      Rol del usuario (1=admin, 2=cliente, 3=proveedor)
     * @return           String con el token firmado
     *
     * Flujo:
     *  1. Se define el "subject" (el userId como identificador único)
     *  2. Se agrega el rolId como "claim" (dato extra en el payload)
     *  3. Se firma con la clave secreta
     *  4. Se establece la fecha de expiración
     */
    public static String generarToken(int usuarioId, int rolId) {
        return Jwts.builder()
            .subject(String.valueOf(usuarioId))   // Quién es (ID del usuario)
            .claim("rol", rolId)                   // Qué rol tiene
            .issuedAt(new Date())                  // Cuándo se generó
            .expiration(new Date(System.currentTimeMillis() + EXPIRACION_MS)) // Cuándo expira
            .signWith(SECRET_KEY)                  // Firmado con nuestra clave
            .compact();                            // Genera el String final
    }

    // -------------------------------------------------------
    // VALIDAR TOKEN
    // -------------------------------------------------------
    /**
     * Verifica que el token sea auténtico, no haya sido modificado
     * y no esté expirado.
     *
     * @param token  El token que envió el cliente en el header
     * @return       Claims (datos del payload) si es válido, null si no
     *
     * Flujo:
     *  1. Se parsea el token con la misma clave secreta
     *  2. Si la firma no coincide → JwtException
     *  3. Si está expirado → ExpiredJwtException
     *  4. Si todo OK → retorna los datos (userId, rol, fechas)
     */
    public static Claims validarToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(SECRET_KEY)  // Usamos la misma clave para verificar
                .build()
                .parseSignedClaims(token)
                .getPayload();           // Retorna los datos del token
        } catch (ExpiredJwtException e) {
            // El token expiró (pasaron las 8 horas)
            System.err.println("⏰ Token expirado: " + e.getMessage());
            return null;
        } catch (JwtException e) {
            // Token inválido, modificado o con firma incorrecta
            System.err.println("❌ Token inválido: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------
    // EXTRAER DATOS DEL TOKEN
    // -------------------------------------------------------
    /**
     * Obtiene el ID del usuario desde el token sin tener que
     * consultar la base de datos.
     *
     * @param token  Token JWT del cliente
     * @return       ID del usuario, o -1 si el token no es válido
     */
    public static int getUsuarioId(String token) {
        Claims claims = validarToken(token);
        if (claims == null) return -1;
        return Integer.parseInt(claims.getSubject());
    }

    /**
     * Obtiene el rol del usuario desde el token.
     * 1 = administrador, 2 = cliente, 3 = proveedor
     *
     * @param token  Token JWT del cliente
     * @return       ID del rol, o -1 si el token no es válido
     */
    public static int getRol(String token) {
        Claims claims = validarToken(token);
        if (claims == null) return -1;
        return ((Number) claims.get("rol")).intValue();
    }

    // -------------------------------------------------------
    // EXTRAER TOKEN DEL HEADER
    // -------------------------------------------------------
    /**
     * El frontend envía el token en el header de esta forma:
     *   Authorization: Bearer eyJhbGci...
     *
     * Este método limpia el prefijo "Bearer " y retorna solo el token.
     *
     * @param authHeader  Valor del header Authorization
     * @return            Token limpio, o null si el formato es incorrecto
     */
    public static String extraerTokenDelHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Quitamos los primeros 7 caracteres ("Bearer ")
        }
        return null;
    }
}