/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import com.google.gson.JsonObject;
import daos.UsuarioDAO;
import modelo.Usuario;
import servicios.AuthServicio;
import otros.BaseServlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author jeffm
 */
@WebServlet(name = "UsuarioServlet", urlPatterns = {"/api/usuarios/*"})
public class UsuarioServlet extends BaseServlet {
    
    private final UsuarioDAO  dao  = new UsuarioDAO();
    private final AuthServicio auth = new AuthServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                sendOk(resp, dao.obtenerTodos());
            } else {
                int id = Integer.parseInt(extractIdFromPath(path));
                Usuario u = dao.obtenerPorId(id);
                if (u == null){
                    sendNotFound(resp, "Usuario no encontrado.");
                    return;
                }
                sendOk(resp, u);
            }
        } catch (Exception e) { sendServerError(resp, e.getMessage()); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject body = GSON.fromJson(req.getReader(), JsonObject.class);
            String username = body.get("usuario").getAsString();
            String password = body.get("contraHasheada").getAsString();
            String nombre   = body.get("nombreCompleto").getAsString();
            int    rol      = body.get("rol").getAsInt();

            if (password.length() < 6) { sendBadRequest(resp, "Contraseña mínima 6 caracteres."); return; }
            if (rol < 1 || rol > 3)    { sendBadRequest(resp, "Rol inválido (1, 2 o 3)."); return; }
            if (dao.existeUsername(username)) { sendBadRequest(resp, "Nombre de usuario ya existe."); return; }

            Usuario u = new Usuario(username, auth.hashPassword(password), nombre, rol);
            int id = dao.ingresar(u);
            sendCreated(resp, Map.of("idUsuario", id, "mensaje", "Usuario creado correctamente."));
        } catch (Exception e) { sendServerError(resp, e.getMessage()); }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = Integer.parseInt(extractIdFromPath(req.getPathInfo()));
            JsonObject body = GSON.fromJson(req.getReader(), JsonObject.class);

            Usuario u = dao.obtenerPorId(id);
            if (u == null) { sendNotFound(resp, "Usuario no encontrado."); return; }

            u = new Usuario();
            if (body.has("nombreCompleto")) u.setNombreCompleto(body.get("nombreCompleto").getAsString());
            if (body.has("rol"))            u.setRol(body.get("rol").getAsInt());
            if (body.has("activo"))         u.setActivo(body.get("activo").getAsBoolean());
            dao.actualizar(u);

            // Cambio de contraseña opcional
            if (body.has("contraHasheada") && !body.get("contraHasheada").getAsString().isBlank()) {
                String np = body.get("contraHasheada").getAsString();
                if (np.length() < 6) { sendBadRequest(resp, "Contraseña mínima 6 caracteres."); return; }
                dao.actualizarContra(id, auth.hashPassword(np));
            }
            sendOk(resp, Map.of("mensaje", "Usuario actualizado correctamente."));
        } catch (Exception e) { sendServerError(resp, e.getMessage()); }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = Integer.parseInt(extractIdFromPath(req.getPathInfo()));
            // desactivar usuario
            if (!dao.desactivar(id)) sendNotFound(resp, "Usuario no encontrado.");
            else sendOk(resp, Map.of("mensaje", "Usuario desactivado correctamente."));
        } catch (Exception e) { sendServerError(resp, e.getMessage()); }
    }        
}

