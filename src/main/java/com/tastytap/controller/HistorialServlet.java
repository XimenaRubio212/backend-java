package com.tastytap.controller;

import com.google.gson.Gson;
import com.tastytap.dao.PedidoDao;
import com.tastytap.modelo.Pedido;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/historial")
public class HistorialServlet extends HttpServlet {
    private final PedidoDao pedidoDao = new PedidoDao();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer userId = (Integer) request.getAttribute("userId");
        String userRol = (String) request.getAttribute("userRol");
        String format = request.getParameter("format"); // "pdf" o "json"

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        List<Pedido> pedidos;
        // RF10 y RF26: Filtrado por rol
        if ("PROVEEDOR".equals(userRol)) {
            pedidos = pedidoDao.listarPorProveedor(userId);
        } else {
            pedidos = pedidoDao.listarPorCliente(userId);
        }

        if ("pdf".equalsIgnoreCase(format)) {
            // RF26/RF39: Exportación a PDF
            generarPdf(response, pedidos, userRol);
        } else {
            response.setContentType("application/json");
            response.getWriter().print(gson.toJson(pedidos));
        }
    }

    private void generarPdf(HttpServletResponse response, List<Pedido> pedidos, String rol) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=historial.pdf");
        try (Document document = new Document()) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            document.add(new Paragraph("HISTORIAL DE PEDIDOS - TASTYTAP"));
            document.add(new Paragraph("Rol: " + rol));
            document.add(new Paragraph("--------------------------------------------------"));
            for (Pedido p : pedidos) {
                document.add(new Paragraph("Pedido #" + p.getId() + " - Total: $" + p.getTotal() + " - Fecha: " + p.getFechaHora()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}