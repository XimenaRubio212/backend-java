package com.tastytap.modelo;

/**
 * Representa cada ítem dentro de un pedido.
 * Cumple con RF23 y RNF07.
 */
public class DetallePedido {
    private int id;
    private int pedidoId;
    private int productoId;
    private String nombreProducto; // Para mostrar en el historial sin consultar catálogo
    private int cantidad;
    
    // RNF07: Precio unitario fijo al momento de la compra. 
    // No cambia aunque el proveedor suba el precio del producto después.
    private double precioUnitarioFijo; 

    public DetallePedido() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitarioFijo() { return precioUnitarioFijo; }
    public void setPrecioUnitarioFijo(double precioUnitarioFijo) { this.precioUnitarioFijo = precioUnitarioFijo; }
}