/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import daos.ProveedorDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import modelo.Proveedor;
import otros.BaseServlet;

/**
 *
 * @author jeffm
 */
@WebServlet(name = "ProveedorServlet", urlPatterns = {"/api/proveedores/*"})
public class ProveedorServlet extends BaseServlet {

    private final ProveedorDAO dao = new ProveedorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                sendOk(resp, dao.obtenerTodos());
            } else {
                int id = Integer.parseInt(extractIdFromPath(path));
                Proveedor p = dao.obtenerPorId(id);
                if (p == null) {
                    sendNotFound(resp, "Proveedor no encontrado.");
                } else {
                    sendOk(resp, p);
                }
            }
        } catch (NumberFormatException e) {
            sendBadRequest(resp, "ID inválido.");
        } catch (Exception e) {
            sendServerError(resp, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Proveedor p = GSON.fromJson(req.getReader(), Proveedor.class);
            if (p.getNombre() == null || p.getNombre().isBlank()) {
                sendBadRequest(resp, "Nombre es obligatorio.");
                return;
            }
            if (p.getTipoServicio() < 1 || p.getTipoServicio() > 5) {
                sendBadRequest(resp, "Tipo de servicio inválido (1-5).");
                return;
            }
            if (dao.obtenerPorNombre(p.getNombre()) != null) {
                sendBadRequest(resp, "Ya existe un proveedor con ese nombre.");
                return;
            }
            int id = dao.ingresar(p);
            sendCreated(resp, Map.of("idProveedor", id, "mensaje", "Proveedor creado correctamente."));
        } catch (Exception e) {
            sendServerError(resp, e.getMessage());
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = Integer.parseInt(extractIdFromPath(req.getPathInfo()));
            Proveedor existente = dao.obtenerPorId(id);
            if (existente == null) {
                sendNotFound(resp, "Proveedor no encontrado.");
                return;
            }

            Proveedor p = GSON.fromJson(req.getReader(), Proveedor.class);
            p.setIdProveedor(id);           
            p.setActivo(existente.isActivo());
            dao.actualizar(p);
            sendOk(resp, Map.of("mensaje", "Proveedor actualizado correctamente."));
        } catch (Exception e) {
            sendServerError(resp, e.getMessage());
        }
    }

}
