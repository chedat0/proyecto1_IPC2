/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daos;

import modelo.Pago;
import otros.ConnectionMySQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jeffm
 */
public class PagoDAO {
    
    ConnectionMySQL connMySQL = new ConnectionMySQL();
    Connection conn = null;
    
    public PagoDAO(){
            conn = connMySQL.conectar();
    }
    
    public List<Pago> obtenerPorReservacion(int idReservacion) throws SQLException {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT pg.*, r.numero_reservacion FROM pago pg " +
                     "JOIN reservacion r ON pg.id_reservacion=r.id_reservacion " +
                     "WHERE pg.id_reservacion=? ORDER BY pg.fecha_pago";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReservacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        }
        return lista;
    }

    public Double totalPagado(int idReservacion) throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto),0) FROM pago WHERE id_reservacion=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReservacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public int ingresar(Pago p) throws SQLException {
        String sql = "INSERT INTO pago (id_reservacion,monto,metodo_pago,fecha_pago,numero_comprobante) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getIdReservacion());
            ps.setDouble(2, p.getMonto());
            ps.setInt(3, p.getMetodoPago());
            ps.setTimestamp(4, p.getFechaPago() != null ? Timestamp.valueOf(p.getFechaPago()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(5, p.getNumeroComprobante());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) { if (gk.next()) return gk.getInt(1); }
        }
        return -1;
    }

    private Pago map(ResultSet rs) throws SQLException {
        Pago p = new Pago();
        p.setIdPago(rs.getInt("id_pago"));
        p.setIdReservacion(rs.getInt("id_reservacion"));
        p.setNumeroReservacion(rs.getString("numero_reservacion"));
        p.setMonto(rs.getDouble("monto"));
        p.setMetodoPago(rs.getInt("metodo_pago"));
        Timestamp ts = rs.getTimestamp("fecha_pago");
        if (ts != null) p.setFechaPago(ts.toLocalDateTime());
        p.setNumeroComprobante(rs.getString("numero_comprobante"));
        return p;
    }
}
