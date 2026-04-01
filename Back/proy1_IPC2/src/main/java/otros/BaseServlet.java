/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package otros;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;
import java.util.Map;

/**
 *
 * @author jeffm
 */
public class BaseServlet extends HttpServlet{

    protected static final Gson GSON = GsonConfig.get();

    protected void sendJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON.toJson(body));
    }

    protected void sendOk(HttpServletResponse resp, Object body) throws IOException {
        sendJson(resp, HttpServletResponse.SC_OK, body);
    }

    protected void sendCreated(HttpServletResponse resp, Object body) throws IOException {
        sendJson(resp, HttpServletResponse.SC_CREATED, body);
    }

    protected void sendError(HttpServletResponse resp, int status, String msg) throws IOException {
        sendJson(resp, status, Map.of("error", msg));
    }

    protected void sendBadRequest(HttpServletResponse resp, String msg) throws IOException {
        sendError(resp, HttpServletResponse.SC_BAD_REQUEST, msg);
    }

    protected void sendNotFound(HttpServletResponse resp, String msg) throws IOException {
        sendError(resp, HttpServletResponse.SC_NOT_FOUND, msg);
    }

    protected void sendServerError(HttpServletResponse resp, String msg) throws IOException {
        sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
    }

    protected String extractIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            return null;
        }
        String[] parts = pathInfo.split("/");
        return parts.length >= 2 ? parts[1] : null;
    }
}
