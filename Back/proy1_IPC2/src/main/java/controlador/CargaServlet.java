/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import modelo.Usuario;
import otros.BaseServlet;
import servicios.CargaServicio;

/**
 *
 * @author jeffm
 */
@WebServlet(name = "CargaServlet", urlPatterns = {"/api/carga/*"})
@MultipartConfig
public class CargaServlet extends BaseServlet {
   
    private final CargaServicio cargaServicio = new CargaServicio();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            Part filePart = req.getPart("archivo");
            if (filePart == null || filePart.getSize() == 0)
                { sendBadRequest(res, "No se recibió ningún archivo."); return; }

            String fileName = filePart.getSubmittedFileName();
            if (fileName == null || !fileName.toLowerCase().endsWith(".txt"))
                { sendBadRequest(res, "Solo se aceptan archivos .txt"); return; }

            Usuario admin = (Usuario) req.getSession().getAttribute("usuario");
            var resultado = cargaServicio.procesar(filePart.getInputStream(), admin.getIdUsuario());
            sendOk(res, resultado);

        } catch (Exception e) {
            sendServerError(res, "Error al procesar el archivo: " + e.getMessage());
        }
    }

}
