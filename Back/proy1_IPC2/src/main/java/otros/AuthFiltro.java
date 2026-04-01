/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package otros;

import com.google.gson.Gson;
import modelo.Usuario;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 *
 * @author jeffm
 */
public class AuthFiltro implements Filter{
    
    private static final Gson GSON = new Gson();

    // Rutas que no requieren autenticación
    private static final String[] PUBLIC_PATHS = {
        "/api/auth/login",
        "/api/auth/logout"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        // Verificar si es ruta pública
        for (String pub : PUBLIC_PATHS) {
            if (path.startsWith(pub)) {
                chain.doFilter(req, res);
                return;
            }
        }

        // Verificar sesión activa
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(GSON.toJson(
                Map.of("error", "Sesión no válida. Por favor inicie sesión.")
            ));
            return;
        }

        // Verificar rol para rutas de administración
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (path.startsWith("/api/usuarios") || path.startsWith("/api/carga")) {
            if (usuarioSesion.getRol() != 3) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(GSON.toJson(
                    Map.of("error", "No tiene permisos para acceder a este recurso.")
                ));
                return;
            }
        }

        // Verificar rol para rutas de operaciones
        if (path.startsWith("/api/destinos") || path.startsWith("/api/proveedores")
            || path.startsWith("/api/paquetes") || path.startsWith("/api/servicios")) {
            if (usuarioSesion.getRol() != 2 && usuarioSesion.getRol() != 3) {
                // Permitir GET a todos los roles
                if (!"GET".equalsIgnoreCase(request.getMethod())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(GSON.toJson(
                        Map.of("error", "Solo el área de Operaciones puede modificar estos datos.")
                    ));
                    return;
                }
            }
        }

        // Verificar rol para reportes
        if (path.startsWith("/api/reportes")) {
            if (usuarioSesion.getRol() != 3) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(GSON.toJson(
                    Map.of("error", "Solo el área Financiera/Administración puede ver reportes.")
                ));
                return;
            }
        }

        chain.doFilter(req, res);
    }

    @Override public void init(FilterConfig fc)  {}
    @Override public void destroy()              {}
}
