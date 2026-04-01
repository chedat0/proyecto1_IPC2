/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import otros.BaseServlet;
import servicios.AuthServicio;
import java.util.Map;
import modelo.Usuario;

/**
 *
 * @author jeffm
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth/*"})
public class AuthServlet extends BaseServlet {
       
    private final AuthServicio authServicio = new AuthServicio();
    private static final com.google.gson.Gson Gson = otros.GsonConfig.get();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {    
        res.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null){
            res.setStatus(401);
            res.getWriter().write(GSON.toJson(Map.of("error", "No hay sesión activa")));
            return;
        }
        Usuario u = (Usuario) session.getAttribute("usuario");
        Map<String, Object> r = new HashMap<>();
        r.put("idUsuario", u.getIdUsuario());
        r.put("usuario", u.getUsuario());
        r.put("nombreCompleto", u.getNombreCompleto());
        r.put("rol", u.getRol());
        r.put("rolNombre", u.getRolNombre());
        res.setStatus(200);
        res.getWriter().write(GSON.toJson(r));
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {        
        res.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        if ("/login".equals(path)) {
            try {
                var body = GSON.fromJson(req.getReader(), Map.class);
                String username = (String) body.get("usuario");
                String password = (String) body.get("contraHasheada");

                if (username == null || password == null) {
                    res.setStatus(400);
                    res.getWriter().write(GSON.toJson(Map.of("error", "Usuario y contraseña son obligatorios.")));
                    return;
                }

                Usuario u = authServicio.login(username, password);
                if (u == null) {
                    res.setStatus(401);
                    res.getWriter().write(GSON.toJson(Map.of("error", "Credenciales incorrectas o usuario inactivo.")));
                    return;
                }
                
                HttpSession session = req.getSession(true);
                session.setAttribute("usuario", u);
                session.setMaxInactiveInterval(3600); // 1 hora

                Map<String, Object> resultado = new HashMap<>();
                resultado.put("idUsuario",      u.getIdUsuario());
                resultado.put("usuario",        u.getUsuario());
                resultado.put("nombreCompleto",  u.getNombreCompleto());
                resultado.put("rol",             u.getRol());
                resultado.put("rolNombre",       u.getRolNombre());
                resultado.put("mensaje",         "Bienvenido, " + u.getNombreCompleto());

                res.setStatus(200);
                res.getWriter().write(GSON.toJson(resultado));

            } catch (Exception e) {
                res.setStatus(500);
                res.getWriter().write(GSON.toJson(Map.of("error", "Error interno: " + e.getMessage())));
            }
        } else if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
            res.setStatus(200);
            res.getWriter().write(GSON.toJson(Map.of("mensaje", "Sesión cerrada correctamente.")));
        } else {
            res.setStatus(404);
            res.getWriter().write(GSON.toJson(Map.of("error", "Endpoint no encontrado.")));
        }
    }   
}
