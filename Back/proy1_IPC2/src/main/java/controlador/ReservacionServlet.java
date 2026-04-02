/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import com.google.gson.JsonObject;
import daos.ClienteDAO;
import daos.PagoDAO;
import daos.ReservacionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.Pago;
import modelo.Reservacion;
import modelo.Usuario;
import modelo.Cliente;
import otros.BaseServlet;
import servicios.PdfServicio;
import servicios.ReservacionServicio;

/**
 *
 * @author jeffm
 */
@WebServlet(name = "ReservacionServlet", urlPatterns = {"/api/reservaciones/*"})
public class ReservacionServlet extends BaseServlet {

    private final ReservacionServicio reservacionServicio = new ReservacionServicio();
    private final ReservacionDAO reservacionDAO = new ReservacionDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final PdfServicio pdfServicio = new PdfServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                String destino = req.getParameter("idDestino");
                String fecha = req.getParameter("fecha");
                String hoy = req.getParameter("hoy");

                if ("true".equals(hoy)) {
                    sendOk(res, reservacionDAO.obtenerPorFecha(LocalDate.now()));
                } else if (destino != null && fecha != null) {
                    sendOk(res, reservacionDAO.obtenerPorDestinoyFecha(
                            Integer.parseInt(destino), LocalDate.parse(fecha)));
                } else {
                    sendOk(res, reservacionDAO.obtenerTodas());
                }
                return;
            }

            String[] parts = path.split("/");
            int id = Integer.parseInt(parts[1]);

            if (parts.length >= 3) {
                switch (parts[2]) {
                    case "pasajeros" -> {
                        var pasajeros = clienteDAO.obtenerPorReservacion(id);
                        sendOk(res, pasajeros);
                    }
                    case "pagos" ->
                        sendOk(res, pagoDAO.obtenerPorReservacion(id));
                    case "comprobante" ->
                        generarComprobante(req, res, id);
                    default ->
                        sendNotFound(res, "recurso no reconocido.");
                }
            } else {
                Reservacion r = reservacionDAO.obtenerPorId(id);
                if (r == null) {
                    sendNotFound(res, "Reservación no encontrada.");
                    return;
                }
                r.setPasajeros(clienteDAO.obtenerPorReservacion(id));
                sendOk(res, r);
            }

        } catch (NumberFormatException e) {
            sendBadRequest(res, "ID inválido.");
        } catch (Exception e) {
            sendServerError(res, e.getMessage());
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        try {
            if (path != null && path.contains("/cancelar")) {
                int id = Integer.parseInt(path.split("/")[1]);
                JsonObject body = GSON.fromJson(req.getReader(), JsonObject.class);
                String motivo = body.has("motivo") ? body.get("motivo").getAsString() : "";
                var can = reservacionServicio.procesarCancelacion(id, motivo);
                sendOk(resp, can);
                return;
            }

            JsonObject body = GSON.fromJson(req.getReader(), JsonObject.class);

            if (!body.has("idPaquete") || !body.has("fechaViaje") || !body.has("pasajeros")) {
                sendBadRequest(resp, "Faltan campos: idPaquete, fechaViaje, pasajeros.");
                return;
            }

            int idPaquete = body.get("idPaquete").getAsInt();
            String fechaViaje = body.get("fechaViaje").getAsString();

            List<Integer> idsPasajeros = new ArrayList<>();
            body.get("pasajeros").getAsJsonArray().forEach(e -> idsPasajeros.add(e.getAsInt()));

            Usuario agente = (Usuario) req.getSession().getAttribute("usuario");
            Reservacion nueva = reservacionServicio.crearReservacion(idPaquete, agente.getIdUsuario(), fechaViaje, idsPasajeros);
            nueva.setPasajeros(clienteDAO.obtenerPorReservacion(nueva.getIdReservacion()));
            sendCreated(resp, nueva);

        } catch (IllegalArgumentException | IllegalStateException e) {
            sendBadRequest(resp, e.getMessage());
        } catch (Exception e) {
            sendServerError(resp, e.getMessage());
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws jakarta.servlet.ServletException, IOException {

        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String path = req.getPathInfo();

            if (path == null || path.equals("/")) {
                sendBadRequest(resp, "Debe indicar el ID de la reservación.");
                return;
            }

            int id = Integer.parseInt(extractIdFromPath(path));

            JsonObject body = GSON.fromJson(req.getReader(), JsonObject.class);

            if (!body.has("estado")) {
                sendBadRequest(resp, "El campo estado es obligatorio.");
                return;
            }

            String estado = body.get("estado").getAsString();

            if (!estado.equals("PENDIENTE") && !estado.equals("CONFIRMADA")
                    && !estado.equals("CANCELADA") && !estado.equals("COMPLETADA")) {
                sendBadRequest(resp, "Estado inválido. Use: PENDIENTE, CONFIRMADA, CANCELADA o COMPLETADA.");
                return;
            }

            reservacionDAO.actualizarEstado(id, estado);
            sendOk(resp, Map.of("mensaje", "Estado actualizado correctamente."));

        } catch (NumberFormatException e) {
            sendBadRequest(resp, "ID inválido.");
        } catch (Exception e) {
            sendServerError(resp, e.getMessage());
        }
    }

    private void generarComprobante(HttpServletRequest req, HttpServletResponse resp, int idReservacion) throws Exception {
        Reservacion r = reservacionDAO.obtenerPorId(idReservacion);
        if (r == null) {
            sendNotFound(resp, "Reservación no encontrada.");
            return;
        }
        List<Pago> pagos = pagoDAO.obtenerPorReservacion(idReservacion);
        if (pagos == null) {
            sendBadRequest(resp, "No hay pagos registrados para esta reservación.");
            return;
        }
        List<Cliente> pasajeros = clienteDAO.obtenerPorReservacion(idReservacion);
        byte[] pdf = pdfServicio.generarComprobantePago(r, pagos.get(pagos.size() - 1), pasajeros);
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=comprobante-" + r.getNumeroReservacion() + ".pdf");
        resp.getOutputStream().write(pdf);
    }
}
