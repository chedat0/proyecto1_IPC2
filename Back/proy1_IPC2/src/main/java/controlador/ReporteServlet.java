/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import otros.BaseServlet;
import otros.ConnectionMySQL;
import java.sql.*;
import java.util.HashMap;

/**
 *
 * @author jeffm
 */
@WebServlet(name = "ReporteServlet", urlPatterns = {"/api/reportes/*"})
public class ReporteServlet extends BaseServlet {
  
    private final ConnectionMySQL connMySQL = new ConnectionMySQL();   
            
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path   = req.getPathInfo();
        String desde  = req.getParameter("desde");
        String hasta  = req.getParameter("hasta");

        // Convertir a timestamps para SQL
        String tsDesde = (desde != null && !desde.isBlank()) ? desde + " 00:00:00" : null;
        String tsHasta = (hasta != null && !hasta.isBlank()) ? hasta + " 23:59:59" : null;

        try (Connection conn = connMySQL.conectar()) {
            switch (path == null ? "/" : path) {
                case "/ventas"        -> sendOk(resp, reporteVentas(conn, tsDesde, tsHasta));
                case "/cancelaciones" -> sendOk(resp, reporteCancelaciones(conn, tsDesde, tsHasta));
                case "/ganancias"     -> sendOk(resp, reporteGanancias(conn, tsDesde, tsHasta));
                case "/agente-ventas" -> sendOk(resp, reporteAgenteVentas(conn, tsDesde, tsHasta));
                case "/agente-ganancias"-> sendOk(resp, reporteAgenteGanancias(conn, tsDesde, tsHasta));
                case "/paquete-mas-vendido"   -> sendOk(resp, reportePaqueteVendido(conn, tsDesde, tsHasta, true));
                case "/paquete-menos-vendido" -> sendOk(resp, reportePaqueteVendido(conn, tsDesde, tsHasta, false));
                case "/ocupacion-destino"     -> sendOk(resp, reporteOcupacion(conn, tsDesde, tsHasta));
                default -> sendNotFound(resp, "Reporte no encontrado.");
            }
        } catch (Exception e) {
            sendServerError(resp, e.getMessage());
        }
    }
    
    
  
    // 1. Ventas en intervalo
    private List<Map<String,Object>> reporteVentas(Connection c, String desde, String hasta) throws Exception {
        StringBuilder sql = new StringBuilder(
            "SELECT r.numero_reservacion, r.fecha_creacion, r.fecha_viaje, r.costo_total, r.estado, " +
            "p.nombre AS paquete, d.nombre AS destino, u.nombre_completo AS agente, " +
            "GROUP_CONCAT(cl.nombre_completo SEPARATOR ', ') AS pasajeros " +
            "FROM reservacion r " +
            "JOIN paquete p ON r.id_paquete=p.id_paquete " +
            "JOIN destino d ON p.id_destino=d.id_destino " +
            "JOIN usuario u ON r.id_agente=u.id_usuario " +
            "LEFT JOIN reservacion_pasajero rp ON r.id_reservacion=rp.id_reservacion " +
            "LEFT JOIN cliente cl ON rp.id_cliente=cl.id_cliente " +
            "WHERE r.estado='CONFIRMADA'");
        agregarRango(sql, "r.fecha_creacion", desde, hasta);
        sql.append(" GROUP BY r.id_reservacion ORDER BY r.fecha_creacion DESC");
        return queryList(c, sql.toString());
    }

    // 2. Cancelaciones en intervalo
    private List<Map<String,Object>> reporteCancelaciones(Connection c, String desde, String hasta) throws Exception {
        StringBuilder sql = new StringBuilder(
            "SELECT r.numero_reservacion, can.fecha_cancelacion, can.monto_pagado, " +
            "can.porcentaje_reembolso, can.monto_reembolso, can.perdida_agencia, can.motivo, " +
            "p.nombre AS paquete " +
            "FROM cancelacion can " +
            "JOIN reservacion r ON can.id_reservacion=r.id_reservacion " +
            "JOIN paquete p ON r.id_paquete=p.id_paquete WHERE 1=1");
        agregarRango(sql, "can.fecha_cancelacion", desde, hasta);
        sql.append(" ORDER BY can.fecha_cancelacion DESC");
        return queryList(c, sql.toString());
    }

    // 3. Ganancias en intervalo
    private Map<String,Object> reporteGanancias(Connection c, String desde, String hasta) throws Exception {
        StringBuilder sqlVentas = new StringBuilder(
            "SELECT COALESCE(SUM(r.costo_total - COALESCE(sp_sum.costo,0)),0) AS ganancia_bruta " +
            "FROM reservacion r " +
            "JOIN paquete p ON r.id_paquete=p.id_paquete " +
            "LEFT JOIN (SELECT id_paquete, SUM(costo_proveedor) AS costo FROM servicios_paquete GROUP BY id_paquete) sp_sum ON sp_sum.id_paquete=p.id_paquete " +
            "WHERE r.estado='CONFIRMADA'");
        agregarRango(sqlVentas, "r.fecha_creacion", desde, hasta);

        StringBuilder sqlRemb = new StringBuilder(
            "SELECT COALESCE(SUM(can.monto_reembolso),0) AS total_reembolsos FROM cancelacion can WHERE 1=1");
        agregarRango(sqlRemb, "can.fecha_cancelacion", desde, hasta);

        Map<String,Object> res = new HashMap<>();
        try (PreparedStatement ps = c.prepareStatement(sqlVentas.toString()); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) res.put("gananciaBruta", rs.getBigDecimal("ganancia_bruta"));
        }
        try (PreparedStatement ps = c.prepareStatement(sqlRemb.toString()); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) res.put("totalReembolsos", rs.getBigDecimal("total_reembolsos"));
        }
        java.math.BigDecimal gb = (java.math.BigDecimal) res.getOrDefault("gananciaBruta", java.math.BigDecimal.ZERO);
        java.math.BigDecimal tr = (java.math.BigDecimal) res.getOrDefault("totalReembolsos", java.math.BigDecimal.ZERO);
        res.put("gananciaNeta", gb.subtract(tr));
        return res;
    }

    // 4. Agente con más ventas
    private List<Map<String,Object>> reporteAgenteVentas(Connection c, String desde, String hasta) throws Exception {
        StringBuilder sql = new StringBuilder(
            "SELECT u.nombre_completo AS agente, u.usuario, COUNT(r.id_reservacion) AS total_reservaciones, " +
            "COALESCE(SUM(r.costo_total),0) AS monto_total " +
            "FROM reservacion r JOIN usuario u ON r.id_agente=u.id_usuario WHERE r.estado='CONFIRMADA'");
        agregarRango(sql, "r.fecha_creacion", desde, hasta);
        sql.append(" GROUP BY u.id_usuario ORDER BY monto_total DESC");
        return queryList(c, sql.toString());
    }

    // 5. Agente con más ganancias (por ganancia bruta de paquetes que vendió)
    private List<Map<String,Object>> reporteAgenteGanancias(Connection c, String desde, String hasta) throws Exception {
        StringBuilder sql = new StringBuilder(
            "SELECT u.nombre_completo AS agente, u.usuario, " +
            "COALESCE(SUM(r.costo_total - COALESCE(sp_sum.costo,0)),0) AS ganancia_generada " +
            "FROM reservacion r " +
            "JOIN usuario u ON r.id_agente=u.id_usuario " +
            "LEFT JOIN (SELECT id_paquete, SUM(costo_proveedor) AS costo FROM servicios_paquete GROUP BY id_paquete) sp_sum ON sp_sum.id_paquete=r.id_paquete " +
            "WHERE r.estado='CONFIRMADA'");
        agregarRango(sql, "r.fecha_creacion", desde, hasta);
        sql.append(" GROUP BY u.id_usuario ORDER BY ganancia_generada DESC");
        return queryList(c, sql.toString());
    }

    // 6/7. Paquete más/menos vendido
    private Map<String,Object> reportePaqueteVendido(Connection c, String desde, String hasta, boolean mas) throws Exception {
        StringBuilder sqlPaq = new StringBuilder(
            "SELECT p.id_paquete, p.nombre, d.nombre AS destino, COUNT(r.id_reservacion) AS ventas " +
            "FROM reservacion r JOIN paquete p ON r.id_paquete=p.id_paquete " +
            "JOIN destino d ON p.id_destino=d.id_destino WHERE r.estado='CONFIRMADA'");
        agregarRango(sqlPaq, "r.fecha_creacion", desde, hasta);
        sqlPaq.append(" GROUP BY p.id_paquete ORDER BY ventas ").append(mas ? "DESC" : "ASC").append(" LIMIT 1");

        Map<String,Object> res = new HashMap<>();
        try (PreparedStatement ps = c.prepareStatement(sqlPaq.toString()); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                res.put("idPaquete",    rs.getInt("id_paquete"));
                res.put("nombrePaquete",rs.getString("nombre"));
                res.put("destino",      rs.getString("destino"));
                res.put("totalVentas",  rs.getInt("ventas"));
                // Detalle de reservaciones del paquete
                int idPaq = rs.getInt("id_paquete");
                StringBuilder sqlDet = new StringBuilder(
                    "SELECT r.numero_reservacion, r.fecha_viaje, r.costo_total, r.estado, r.cantidad_pasajeros " +
                    "FROM reservacion r WHERE r.id_paquete=" + idPaq + " AND r.estado='CONFIRMADA'");
                agregarRango(sqlDet, "r.fecha_creacion", desde, hasta);
                res.put("reservaciones", queryList(c, sqlDet.toString()));
            }
        }
        return res;
    }

    // 8. Ocupación por destino
    private List<Map<String,Object>> reporteOcupacion(Connection c, String desde, String hasta) throws Exception {
        StringBuilder sql = new StringBuilder(
            "SELECT d.nombre AS destino, d.pais, COUNT(r.id_reservacion) AS total_reservaciones, " +
            "COALESCE(SUM(r.cantidad_pasajeros),0) AS total_pasajeros " +
            "FROM reservacion r " +
            "JOIN paquete p ON r.id_paquete=p.id_paquete " +
            "JOIN destino d ON p.id_destino=d.id_destino WHERE r.estado IN ('CONFIRMADA','COMPLETADA')");
        agregarRango(sql, "r.fecha_viaje", desde, hasta);
        sql.append(" GROUP BY d.id_destino ORDER BY total_reservaciones DESC");
        return queryList(c, sql.toString());
    }
    

    private void agregarRango(StringBuilder sb, String campo, String desde, String hasta) {
        if (desde != null) sb.append(" AND ").append(campo).append(" >= '").append(desde).append("'");
        if (hasta != null) sb.append(" AND ").append(campo).append(" <= '").append(hasta).append("'");
    }

    private List<Map<String,Object>> queryList(Connection c, String sql) throws Exception {
        List<Map<String,Object>> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String,Object> row = new HashMap<>();
                for (int i = 1; i <= cols; i++) row.put(meta.getColumnLabel(i), rs.getObject(i));
                list.add(row);
            }
        }
        return list;
    }
}
