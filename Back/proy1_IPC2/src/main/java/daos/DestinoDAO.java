/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daos;

import modelo.Destino;
import otros.ConnectionMySQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jeffm
 */
public class DestinoDAO {
    
    ConnectionMySQL connMySQL = new ConnectionMySQL();
    Connection conn = null;
    
    public DestinoDAO(){
            conn = connMySQL.conectar();
    }
    
    public List<Destino> obtenerTodos() throws SQLException {
        List<Destino> lista = new ArrayList<>();
        String sql = "SELECT * FROM destino ORDER BY nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public List<Destino> obtenerActivos() throws SQLException {
        List<Destino> lista = new ArrayList<>();
        String sql = "SELECT * FROM destino WHERE activo=TRUE ORDER BY nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public Destino obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM destino WHERE id_destino=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public Destino obtenerPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM destino WHERE nombre=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public int ingresar(Destino d) throws SQLException {
        String sql = "INSERT INTO destino (nombre,pais,descripcion,clima,mejor_epoca,url_imagen,activo) VALUES (?,?,?,?,?,?,TRUE)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getNombre());   
            ps.setString(2, d.getPais());
            ps.setString(3, d.getDescripcion()); 
            ps.setString(4, d.getClima());
            ps.setString(5, d.getMejorEpoca()); 
            ps.setString(6, d.getUrlImagen());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) { if (gk.next()) return gk.getInt(1); }
        }
        return -1;
    }

    public boolean actualizar(Destino d) throws SQLException {
        String sql = "UPDATE destino SET nombre=?,pais=?,descripcion=?,clima=?,mejor_epoca=?,url_imagen=?,activo=? WHERE id_destino=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getNombre());   
            ps.setString(2, d.getPais());
            ps.setString(3, d.getDescripcion()); 
            ps.setString(4, d.getClima());
            ps.setString(5, d.getMejorEpoca()); 
            ps.setString(6, d.getUrlImagen());
            ps.setBoolean(7, d.isActivo());   
            ps.setInt(8, d.getIdDestino());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM destino WHERE id_destino=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Destino map(ResultSet rs) throws SQLException {
        Destino d = new Destino();
        d.setIdDestino(rs.getInt("id_destino"));
        d.setNombre(rs.getString("nombre"));        
        d.setPais(rs.getString("pais"));
        d.setDescripcion(rs.getString("descripcion")); 
        d.setClima(rs.getString("clima"));
        d.setMejorEpoca(rs.getString("mejor_epoca")); 
        d.setUrlImagen(rs.getString("url_imagen"));
        d.setActivo(rs.getBoolean("activo"));       
        return d;
    }
}
