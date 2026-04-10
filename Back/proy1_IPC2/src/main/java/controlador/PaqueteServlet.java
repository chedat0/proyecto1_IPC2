/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import daos.PaqueteDAO;
import daos.ServicioPaqueteDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import modelo.Paquete;
import otros.BaseServlet;
import java.util.Map;


/**
 *
 * @author jeffm
 */
@WebServlet(name = "PaqueteServlet", urlPatterns = {"/api/paquetes/*"})
public class PaqueteServlet extends BaseServlet {

    private final PaqueteDAO paqueteDAO = new PaqueteDAO();
    private final ServicioPaqueteDAO servicioDAO = new ServicioPaqueteDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {  
        String path = request.getPathInfo();
        try {
            if (path == null || path.equals("/")){
                String soloActivos = request.getParameter("activos");
                String alertas = request.getParameter("altaDemanda");
                if ("true".equals(alertas)) sendOk(response, paqueteDAO.obtenerAltaDemanda());
                else if ("true".equals(soloActivos)) sendOk(response, paqueteDAO.obtenerActivos());
                else sendOk(response, paqueteDAO.obtenerTodos());
                return;
            }
            String[] parts = path.split("/");
            int id = Integer.parseInt(parts[1]);
            if (parts.length >= 3 && "servicios".equals(parts[2])){
                sendOk(response, servicioDAO.obtenerPorPaquete(id));                
            } else {
                Paquete p = paqueteDAO.obtenerPorId(id);
                if (p == null) { sendNotFound(response, "Paquete no encontrado"); return;}
                p.setServicios(servicioDAO.obtenerPorPaquete(id));
                sendOk(response, p);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(response, "ID INVALIDO");
        } catch (Exception e ) {
            sendServerError(response, e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Paquete p = GSON.fromJson(request.getReader(), Paquete.class);
            if(p.getNombre() == null || p.getNombre().isBlank())
            {
                sendBadRequest(response, "El nombre es obligatorio");
                return;
            }
            if (p.getIdDestino() == null){
                sendBadRequest(response, "El destino es obligatorio");
                return;
            }
            if (p.getPrecioVenta() <= 0){
                sendBadRequest(response, "El precio de venta debe ser positivo");
                return;
            }
            if (p.getDuracionDias() <= 0){ 
                sendBadRequest(response, "La duración debe ser mayor a cero."); 
                return; 
            }
            if (p.getCapacidadMaxima() <= 0){
                sendBadRequest(response, "capacidad maxima debe ser positiva");
                return;
            }
            if (paqueteDAO.obtenerPorNombre(p.getNombre()) != null){
                sendBadRequest(response, "Ya existe un paquete con ese nombre");
            }
            int id = paqueteDAO.ingresar(p);
            sendCreated(response, Map.of("idPaquete", id, "mensaje", "Paquete creado correctamente"));    
            return;
        } catch (Exception e){
            sendServerError(response, e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try{
            int id = Integer.parseInt(extractIdFromPath(req.getPathInfo()));
            Paquete p = GSON.fromJson(req.getReader(),  Paquete.class);
            p.setIdPaquete(id);
            if (paqueteDAO.obtenerPorId(id) == null){
                sendNotFound(res, "Paquete no encontrado");
                return;
            }
            paqueteDAO.actualizar(p);
            sendOk(res, Map.of("mensaje", "Paquete actualizado correctamente"));
        } catch (Exception e) {
            sendServerError(res, e.getMessage());
        }
    }
}
