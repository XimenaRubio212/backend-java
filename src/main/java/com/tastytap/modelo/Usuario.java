package com.tastytap.modelo;

import java.time.LocalDateTime;

/**
 * Clase que representa al Usuario en el sistema TastyTap.
 * Cumple con los requisitos: RF01, RF18, RF20, RF32, RF34 y RNF17.
 */
public class Usuario {
    private int id;
    private String nombre;
    private int edad;
    private String password; // Se almacenará encriptada con BCrypt (RNF01)
    private String rol;      // "ADMINISTRADOR", "CLIENTE", "PROVEEDOR" (RF02)
    
    // Gestión de Correos (RF20 y RNF11)
    private String correoPrincipal;
    private String correoRespaldo;
    private boolean correoVerificado; // RF32: Bloqueo de inicio si es false
    
    // Gestión de Teléfonos (RF19)
    // Nota: En la DB será una tabla aparte, aquí guardamos el principal para facilitar el modelo
    private String telefonoPrincipal;
    private boolean telefonoVerificado; // RF33
    
    // Preferencias del Cliente (RF34)
    private String preferenciasJson; // Formato JSON flexible
    
    // Trazabilidad (RNF17)
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Constructor vacío (Necesario para frameworks y DAO)
    public Usuario() {}

    // Constructor completo
    public Usuario(int id, String nombre, int edad, String password, String rol, 
                   String correoPrincipal, String correoRespaldo, boolean correoVerificado, 
                   String telefonoPrincipal, String preferenciasJson) {
        this.id = id;
        this.nombre = nombre;
        this.edad = edad;
        this.password = password;
        this.rol = rol;
        this.correoPrincipal = correoPrincipal;
        this.correoRespaldo = correoRespaldo;
        this.correoVerificado = correoVerificado;
        this.telefonoPrincipal = telefonoPrincipal;
        this.preferenciasJson = preferenciasJson;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getCorreoPrincipal() { return correoPrincipal; }
    public void setCorreoPrincipal(String correoPrincipal) { this.correoPrincipal = correoPrincipal; }

    public String getCorreoRespaldo() { return correoRespaldo; }
    public void setCorreoRespaldo(String correoRespaldo) { this.correoRespaldo = correoRespaldo; }

    public boolean isCorreoVerificado() { return correoVerificado; }
    public void setCorreoVerificado(boolean correoVerificado) { this.correoVerificado = correoVerificado; }

    public String getTelefonoPrincipal() { return telefonoPrincipal; }
    public void setTelefonoPrincipal(String telefonoPrincipal) { this.telefonoPrincipal = telefonoPrincipal; }

    public boolean isTelefonoVerificado() { return telefonoVerificado; }
    public void setTelefonoVerificado(boolean telefonoVerificado) { this.telefonoVerificado = telefonoVerificado; }

    public String getPreferenciasJson() { return preferenciasJson; }
    public void setPreferenciasJson(String preferenciasJson) { this.preferenciasJson = preferenciasJson; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    @Override
    public String toString() {
        return "Usuario{" + "id=" + id + ", nombre=" + nombre + ", rol=" + rol + ", correo=" + correoPrincipal + '}';
    }
}