/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicios;

import daos.*;
import modelo.*;
import otros.ConnectionMySQL;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 *
 * @author jeffm
 */
public class ReservacionServicio {
    
    private final ReservacionDAO reservacionDAO = new ReservacionDAO();
    private final PaqueteDAO     paqueteDAO     = new PaqueteDAO();
    private final ClienteDAO     clienteDAO     = new ClienteDAO();
    private final PagoDAO        pagoDAO        = new PagoDAO();
    private final CancelacionDAO cancelacionDAO = new CancelacionDAO();
         

    public Reservacion crearReservacion(Integer idPaquete, Integer idAgente,
            String fechaViajeStr, List<Integer> idsPasajeros) throws Exception {

        if (idPaquete == null || idAgente == null || fechaViajeStr == null || idsPasajeros == null || idsPasajeros.isEmpty())
            throw new IllegalArgumentException("Faltan datos obligatorios para crear la reservación.");

        Paquete paquete = paqueteDAO.obtenerPorId(idPaquete);
        if (paquete == null)
            throw new Exception("El Paquete seleccionado no fue encontrado.");

        if (!paquete.isActivo())
            throw new Exception("El paquete seleccionado está inactivo.");

        LocalDate fechaViaje = LocalDate.parse(fechaViajeStr);
        if (fechaViaje.isBefore(LocalDate.now().plusDays(1)))
            throw new Exception("La fecha de viaje debe ser posterior a hoy.");

        if (idsPasajeros.size() > paquete.getCapacidadMaxima())
            throw new Exception("La cantidad de pasajeros supera la capacidad del paquete.");

        // Verificar que todos los clientes existen
        for (Integer idC : idsPasajeros) {
            Cliente cl = clienteDAO.obtenerPorId(idC);
            if (cl == null)
                throw new Exception("Cliente con ID " + idC + " no encontrado.");
        }

        double costoTotal = paquete.getPrecioVenta() * idsPasajeros.size();
        
        Reservacion r = new Reservacion();        
        r.setNumeroReservacion(generarNumero());
        r.setIdPaquete(idPaquete);
        r.setIdAgente(idAgente);
        r.setFechaViaje(fechaViaje);
        r.setCantidadPasajeros(idsPasajeros.size());
        r.setCostoTotal(costoTotal);
        r.setEstado("PENDIENTE");

        int idReservacion = reservacionDAO.ingresar(r);
        for (Integer idC : idsPasajeros) {
            reservacionDAO.agregarPasajero(idReservacion, idC);
        }

        Reservacion nueva = reservacionDAO.obtenerPorId(idReservacion);
        if (nueva == null)
            throw new Exception("Errir al recuperar la reservación creada.");
        return nueva;
    }
    
    public Pago registrarPago(Integer idReservacion, double monto, int metodoPago) throws Exception {
        if (idReservacion == null)
            throw new Exception("El id de reservacion es obligatorio");
        if (monto <= 0)
            throw new Exception("El monto debe ser mayor a cero.");
        if (metodoPago < 1 || metodoPago > 3)
            throw new Exception ("Método de pago inválido (1=Efectivo, 2=Tarjeta, 3=Transferencia");               

        Reservacion r = reservacionDAO.obtenerPorId(idReservacion);
        if (r == null)
            throw new Exception("Reservación no encontrada.");

        if ("CANCELADA".equals(r.getEstado()) || "COMPLETADA".equals(r.getEstado()))
            throw new Exception("No se puede registrar pago en una reservación " + r.getEstado().toLowerCase() + ".");

        double totalPagado = pagoDAO.totalPagado(idReservacion);
        double saldoPendiente = r.getCostoTotal() - totalPagado;
        
        Pago pago = new Pago();
        pago.setIdReservacion(idReservacion);
        pago.setMonto(monto);
        pago.setMetodoPago(metodoPago);
        pago.setFechaPago(LocalDateTime.now());
        pago.setNumeroComprobante("COMP-" + System.currentTimeMillis());

        pagoDAO.ingresar(pago);

        // Verificar si el pago total cubre el costo → Confirmar
        double totalPagadoNuevo = totalPagado + monto;
        if (totalPagadoNuevo >= r.getCostoTotal()) {
            reservacionDAO.actualizarEstado(idReservacion, "CONFIRMADA");
        }

        return pago;
    }
  
    public Cancelacion procesarCancelacion(Integer idReservacion, String motivo) throws Exception {
        Reservacion r = reservacionDAO.obtenerPorId(idReservacion);
        if (r == null)
            throw new Exception("Reservación no encontrada.");

        if (!"PENDIENTE".equals(r.getEstado()) && !"CONFIRMADA".equals(r.getEstado()))
            throw new Exception("Solo se pueden cancelar reservaciones en estado PENDIENTE o CONFIRMADA.");

        long diasDiferencia = ChronoUnit.DAYS.between(LocalDate.now(), r.getFechaViaje());
        if (diasDiferencia < 7)
            throw new Exception("No se puede cancelar con menos de 7 días de anticipación al viaje." + "Días restantes: " + diasDiferencia + ".");

        double porcentaje;
        if (diasDiferencia > 30) porcentaje = 100.0;
        else if (diasDiferencia >= 15)  porcentaje = 70.0;
        else porcentaje = 40.0;
                
        double montoPagado = pagoDAO.totalPagado(idReservacion);
        double montoReembolso = montoPagado * porcentaje / 100.0;
        double perdidaAgencia = montoPagado - montoReembolso;

        Cancelacion can = new Cancelacion();
        can.setIdReservacion(idReservacion);
        can.setMotivo(motivo);
        can.setMontoPagado(montoPagado);
        can.setPorcentajeReembolso(porcentaje);
        can.setMontoReembolso(montoReembolso);
        can.setPerdidaAgencia(perdidaAgencia);

        cancelacionDAO.ingresar(can);
        reservacionDAO.actualizarEstado(idReservacion, "CANCELADA");

        return can;
    }
  

    ConnectionMySQL connMySQL = new ConnectionMySQL();
    Connection conn = null;
    
    public ReservacionServicio(){
            conn = connMySQL.conectar();
    }
    
    private String generarNumero() throws SQLException {
        // Obtener el mayor número existente para generar secuencial
        try (PreparedStatement ps = conn.prepareStatement("SELECT MAX(CAST(SUBSTRING(numero_reservacion,5) AS UNSIGNED)) FROM reservaciones");
             ResultSet rs = ps.executeQuery()) {
            int n = rs.next() ? rs.getInt(1) : 0;
            return String.format("RES-%05d", n + 1);
        }
    }            
}
