package com.tastytap.modelo;

public class Usuario {
    private int id;
    private String nombre;
    private String pass;
    private int edad;
    private int rol_id;
    private String email;
    private String telefono;

    // Constructor vacío
    public Usuario() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }
    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
    public int getRol_id() { return rol_id; }
    public void setRol_id(int rol_id) { this.rol_id = rol_id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}