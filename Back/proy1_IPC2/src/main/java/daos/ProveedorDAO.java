/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daos;

import modelo.Proveedor;
import otros.ConnectionMySQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jeffm
 */
public class ProveedorDAO {
    
    ConnectionMySQL connMySQL = new ConnectionMySQL();
    Connection conn = null;
    
    public ProveedorDAO(){
            conn = connMySQL.conectar();
    }
    
    public List<Proveedor> obtenerTodos() throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedor WHERE activo=TRUE ORDER BY nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public Proveedor obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM proveedor WHERE id_proveedor=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public Proveedor obtenerPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM proveedor WHERE nombre=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public int ingresar(Proveedor p) throws SQLException {
        String sql = "INSERT INTO proveedor (nombre,tipo_servicio,pais_operacion,telefono,email,activo) VALUES (?,?,?,?,?,TRUE)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());       
            ps.setInt(2, p.getTipoServicio());
            ps.setString(3, p.getPaisOperacion()); 
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getEmail());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) { if (gk.next()) return gk.getInt(1); }
        }
        return -1;
    }

    public boolean actualizar(Proveedor p) throws SQLException {
        String sql = "UPDATE proveedor SET nombre=?,tipo_servicio=?,pais_operacion=?,telefono=?,email=?,activo=? WHERE id_proveedor=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());       
            ps.setInt(2, p.getTipoServicio());
            ps.setString(3, p.getPaisOperacion()); 
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getEmail());         
            ps.setBoolean(6, p.isActivo());
            ps.setInt(7, p.getIdProveedor());
            return ps.executeUpdate() > 0;
        }
    }

    private Proveedor map(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setIdProveedor(rs.getInt("id_proveedor")); p.setNombre(rs.getString("nombre"));
        p.setTipoServicio(rs.getInt("tipo_servicio")); p.setPaisOperacion(rs.getString("pais_operacion"));
        p.setTelefono(rs.getString("telefono"));     p.setEmail(rs.getString("email"));
        p.setActivo(rs.getBoolean("activo"));        
        return p;
    }
        
}
