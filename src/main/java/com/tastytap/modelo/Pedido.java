package com.tastytap.modelo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Representa la orden de compra general.
 * Cumple con RF08, RF22, RF41 y RNF09.
 */
public class Pedido {
    private int id;
    private int clienteId;        // RF41: Quién realizó el pedido
    private int emprendimientoId; // A qué negocio pertenece
    private double total;         // Sumatoria de los detalles
    private String estado;        // PENDIENTE_PAGO, PAGADO, ENTREGADO, REEMBOLSADO (RF43)
    
    // RF22 / RNF09: Fecha y hora exacta con precisión de segundos
    private LocalDateTime fechaHora; 
    
    // RF23: Desglose detallado de ítems
    private List<DetallePedido> detalles;

    public Pedido() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public int getEmprendimientoId() { return emprendimientoId; }
    public void setEmprendimientoId(int emprendimientoId) { this.emprendimientoId = emprendimientoId; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }
}