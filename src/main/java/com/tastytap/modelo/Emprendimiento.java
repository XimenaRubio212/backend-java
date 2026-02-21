package com.tastytap.modelo;

import java.time.LocalDateTime;

/**
 * Representa un negocio o emprendimiento en el sistema.
 * Implementa RF04, RF29, RF30 y RF35.
 */
public class Emprendimiento {
    private int id;
    private String nombre;
    private String ciudad;    // RF04: Filtro obligatorio para el catálogo
    private String ubicacion; // RF29: Dirección física
    private String infoEmpresa; // RF29: Información pública/Bio
    private int proveedorId;  // RF29: ID del usuario con rol PROVEEDOR
    private String estado;    // RF30: "ACTIVO", "INACTIVO", "MANTENIMIENTO"
    private LocalDateTime fechaCreacion;

    public Emprendimiento() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getInfoEmpresa() { return infoEmpresa; }
    public void setInfoEmpresa(String infoEmpresa) { this.infoEmpresa = infoEmpresa; }

    public int getProveedorId() { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}