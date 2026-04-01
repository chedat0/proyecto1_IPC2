/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import daos.DestinoDAO;
import daos.PaqueteDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import modelo.Destino;
import otros.BaseServlet;

/**
 *
 * @author jeffm
 */

@WebServlet(name = "DestinoServlet", urlPatterns = {"/api/destinos/*"})
public class DestinoServlet extends BaseServlet {

    private final DestinoDAO destinoDAO = new DestinoDAO();
    private final PaqueteDAO paqueteDAO = new PaqueteDAO();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                sendOk(resp, destinoDAO.obtenerActivos());
            } else {
                String[] parts = path.split("/");
                int id = Integer.parseInt(parts[1]);
                if (parts.length >= 3 && "paquetes".equals(parts[2])) {
                    sendOk(resp, paqueteDAO.obtenerPorDestino(id));
                } else {
                    Destino d = destinoDAO.obtenerPorId(id);
                    if (d == null) sendNotFound(resp, "Destino no encontrado.");
                    else sendOk(resp, d);
                }
            }
        } catch (NumberFormatException e) { sendBadRequest(resp, "ID inválido.");
        } catch (Exception e) { sendServerError(resp, e.getMessage()); }
    }
   
    
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {    
        
        try {
            Destino d = GSON.fromJson(req.getReader(), Destino.class);
            if (d.getNombre() == null || d.getNombre().isBlank() || d.getPais() == null || d.getPais().isBlank())
                { sendBadRequest(resp, "Nombre y País son obligatorios."); return; }
            if (destinoDAO.obtenerPorNombre(d.getNombre()) != null)
                { sendBadRequest(resp, "Ya existe un destino con ese nombre."); return; }
            int id = destinoDAO.ingresar(d);
            sendCreated(resp, Map.of("idDestino", id, "mensaje", "Destino creado correctamente."));
        } catch (Exception e) { sendServerError(resp, e.getMessage()); }
    }
   
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = Integer.parseInt(extractIdFromPath(req.getPathInfo()));
            Destino d = GSON.fromJson(req.getReader(), Destino.class);
            d.setIdDestino(id);
            if (destinoDAO.obtenerPorId(id) == null) { sendNotFound(resp, "Destino no encontrado."); return; }
            destinoDAO.actualizar(d);
            sendOk(resp, Map.of("mensaje", "Destino actualizado correctamente."));
        } catch (Exception e) { sendServerError(resp, e.getMessage()); }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = Integer.parseInt(extractIdFromPath(req.getPathInfo()));
            if (!destinoDAO.eliminar(id)) sendNotFound(resp, "Destino no encontrado.");
            else sendOk(resp, Map.of("mensaje", "Destino eliminado."));
        } catch (Exception e) { sendServerError(resp, e.getMessage()); }
    }
}
