/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daos;

import modelo.ServicioPaquete;
import otros.ConnectionMySQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jeffm
 */
public class ServicioPaqueteDAO {
    
    ConnectionMySQL connMySQL = new ConnectionMySQL();
    Connection conn = null;
    
    public ServicioPaqueteDAO(){
            conn = connMySQL.conectar();
    }
    
    public List<ServicioPaquete> obtenerPorPaquete(int idPaquete) throws SQLException {
        List<ServicioPaquete> lista = new ArrayList<>();
        String sql = "SELECT sp.*, pr.nombre AS proveedor_nombre FROM servicios_paquete sp " +
                     "JOIN proveedor pr ON sp.id_proveedor=pr.id_proveedor WHERE sp.id_paquete=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaquete);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ServicioPaquete s = new ServicioPaquete();
                    s.setIdServicio(rs.getInt("id_servicio"));
                    s.setIdPaquete(rs.getInt("id_paquete"));
                    s.setIdProveedor(rs.getInt("id_proveedor"));
                    s.setProveedorNombre(rs.getString("proveedor_nombre"));
                    s.setDescripcion(rs.getString("descripcion"));
                    s.setCostoProveedor(rs.getDouble("costo_proveedor"));
                    lista.add(s);
                }
            }
        }
        return lista;
    }

    public int ingresar(ServicioPaquete s) throws SQLException {
        String sql = "INSERT INTO servicios_paquete (id_paquete,id_proveedor,descripcion,costo_proveedor) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getIdPaquete()); ps.setInt(2, s.getIdProveedor());
            ps.setString(3, s.getDescripcion()); ps.setDouble(4, s.getCostoProveedor());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) { if (gk.next()) return gk.getInt(1); }
        }
        return -1;
    }

    public boolean eliminar(int idServicio) throws SQLException {
        String sql = "DELETE FROM servicios_paquete WHERE id_servicio=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idServicio);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteByPaquete(int idPaquete) throws SQLException {
        String sql = "DELETE FROM servicios_paquete WHERE id_paquete=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaquete);
            return ps.executeUpdate() >= 0;
        }
    }
}
