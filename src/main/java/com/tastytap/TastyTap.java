package com.tastytap;

import com.tastytap.dao.UsuarioDao;

public class TastyTap {
    public static void main(String[] args) {
        System.out.println("\n=== INICIANDO SISTEMA DE CONSULTAS ===\n");

        // 1. Obtener todos los usuarios como texto plano
        System.out.println("--- Lista Completa ---");
        String resultadoCompleto = UsuarioDao.obtenerTodosComoTexto();
        System.out.println(resultadoCompleto);
    }
}