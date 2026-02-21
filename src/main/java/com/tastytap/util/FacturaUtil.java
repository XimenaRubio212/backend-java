package com.tastytap.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tastytap.modelo.Pedido;
import com.tastytap.modelo.DetallePedido;
import com.tastytap.modelo.Usuario;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class FacturaUtil {

    public static byte[] generarFacturaPDF(Pedido pedido, Usuario cliente, List<DetallePedido> detalles) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Encabezado RF09
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            document.add(new Paragraph("TASTYTAP - FACTURA ELECTRÓNICA", titleFont));
            document.add(new Paragraph("Factura N°: " + pedido.getId()));
            document.add(new Paragraph("Fecha: " + pedido.getFechaHora()));
            document.add(new Paragraph("Cliente: " + cliente.getNombre()));
            document.add(new Paragraph("Correo: " + cliente.getCorreoPrincipal()));
            document.add(new Paragraph("------------------------------------------------------------------"));
            document.add(new Paragraph(" "));

            // Tabla de productos (RF23)
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Producto");
            table.addCell("Cantidad");
            table.addCell("Precio Unit.");
            table.addCell("Subtotal");

            for (DetallePedido item : detalles) {
                table.addCell("Producto #" + item.getProductoId());
                table.addCell(String.valueOf(item.getCantidad()));
                table.addCell("$" + item.getPrecioUnitarioFijo());
                table.addCell("$" + (item.getCantidad() * item.getPrecioUnitarioFijo()));
            }

            document.add(table);
            document.add(new Paragraph(" "));
            
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            document.add(new Paragraph("TOTAL A PAGAR: $" + pedido.getTotal(), totalFont));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
}