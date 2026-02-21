package com.tastytap.controller;

import com.google.gson.Gson;
import com.tastytap.dao.PedidoDao;
import com.tastytap.dao.EmprendimientoDao;
import com.tastytap.dao.PromocionDao;
import com.tastytap.modelo.Pedido;
import com.tastytap.modelo.DetallePedido;
import com.tastytap.modelo.Emprendimiento;
import com.tastytap.modelo.Promocion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet central para la gestión de compras.
 * Implementa RF01, RF22, RF23, RF35, RF38 y RNF07.
 */
@WebServlet("/pedidos")
public class PedidosServlet extends HttpServlet {

    private final PedidoDao pedidoDao = new PedidoDao();
    private final EmprendimientoDao emprendimientoDao = new EmprendimientoDao();
    private final PromocionDao promocionDao = new PromocionDao();
    private final Gson gson = new Gson();

    /**
     * RF01, RF35, RF38: Crear un nuevo pedido con lógica de promociones.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> responseData = new HashMap<>();

        // RF01: Validar que el usuario esté autenticado (inyectado por Filtro)
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Inicie sesión para comprar (RF01)\"}");
            return;
        }

        try {
            BufferedReader reader = request.getReader();
            Pedido pedidoReq = gson.fromJson(reader, Pedido.class);

            // 1. RF35: Validar que el negocio no esté cerrado o en mantenimiento
            Emprendimiento emp = emprendimientoDao.buscarPorId(pedidoReq.getEmprendimientoId());
            if (emp == null || !"ACTIVO".equals(emp.getEstado())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\": \"Negocio cerrado temporalmente (RF35).\"}");
                return;
            }

            // 2. Preparar cabecera del pedido (RF22: Fecha automática en DAO)
            pedidoReq.setClienteId(userId);
            pedidoReq.setEstado("PENDIENTE_PAGO"); // Inicia flujo RF08

            // 3. Lógica de Precios y Promociones (RF38 y RNF07)
            double totalFinal = 0;
            for (DetallePedido item : pedidoReq.getDetalles()) {
                
                // RF38: Buscar si el producto tiene promociones vigentes
                List<Promocion> promos = promocionDao.obtenerPromocionesVigentesPorProducto(item.getProductoId());
                double descuento = 0;
                
                if (!promos.isEmpty()) {
                    // Aplicamos la promoción más alta encontrada
                    descuento = promos.stream()
                                     .mapToDouble(Promocion::getPorcentajeDescuento)
                                     .max().orElse(0);
                }

                // Calcular precio con descuento
                double precioOriginal = item.getPrecioUnitarioFijo();
                double precioConDescuento = precioOriginal - (precioOriginal * (descuento / 100));
                
                // RNF07: Fijar el precio inmutable para el histórico
                item.setPrecioUnitarioFijo(precioConDescuento);
                totalFinal += (precioConDescuento * item.getCantidad());
            }

            pedidoReq.setTotal(totalFinal);

            // 4. Guardar en Base de Datos (Transaccional)
            boolean exito = pedidoDao.crearPedidoCompleto(pedidoReq);

            if (exito) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                responseData.put("message", "Pedido creado. Total con descuentos (RF38): $" + totalFinal);
                responseData.put("pedidoId", pedidoReq.getId());
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseData.put("error", "Error al procesar el pedido.");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseData.put("error", "Datos de pedido inválidos.");
        }

        out.print(gson.toJson(responseData));
        out.flush();
    }

    /**
     * RF26: Listar pedidos según el rol.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Integer userId = (Integer) request.getAttribute("userId");
        String rol = (String) request.getAttribute("userRol");
        response.setContentType("application/json");

        if ("CLIENTE".equals(rol)) {
            response.getWriter().print(gson.toJson(pedidoDao.listarPorCliente(userId)));
        } else if ("PROVEEDOR".equals(rol)) {
            response.getWriter().print(gson.toJson(pedidoDao.listarPorProveedor(userId)));
        }
    }
}