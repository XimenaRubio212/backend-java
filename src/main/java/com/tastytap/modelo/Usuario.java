package com.tastytap.modelo;

public class Usuario {
    private String nombre;
    private int edad;
    private String pass;

    // Constructor vacío
    public Usuario() {}

    // Constructor lleno
    public Usuario(String nombre, int edad, String pass) {
        this.nombre = nombre;
        this.edad = edad;
        this.pass = pass;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    // Método toString CORRECTO (sin duplicados, sin "correo")
    @Override
    public String toString() {
        return "Usuario{nombre='" + nombre + "', edad=" + edad + ", pass='" + pass + "'}";
    }
}