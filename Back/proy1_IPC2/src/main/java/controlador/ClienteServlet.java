/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import daos.ClienteDAO;
import daos.ReservacionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import modelo.Cliente;
import otros.BaseServlet;

/**
 *
 * @author jeffm
 */
@WebServlet(name = "ClienteServlet", urlPatterns = {"/api/clientes/*"})
public class ClienteServlet extends BaseServlet {
   
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ReservacionDAO reservacionDAO = new ReservacionDAO();
    
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                String q = req.getParameter("q");
                if (q != null && !q.isBlank()) sendOk(res, clienteDAO.busqueda(q));
                else sendOk(res, clienteDAO.obtenerTodos());

            } else if (path.matches("/dpi/.+")) {
                String dpi = path.split("/")[2];
                var cl = clienteDAO.obtenerPorDPI(dpi);
                if (cl != null) sendOk(res, cl);
                else sendNotFound(res, "Cliente con DPI " + dpi + " no encontrado.");

            } else {
                int id = Integer.parseInt(extractIdFromPath(path));
                Cliente cl = clienteDAO.obtenerPorId(id);
                if (cl == null) { sendNotFound(res, "Cliente no encontrado."); return; }

                String sub = path.contains("/reservaciones") ? "reservaciones" : null;
                if ("reservaciones".equals(sub)) sendOk(res, reservacionDAO.obtenerPorCliente(id));
                else sendOk(res, cl);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(res, "ID inválido.");
        } catch (Exception e) {
            sendServerError(res, e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            Cliente cl = GSON.fromJson(req.getReader(), Cliente.class);
            if (cl.getDpiPasaporte() == null || cl.getDpiPasaporte().isBlank())
                { sendBadRequest(res, "DPI/Pasaporte es obligatorio."); return; }
            if (cl.getNombreCompleto() == null || cl.getNombreCompleto().isBlank())
                { sendBadRequest(res, "Nombre completo es obligatorio."); return; }
            if (cl.getFechaNacimiento() == null)
                { sendBadRequest(res, "Fecha de nacimiento es obligatoria."); return; }
            if (clienteDAO.obtenerPorDPI(cl.getDpiPasaporte()) != null)
                { sendBadRequest(res, "Ya existe un cliente con ese DPI/Pasaporte."); return; }

            int id = clienteDAO.ingresar(cl);
            sendCreated(res, Map.of("idCliente", id, "mensaje", "Cliente registrado correctamente."));
        } catch (Exception e) {
            sendServerError(res, e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) 
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(extractIdFromPath(req.getPathInfo()));
            Cliente cl = GSON.fromJson(req.getReader(), Cliente.class);
            cl.setIdCliente(id);
            if (clienteDAO.obtenerPorId(id) == null) { sendNotFound(res, "Cliente no encontrado."); return; }
            clienteDAO.actualizar(cl);
            sendOk(res, Map.of("mensaje", "Cliente actualizado correctamente."));
        } catch (NumberFormatException e) {
            sendBadRequest(res, "ID inválido.");
        } catch (Exception e) {
            sendServerError(res, e.getMessage());
        }
    }
}
