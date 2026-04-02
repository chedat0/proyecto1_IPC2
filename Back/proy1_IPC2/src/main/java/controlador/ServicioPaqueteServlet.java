/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import daos.ServicioPaqueteDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import modelo.ServicioPaquete;
import otros.BaseServlet;

/**
 *
 * @author jeffm
 */
@WebServlet(name = "ServicioPaqueteServlet", urlPatterns = {"/api/servicios/*"})
public class ServicioPaqueteServlet extends BaseServlet {

    private final ServicioPaqueteDAO dao = new ServicioPaqueteDAO();
      
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {      
        try {
            ServicioPaquete s = GSON.fromJson(req.getReader(), ServicioPaquete.class);
            if (s.getIdPaquete() == null || s.getIdProveedor() == null)
                { 
                    sendBadRequest(res, "idPaquete y idProveedor son obligatorios."); 
                    return; 
                }
            if (s.getCostoProveedor() <= 0){
                sendBadRequest(res, "El costo debe ser mayor a cero");
            }
            
            int id = dao.ingresar(s);
            sendCreated(res, Map.of("idServicio", id, "mensaje", "Servicio agregado correctamente."));
        } catch (Exception e) { 
            sendServerError(res, e.getMessage()); 
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = Integer.parseInt(extractIdFromPath(req.getPathInfo()));
            if (!dao.eliminar(id)) sendNotFound(resp, "Servicio no encontrado.");
            else sendOk(resp, Map.of("mensaje", "Servicio eliminado correctamente."));
        } catch (Exception e) { sendServerError(resp, e.getMessage()); }
    }
}
