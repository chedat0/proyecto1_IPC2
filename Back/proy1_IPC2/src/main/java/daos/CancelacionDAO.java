/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daos;

import modelo.Cancelacion;
import otros.ConnectionMySQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jeffm
 */
public class CancelacionDAO {
    
    ConnectionMySQL connMySQL = new ConnectionMySQL();
    Connection conn = null;
    
    public CancelacionDAO(){
            conn = connMySQL.conectar();
    }
    
    public int ingresar(Cancelacion can) throws SQLException {
        String sql = "INSERT INTO cancelacion (id_reservacion,motivo,monto_pagado,porcentaje_reembolso,monto_reembolso,perdida_agencia) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, can.getIdReservacion());
            ps.setString(2, can.getMotivo());
            ps.setDouble(3, can.getMontoPagado());
            ps.setDouble(4, can.getPorcentajeReembolso());
            ps.setDouble(5, can.getMontoReembolso());
            ps.setDouble(6, can.getPerdidaAgencia());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) { if (gk.next()) return gk.getInt(1); }
        }
        return -1;
    }

    public Cancelacion obtenerPorReservacion(int idReservacion) throws SQLException {
        String sql = "SELECT can.*, r.numero_reservacion FROM cancelacion can " +
                     "JOIN reservacion r ON can.id_reservacion=r.id_reservacion " +
                     "WHERE can.id_reservacion=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReservacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Cancelacion> obtenerPorRango(String fechaInicio, String fechaFin) throws SQLException {
        List<Cancelacion> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(
            "SELECT can.*, r.numero_reservacion FROM cancelacion can " +
            "JOIN reservacion r ON can.id_reservacion=r.id_reservacion WHERE 1=1");
        if (fechaInicio != null && !fechaInicio.isBlank()) sb.append(" AND can.fecha_cancelacion >= '").append(fechaInicio).append(" 00:00:00'");
        if (fechaFin    != null && !fechaFin.isBlank())    sb.append(" AND can.fecha_cancelacion <= '").append(fechaFin).append(" 23:59:59'");
        sb.append(" ORDER BY can.fecha_cancelacion DESC");
        try (PreparedStatement ps = conn.prepareStatement(sb.toString());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Cancelacion map(ResultSet rs) throws SQLException {
        Cancelacion can = new Cancelacion();
        can.setIdCancelacion(rs.getInt("id_cancelacion"));
        can.setIdReservacion(rs.getInt("id_reservacion"));
        can.setNumeroReservacion(rs.getString("numero_reservacion"));
        can.setMotivo(rs.getString("motivo"));
        can.setMontoPagado(rs.getDouble("monto_pagado"));
        can.setPorcentajeReembolso(rs.getDouble("porcentaje_reembolso"));
        can.setMontoReembolso(rs.getDouble("monto_reembolso"));
        can.setPerdidaAgencia(rs.getDouble("perdida_agencia"));
        Timestamp ts = rs.getTimestamp("fecha_cancelacion");
        if (ts != null) can.setFechaCancelacion(ts.toLocalDateTime());
        return can;
    }
}
