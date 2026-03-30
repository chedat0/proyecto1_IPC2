/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daos;

import jakarta.faces.model.SelectItem;
import modelo.Reservacion;
import otros.ConnectionMySQL;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jeffm
 */
public class ReservacionDAO {
    
    ConnectionMySQL connMySQL = new ConnectionMySQL();
    Connection conn = null;
    
    public ReservacionDAO(){
            conn = connMySQL.conectar();
    }
    
    private static final String SELECT_BASE = 
            "SELECT r.*, p.nombre AS paq_nombre, d.nombre AS dest_nombre, " +
            "u.nombre_completo AS agente_nombre, u.usuario AS agente_usuario, " +
            "COALESCE(SUM(pg.monto),0) AS total_pagado, " +
            "r.costo_total - COALESCE(SUM(pg.monto),0) AS saldo_pendiente " +
            "FROM reservacion r " +
            "JOIN paquete p ON r.id_paquete=p.id_paquete " +
            "JOIN destino d ON p.id_destino=d.id_destino " +
            "JOIN usuario u ON r.id_agente=u.id_usuario " +
            "LEFT JOIN pago pg ON r.id_reservacion=pg.id_reservacion ";
    
    public List<Reservacion> obtenerTodas() throws SQLException {
        List<Reservacion> lista = new ArrayList<>();
        String sql = SELECT_BASE + "GROUP BY r.id_reservacion ORDER BY r.fecha_creacion DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()){
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }
    
    public Reservacion obtenerPorId(int id) throws SQLException{
        String sql = SELECT_BASE + "WHERE r.id_reservacion=? GROUP BY r.id_reservacion";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }
    
    public Reservacion obtenerPorNumero(String numero) throws SQLException {
        String sql = SELECT_BASE + "WHERE r.numero_reservacion=? GROUP BY r.id_reservacion";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) return map(rs);
            }            
        }
        return null;
    }
    
    public List<Reservacion> obtenerPorCliente(int idCliente) throws SQLException {
        List<Reservacion> lista = new ArrayList<>();
        String sql = SELECT_BASE + 
                "JOIN reservacion_pasajero rp ON r.id_reservacion=rp.id_reservacion " +
                "WHERE rp.id_cliente=? GROUP BY r.id_reservacion ORDER BY r.fecha_viaje DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()) lista.add(map(rs));
            }
        }
        return lista;
    }
    
    public List<Reservacion> obtenerPorFecha(LocalDate fecha) throws SQLException {
        List<Reservacion> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE DATE(r.fecha_creacion)=? GROUP BY r.id_reservacion ORDER BY r.fecha_creacion";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()) lista.add(map(rs));
            }
        }
        return lista;
    }
    
    public List<Reservacion> obtenerPorDestinoyFecha(int idDestino, LocalDate fecha) throws SQLException {
        List<Reservacion> lista = new ArrayList<>();
        String sql = SELECT_BASE + 
                "WHERE p.id_destino=? AND r.fecha_viaje=? AND r.estado IN ('PENDIENTE','CONFIRMADA') " +
                "GROUP BY r.id_reservacion ORDER BY r.fecha_creacion";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, idDestino);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()) lista.add(map(rs));
            }
        }
        return lista;
    }
    
    public int ingresar(Reservacion r) throws SQLException {
        String sql = "INSERT INTO reservacion (numero_reservacion,id_paquete,id_agente,fecha_viaje,cantidad_pasajeros,costo_total,estado) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, r.getNumeroReservacion());
            ps.setInt(2, r.getIdPaquete());
            ps.setInt(3,r.getIdAgente());
            ps.setDate(4, Date.valueOf(r.getFechaViaje()));
            ps.setInt(5, r.getCantidadPasajeros());
            ps.setDouble(6, r.getCostoTotal());
            ps.setString(7, r.getEstado() != null ? r.getEstado() : "PENDIENTE");
            try (ResultSet gk = ps.getGeneratedKeys()){
                if (gk.next()) return gk.getInt(1);
            }
        }
        return -1;
    }
    
    public void agregarPasajero(int idReservacion, int idCliente) throws SQLException {
        String sql = "INSERT IGNORE INTO reservacion_pasajero (id_reservacion, id_cliente) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, idReservacion);
            ps.setInt(2, idCliente);
            ps.executeUpdate();
        }
    }
    
    public boolean actualizarEstado (int id, String estado) throws SQLException {
        String sql = "UPDATE reservacion SET estado=? WHERE id_reservacion=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }
    
    private Reservacion map(ResultSet rs) throws  SQLException {
        Reservacion r = new Reservacion();
        r.setIdReservacion(rs.getInt("id_reservacion"));
        r.setNumeroReservacion(rs.getString("numero_reservacion"));
        r.setIdPaquete(rs.getInt("id_paquete"));
        r.setNombrePaquete(rs.getString("paq_nombre"));
        r.setNombreDestino(rs.getString("dest_nombre"));
        r.setIdAgente(rs.getInt("id_agente"));
        r.setNombreAgente(rs.getString("agente_nombre"));
        r.setUsuarioAgente(rs.getString("agente_usuario"));
        r.setCantidadPasajeros(rs.getInt("cantidad_pasajeros"));
        r.setCostoTotal(rs.getDouble("costo_total"));
        r.setEstado(rs.getString("estado"));
        r.setTotalPagado(rs.getDouble("total_pagado"));
        r.setSaldoPendiente(rs.getDouble("saldo_pendiente"));
        Date fv = rs.getDate("fecha_viaje");
        if(fv != null) r.setFechaViaje(fv.toLocalDate());
        Timestamp fc = rs.getTimestamp("fecha_creacion");
        if (fc != null) r.setFechaCreacion(fc.toLocalDateTime());
        return r;
    }
}
