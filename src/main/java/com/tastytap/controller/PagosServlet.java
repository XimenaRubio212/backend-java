package com.tastytap.controller;

import com.tastytap.dao.PedidoDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.IOException;

@WebServlet("/pagos/comprobante")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1MB
    maxFileSize = 1024 * 1024 * 5,    // 5MB
    maxRequestSize = 1024 * 1024 * 10 // 10MB
)
public class PagosServlet extends HttpServlet {
    private final PedidoDao pedidoDao = new PedidoDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pedidoIdStr = request.getParameter("pedidoId");
        Part filePart = request.getPart("comprobante"); // El archivo enviado (foto/pdf)

        if (pedidoIdStr == null || filePart == null || filePart.getSize() == 0) {
            response.setStatus(400);
            response.getWriter().write("{\"error\": \"Debe adjuntar un comprobante válido (RF36).\"}");
            return;
        }

        // Simulación de guardado en carpeta local (en producción usarías S3 o similar)
        String fileName = "comprobante_" + pedidoIdStr + "_" + System.currentTimeMillis() + ".png";
        String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdir();

        filePart.write(uploadPath + File.separator + fileName);

        // Actualizar base de datos
        int pedidoId = Integer.parseInt(pedidoIdStr);
        if (pedidoDao.subirComprobante(pedidoId, "uploads/" + fileName)) {
            response.getWriter().write("{\"message\": \"Comprobante cargado. Esperando validación del administrador.\"}");
        } else {
            response.setStatus(500);
        }
    }
}