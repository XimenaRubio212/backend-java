package com.tastytap.controller;

import com.google.gson.Gson;
import com.tastytap.dao.ProductoDao; // Necesitaremos crear este DAO a continuación
import com.tastytap.modelo.Producto; // Necesitaremos crear este modelo
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
// import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet para la gestión del catálogo de productos.
 * Implementa RF04 (Filtros en cascada), RF06 (Gestión) y RF14 (Imágenes).
 */
@WebServlet("/productos")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1MB
    maxFileSize = 1024 * 1024 * 5,    // 5MB
    maxRequestSize = 1024 * 1024 * 10 // 10MB
)
public class ProductosServlet extends HttpServlet {

    private final ProductoDao productoDao = new ProductoDao();
    private final Gson gson = new Gson();

    /**
     * RF04: Obtener catálogo con filtros en cascada.
     * URL sugerida: /productos?ciudad=Bogota&emprendimiento=1&categoria=Pizza
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String ciudad = request.getParameter("ciudad");
        String emprendimientoIdStr = request.getParameter("emprendimiento");
        String categoria = request.getParameter("categoria");

        List<Producto> catalogo;

        // Lógica de filtros en cascada (RF04)
        if (ciudad != null && emprendimientoIdStr != null && categoria != null) {
            int empId = Integer.parseInt(emprendimientoIdStr);
            catalogo = productoDao.listarPorCascada(ciudad, empId, categoria);
        } else if (ciudad != null && emprendimientoIdStr != null) {
            int empId = Integer.parseInt(emprendimientoIdStr);
            catalogo = productoDao.listarPorEmprendimiento(empId);
        } else if (ciudad != null) {
            catalogo = productoDao.listarPorCiudad(ciudad);
        } else {
            // Si es un visitante, el sistema obliga a usar filtros según RF04
            // pero permitimos una vista general si es Admin
            catalogo = productoDao.listarTodos();
        }

        // Restricción RF06: Los clientes solo ven productos aprobados/activos
        String userRol = (String) request.getAttribute("userRol");
        if (!"ADMINISTRADOR".equals(userRol)) {
            catalogo = catalogo.stream()
                    .filter(p -> p.isActivo() && p.getUrlImagen() != null)
                    .collect(Collectors.toList());
        }

        response.getWriter().print(gson.toJson(catalogo));
    }

    /**
     * RF06 / RF14: Agregar o modificar productos (Solo Administradores).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String userRol = (String) request.getAttribute("userRol");
        
        // Restricción RF06: Solo administradores modifican el catálogo general
        if (!"ADMINISTRADOR".equals(userRol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"No tiene permisos para modificar el catálogo.\"}");
            return;
        }

        try {
            // Leer datos del formulario (usando Multipart para la imagen RF14)
            String nombre = request.getParameter("nombre");
            double precio = Double.parseDouble(request.getParameter("precio"));
            String categoria = request.getParameter("categoria");
            int emprendimientoId = Integer.parseInt(request.getParameter("emprendimientoId"));
            
            // Gestión de Imagen (RF14)
            Part filePart = request.getPart("imagen");
            if (filePart == null || filePart.getSize() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"RF14: Mínimo una imagen es obligatoria para activar el producto.\"}");
                return;
            }

            // Aquí llamarías a una utilidad para guardar el archivo físicamente
            String urlImagen = "uploads/" + filePart.getSubmittedFileName(); 

            Producto p = new Producto();
            p.setNombre(nombre);
            p.setPrecio(precio);
            p.setCategoria(categoria);
            p.setEmprendimientoId(emprendimientoId);
            p.setUrlImagen(urlImagen);
            p.setActivo(true);

            boolean exito = productoDao.insertar(p);

            if (exito) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write("{\"message\": \"Producto añadido con éxito.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Datos inválidos.\"}");
        }
    }
}