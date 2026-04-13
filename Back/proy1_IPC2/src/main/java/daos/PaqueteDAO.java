/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daos;

import modelo.Paquete;
import modelo.ServicioPaquete;
import otros.ConnectionMySQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jeffm
 */
public class PaqueteDAO {
    
    ConnectionMySQL connMySQL = new ConnectionMySQL();
    Connection conn = null;
    
    public PaqueteDAO(){
            conn = connMySQL.conectar();
    }
    
    public List<Paquete> obtenerTodos() throws SQLException {
        List<Paquete> lista = new ArrayList<>();
        String sql = "SELECT p.*, d.nombre AS dest_nombre, d.pais AS dest_pais, " +
                     "COALESCE(SUM(sp.costo_proveedor),0) AS costo_total, " +
                     "p.precio_venta - COALESCE(SUM(sp.costo_proveedor),0) AS ganancia " +
                     "FROM paquete p JOIN destino d ON p.id_destino=d.id_destino " +
                     "LEFT JOIN servicios_paquete sp ON p.id_paquete=sp.id_paquete " +
                     "GROUP BY p.id_paquete ORDER BY p.nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;                
    }
    
    public List<Paquete> obtenerActivos() throws SQLException {
        List<Paquete> lista = new ArrayList<>();
        String sql = "SELECT p.*, d.nombre AS dest_nombre, d.pais AS dest_pais, " +
                     "COALESCE(SUM(sp.costo_proveedor),0) AS costo_total, " +
                     "p.precio_venta - COALESCE(SUM(sp.costo_proveedor),0) AS ganancia " +
                     "FROM paquete p JOIN destino d ON p.id_destino=d.id_destino " + 
                     "LEFT JOIN servicios_paquete sp ON p.id_paquete=sp.id_paquete " + 
                     "WHERE p.activo=TRUE " + 
                     "GROUP BY p.id_paquete ORDER BY p.nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }
    
    public List<Paquete> obtenerPorDestino(int idDestino) throws SQLException {
        List<Paquete> lista = new ArrayList<>();
        String sql = "SELECT p.*, d.nombre AS dest_nombre, d.pais AS dest_pais, " +
                     "COALESCE(SUM(sp.costo_proveedor),0) AS costo_total, " +
                     "p.precio_venta - COALESCE(SUM(sp.costo_proveedor),0) AS ganancia " +
                     "FROM paquete p JOIN destino d ON p.id_destino=d.id_destino " + 
                     "LEFT JOIN servicios_paquete sp ON p.id_paquete=sp.id_paquete " + 
                     "WHERE p.id_destino=? AND p.activo=TRUE " + 
                     "GROUP BY p.id_paquete ORDER BY p.nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, idDestino);
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()) lista.add(map(rs));
            }                                                   
        }
        return lista;
    }
    
    public Paquete obtenerPorId(int id) throws SQLException {        
        String sql = "SELECT p.*, d.nombre AS dest_nombre, d.pais AS dest_pais, " +
                     "COALESCE(SUM(sp.costo_proveedor),0) AS costo_total, " +
                     "p.precio_venta - COALESCE(SUM(sp.costo_proveedor),0) AS ganancia " +
                     "FROM paquete p JOIN destino d ON p.id_destino=d.id_destino " + 
                     "LEFT JOIN servicios_paquete sp ON p.id_paquete=sp.id_paquete " + 
                     "WHERE p.id_paquete=? GROUP BY p.id_paquete";                    
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) return map(rs);
            }                                                   
        }
        return null;
    }
    
    public Paquete obtenerPorNombre(String nombre) throws SQLException {        
        String sql = "SELECT p.*, d.nombre AS dest_nombre, d.pais AS dest_pais, " +
                     "COALESCE(SUM(sp.costo_proveedor),0) AS costo_total, " +
                     "p.precio_venta - COALESCE(SUM(sp.costo_proveedor),0) AS ganancia " +
                     "FROM paquete p JOIN destino d ON p.id_destino=d.id_destino " + 
                     "LEFT JOIN servicios_paquete sp ON p.id_paquete=sp.id_paquete " + 
                     "WHERE p.nombre=? GROUP BY p.id_paquete";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) return map(rs);
            }                                                   
        }
        return null;
    }
    
    public List<Paquete> obtenerAltaDemanda() throws SQLException {
        List<Paquete> lista = new ArrayList<>();
        String sql = "SELECT p.*, d.nombre AS dest_nombre, d.pais AS dest_pais, " +
                     "COALESCE(SUM(sp.costo_proveedor),0) AS costo_total, " +
                     "p.precio_venta - COALESCE(SUM(sp.costo_proveedor),0) AS ganancia " +
                     "FROM paquete p JOIN destino d ON p.id_destino=d.id_destino " + 
                     "LEFT JOIN servicios_paquete sp ON p.id_paquete=sp.id_paquete " + 
                     "WHERE p.activo=TRUE AND (" +
                     "  SELECT COUNT(*) FROM reservacion r " +
                     "  WHERE r.id_paquete=p.id_paquete AND r.estado IN ('PENDIENTE','CONFIRMADA') AND r.fecha_viaje>=CURDATE()" +
                     ") * 100.0 / p.capacidad_maxima > 80 " +
                     "GROUP BY p.id_paquete ORDER BY p.nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);            
            ResultSet rs = ps.executeQuery()){
            while (rs.next()) lista.add(map(rs));                                                               
        }
        return lista;
    }
    
    public int ingresar(Paquete p) throws SQLException {
        String sql = "INSERT INTO paquete (nombre,id_destino,duracion_dias,descripcion,precio_venta,capacidad_maxima,activo) VALUES (?,?,?,?,?,?,TRUE)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());      
            ps.setInt(2, p.getIdDestino());
            ps.setInt(3, p.getDuracionDias());   
            ps.setString(4, p.getDescripcion());
            ps.setDouble(5, p.getPrecioVenta()); 
            ps.setInt(6, p.getCapacidadMaxima());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) { if (gk.next()) return gk.getInt(1); }
        }
        return -1;
    }

    public boolean actualizar(Paquete p) throws SQLException {
        String sql = "UPDATE paquete SET nombre=?,id_destino=?,duracion_dias=?,descripcion=?,precio_venta=?,capacidad_maxima=?,activo=? WHERE id_paquete=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());      
            ps.setInt(2, p.getIdDestino());
            ps.setInt(3, p.getDuracionDias());   
            ps.setString(4, p.getDescripcion());
            ps.setDouble(5, p.getPrecioVenta()); 
            ps.setInt(6, p.getCapacidadMaxima());
            ps.setBoolean(7, p.isActivo());      
            ps.setInt(8, p.getIdPaquete());
            return ps.executeUpdate() > 0;
        }
    }
    
    private Paquete map (ResultSet rs) throws SQLException{
        Paquete p = new Paquete();
        p.setIdPaquete(rs.getInt("id_paquete"));
        p.setNombre(rs.getString("nombre"));
        p.setIdDestino(rs.getInt("id_destino"));
        p.setDestinoNombre(rs.getString("dest_nombre"));
        p.setDestinoPais(rs.getString("dest_pais"));
        p.setDuracionDias(rs.getInt("duracion_dias"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecioVenta(rs.getDouble("precio_venta"));
        p.setCostoTotalProveedores(rs.getDouble("costo_total"));
        p.setGananciaBruta(rs.getDouble("ganancia"));
        p.setCapacidadMaxima(rs.getInt("capacidad_maxima"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }            
}
