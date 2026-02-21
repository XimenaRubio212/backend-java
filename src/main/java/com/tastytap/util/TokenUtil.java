package com.tastytap.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class TokenUtil {
    // Generamos una llave segura para HS256
    private static final SecretKey KEY = Keys.hmacShaKeyFor("Tu_Llave_Secreta_De_Al_Menos_32_Caracteres_Aqui".getBytes());
    private static final long EXPIRACION = 86400000; // 24 horas

    public static String createToken(String nombre, String rol_id) {
        return Jwts.builder()
                .subject(nombre)
                .claim("rol", rol_id)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACION))
                .signWith(KEY)
                .compact();
    }
}