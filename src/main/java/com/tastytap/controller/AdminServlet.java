package com.tastytap.controller;

import com.google.gson.Gson;
import com.tastytap.dao.UsuarioDao;
import com.tastytap.dao.PedidoDao;
import com.tastytap.modelo.Pedido;
import com.tastytap.modelo.Usuario;
import com.tastytap.modelo.DetallePedido;
import com.tastytap.util.FacturaUtil;
import com.tastytap.util.EmailUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet central para la administración del sistema.
 * Implementa RF09, RF12, RF16, RF24, RF25 y RF40.
 */
@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final PedidoDao pedidoDao = new PedidoDao();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String userRol = (String) request.getAttribute("userRol");
        if (!"ADMINISTRADOR".equals(userRol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (path == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        switch (path) {
            case "/usuarios":
                // RF16: Gestión de roles
                out.print(gson.toJson(usuarioDao.listarTodos()));
                break;
            case "/balance-total":
                // RF12: Balances financieros
                out.print(gson.toJson(pedidoDao.obtenerEstadisticasGlobales()));
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String userRol = (String) request.getAttribute("userRol");
        if (!"ADMINISTRADOR".equals(userRol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> responseData = new HashMap<>();

        try {
            if ("/cambiar-rol".equals(path)) {
                int id = Integer.parseInt(request.getParameter("usuarioId"));
                String nuevoRol = request.getParameter("rol");
                if (usuarioDao.cambiarRol(id, nuevoRol)) {
                    responseData.put("message", "Rol actualizado con éxito.");
                } else {
                    response.setStatus(500);
                    responseData.put("error", "Error al actualizar rol.");
                }

            } else if ("/validar-pago".equals(path)) {
                // RF24: Validación manual
                int pedidoId = Integer.parseInt(request.getParameter("pedidoId"));
                double montoRecibido = Double.parseDouble(request.getParameter("montoRecibido"));

                // El DAO valida monto (RF25) y descuenta stock (RF05)
                boolean exito = pedidoDao.validarPagoManual(pedidoId, montoRecibido);

                if (exito) {
                    // --- LOGICA AUTOMÁTICA POST-VALIDACIÓN ---
                    
                    // 1. Obtener info necesaria para la factura
                    Pedido pedido = pedidoDao.buscarPorId(pedidoId);
                    Usuario cliente = usuarioDao.buscarPorId(pedido.getClienteId());
                    List<DetallePedido> detalles = pedidoDao.obtenerDetalles(pedidoId);

                    // 2. Generar Factura en PDF (RF09)
                    byte[] pdfFactura = FacturaUtil.generarFacturaPDF(pedido, cliente, detalles);

                    // 3. Enviar notificación y factura por correo (RF09 / RF40)
                    String mensajeEmail = "Hola " + cliente.getNombre() + 
                        ", tu pago ha sido validado. Adjuntamos tu factura electrónica.";
                    
                    EmailUtil.enviarFactura(cliente.getCorreoPrincipal(), 
                                         "Factura de Compra - TastyTap", 
                                         mensajeEmail, pdfFactura);

                    response.setStatus(HttpServletResponse.SC_OK);
                    responseData.put("message", "Pago validado, inventario actualizado y factura enviada al cliente.");
                } else {
                    // RF25: Bloqueo por discrepancia
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    responseData.put("error", "RF25: El monto no coincide. Validación rechazada.");
                }

            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseData.put("error", "Error en la solicitud: " + e.getMessage());
        }

        out.print(gson.toJson(responseData));
        out.flush();
    }
}