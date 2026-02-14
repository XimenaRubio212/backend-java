package com.tastytap.modelo;

public class Usuario {
    private int id;
    private String nombre;
    private int edad;
    private String pass;
    private int rol_id; /*1 == Admin, 2 == Usuario, 3 == Proveedor*/

    // Constructor vacío
    public Usuario() {}

    // Constructor lleno
    public Usuario(String nombre, int edad, String pass, int rol_id) {
        this.nombre = nombre;
        this.edad = edad;
        this.pass = pass;
        this.rol_id = rol_id;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void seItId(int id) { this.id = id; }

    public int getRol_id() { return rol_id; }
    public void setRol_id(int rol_id) { this.rol_id = rol_id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    // Método toString CORRECTO (sin duplicados, sin "correo")
    @Override
    public String toString() {
        return "Usuario{id = " + id + ", nombre='" + nombre + "', edad=" + edad + ", pass='" + pass + "' + rol=" + rol_id + "}";
    }
}