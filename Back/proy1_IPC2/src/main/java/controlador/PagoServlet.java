/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import com.google.gson.JsonObject;
import daos.PagoDAO;
import daos.ReservacionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import modelo.Pago;
import modelo.Reservacion;
import otros.BaseServlet;
import servicios.ReservacionServicio;


/**
 *
 * @author jeffm
 */
@WebServlet(name = "PagoServlet", urlPatterns = {"/api/pagos/*"})
public class PagoServlet extends BaseServlet {

    private final ReservacionServicio resServicio = new ReservacionServicio();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final ReservacionDAO resDAO = new ReservacionDAO();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            String idRes = req.getParameter("idReservacion");
            if (idRes != null) sendOk(res, pagoDAO.obtenerPorReservacion(Integer.parseInt(idRes)));
            else sendBadRequest(res, "Parámetro idReservacion requerido.");
        } catch (Exception e) { sendServerError(res, e.getMessage()); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { 
        try {
            JsonObject body = GSON.fromJson(req.getReader(), JsonObject.class);
            if (!body.has("idReservacion") || !body.has("monto") || !body.has("metodoPago"))
                { sendBadRequest(res, "Campos requeridos: idReservacion, monto, metodoPago."); return; }
            
            int    idRes   = body.get("idReservacion").getAsInt();
            double monto = body.get("monto").getAsDouble();
            int    metodo  = body.get("metodoPago").getAsInt();
            Pago pago = resServicio.registrarPago(idRes, monto, metodo);            
            Reservacion resActual = resDAO.obtenerPorId(idRes);
            Map<String, Object> r = new HashMap<>();
            r.put("pago", pago);
            r.put("reservacion", resActual);
            
            sendCreated(res, r);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendBadRequest(res, e.getMessage());
        } catch (Exception e) { 
            sendServerError(res, e.getMessage()); }
    }

}
