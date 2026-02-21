package com.tastytap.modelo;

/**
 * Clase que representa un Producto en el sistema.
 * Implementa RF04, RF05, RF06, RF14 y RF29.
 */
public class Producto {
    private int id;
    private String nombre;
    private String descripcion;
    private double precio; // RNF07: Este precio se copiará al pedido al momento de la compra
    private String categoria; // Tipo de comida (RF04)
    private int stock; // Gestión de inventario (RF05)
    private String urlImagen; // RF14: Obligatorio para activar el producto
    private boolean activo; // RF06: Solo los aprobados son visibles para clientes
    
    // Relaciones (RF29)
    private int emprendimientoId; // ID del negocio al que pertenece
    private String nombreEmprendimiento; // Para mostrar en el catálogo
    private String ciudad; // RF04: Filtro jerárquico por ciudad

    // Constructor vacío
    public Producto() {}

    // Constructor completo
    public Producto(int id, String nombre, String descripcion, double precio, 
                    String categoria, int stock, String urlImagen, boolean activo, 
                    int emprendimientoId, String ciudad) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.stock = stock;
        this.urlImagen = urlImagen;
        this.activo = activo;
        this.emprendimientoId = emprendimientoId;
        this.ciudad = ciudad;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getEmprendimientoId() { return emprendimientoId; }
    public void setEmprendimientoId(int emprendimientoId) { this.emprendimientoId = emprendimientoId; }

    public String getNombreEmprendimiento() { return nombreEmprendimiento; }
    public void setNombreEmprendimiento(String nombreEmprendimiento) { this.nombreEmprendimiento = nombreEmprendimiento; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    @Override
    public String toString() {
        return "Producto{" + "id=" + id + ", nombre=" + nombre + ", precio=" + precio + ", ciudad=" + ciudad + '}';
    }
}