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
import java.util.HashMap;
import java.util.Map;
import otros.BaseServlet;
import otros.ConnectionMySQL;
import java.sql.*;

/**
 *
 * @author jeffm
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/api/dashboard/*"})
public class DashboardServlet extends BaseServlet {
    
    private final ConnectionMySQL connMySQL = new ConnectionMySQL();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
            
        try (Connection c = connMySQL.conectar()) {

            Map<String, Object> stats = new HashMap<>();

            // Total de clientes activos
            stats.put("totalClientes",
                scalar(c, "SELECT COUNT(*) FROM clientes WHERE activo = TRUE"));

            // Total de reservaciones
            stats.put("totalReservaciones",
                scalar(c, "SELECT COUNT(*) FROM reservaciones"));

            // Reservaciones creadas hoy
            stats.put("reservacionesHoy",
                scalar(c, "SELECT COUNT(*) FROM reservaciones WHERE DATE(fecha_creacion) = CURDATE()"));

            // Reservaciones pendientes de pago
            stats.put("reservacionesPendientes",
                scalar(c, "SELECT COUNT(*) FROM reservaciones WHERE estado = 'PENDIENTE'"));

            // Reservaciones confirmadas
            stats.put("reservacionesConfirmadas",
                scalar(c, "SELECT COUNT(*) FROM reservaciones WHERE estado = 'CONFIRMADA'"));

            // Total de paquetes activos
            stats.put("totalPaquetes",
                scalar(c, "SELECT COUNT(*) FROM paquetes WHERE activo = TRUE"));

            // Total de destinos activos
            stats.put("totalDestinos",
                scalar(c, "SELECT COUNT(*) FROM destinos WHERE activo = TRUE"));

            // Ingresos del mes actual (suma de pagos del mes)
            stats.put("ingresosMes", scalarDouble(c,
                "SELECT COALESCE(SUM(p.monto), 0) " +
                "FROM pagos p " +
                "JOIN reservaciones r ON p.id_reservacion = r.id_reservacion " +
                "WHERE MONTH(p.fecha_pago) = MONTH(CURDATE()) " +
                "AND YEAR(p.fecha_pago) = YEAR(CURDATE())"));

            // Paquetes con alta demanda (más del 80% de capacidad ocupada)
            stats.put("paquetesAltaDemanda", scalar(c,
                "SELECT COUNT(*) FROM paquetes p " +
                "WHERE p.activo = TRUE AND (" +
                "   SELECT COUNT(*) FROM reservaciones r " +
                "   WHERE r.id_paquete = p.id_paquete " +
                "   AND r.estado IN ('PENDIENTE','CONFIRMADA') " +
                "   AND r.fecha_viaje >= CURDATE()" +
                ") * 100.0 / p.capacidad_maxima > 80"));

            sendOk(res, stats);

        } catch (Exception e) {
            sendServerError(res, e.getMessage());
        }                
    }
    
    private long scalar(Connection c, String sql) throws Exception {
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        long resultado = 0;
        if (rs.next()) {
            resultado = rs.getLong(1);
        }
        rs.close();
        ps.close();
        return resultado;
    }
    
    private double scalarDouble(Connection c, String sql) throws Exception {
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        double resultado = 0.0;
        if (rs.next()) {
            resultado = rs.getDouble(1);
        }
        rs.close();
        ps.close();
        return resultado;
    }

}
