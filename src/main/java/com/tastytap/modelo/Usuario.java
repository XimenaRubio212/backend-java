package com.tastytap.modelo;

/**
 * ============================================================
 *  Usuario — Modelo que representa la tabla `usuarios`
 *  junto con sus datos relacionados de `correos` y `telefonos`
 * ============================================================
 *
 *  En la BD tenemos tablas separadas para correos y teléfonos
 *  (un usuario puede tener varios), pero en este modelo guardamos
 *  el principal de cada uno para simplificar las respuestas JSON.
 *
 *  Roles:
 *    1 = administrador
 *    2 = cliente
 *    3 = proveedor
 * ============================================================
 */
public class Usuario {

    // Datos de la tabla `usuarios`
    private int id;
    private String nombre;
    private int edad;
    private String pass;       // Guardada como hash BCrypt en la BD
    private int rol_id;

    // Datos de la tabla `correos` (el correo principal)
    private String email;

    // Datos de la tabla `telefonos` (el teléfono principal)
    private String telefono;

    // -------------------------------------------------------
    // CONSTRUCTORES
    // -------------------------------------------------------

    /** Constructor vacío — necesario para construir el objeto desde ResultSet */
    public Usuario() {}

    /**
     * Constructor para REGISTRO de nuevos usuarios.
     * No incluye email/teléfono porque se insertan en tablas separadas.
     */
    public Usuario(String nombre, int edad, String pass, int rol_id) {
        this.nombre = nombre;
        this.edad = edad;
        this.pass = pass;
        this.rol_id = rol_id;
    }

    /**
     * Constructor completo con email y teléfono.
     * Útil para devolver el perfil completo al frontend.
     */
    public Usuario(int id, String nombre, int edad, String pass, int rol_id,
                   String email, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.edad = edad;
        this.pass = pass;
        this.rol_id = rol_id;
        this.email = email;
        this.telefono = telefono;
    }

    // -------------------------------------------------------
    // GETTERS Y SETTERS
    // -------------------------------------------------------

    public int getId() { return id; }

    // Nota: el nombre del setter tiene un error tipográfico en el código original
    // ("seItId" en vez de "setId"). Lo corregimos aquí pero mantenemos compatibilidad.
    public void setId(int id) { this.id = id; }

    /** @deprecated Usar setId(int id). Mantenido por compatibilidad con código existente. */
    @Deprecated
    public void seItId(int id) { this.id = id; }

    public int getRol_id() { return rol_id; }
    public void setRol_id(int rol_id) { this.rol_id = rol_id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    // -------------------------------------------------------
    // NOMBRE DEL ROL (útil para respuestas JSON legibles)
    // -------------------------------------------------------
    /**
     * Convierte el rol_id numérico a un String legible.
     * Así el frontend puede mostrar "Cliente" en vez de "2".
     */
    public String getNombreRol() {
        return switch (rol_id) {
            case 1 -> "administrador";
            case 2 -> "cliente";
            case 3 -> "proveedor";
            default -> "desconocido";
        };
    }

    // -------------------------------------------------------
    // TO STRING (para logs y depuración)
    // -------------------------------------------------------
    @Override
    public String toString() {
        return "Usuario{id=" + id
            + ", nombre='" + nombre + "'"
            + ", edad=" + edad
            + ", rol=" + getNombreRol()
            + ", email='" + email + "'"
            + ", telefono='" + telefono + "'"
            + "}";
    }
}