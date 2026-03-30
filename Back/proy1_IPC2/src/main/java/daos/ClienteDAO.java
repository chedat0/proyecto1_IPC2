/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daos;

import modelo.Cliente;
import otros.ConnectionMySQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author jeffm
 */
public class ClienteDAO {
    ConnectionMySQL connMySQL = new ConnectionMySQL();
    Connection conn = null;
    
    public ClienteDAO(){
            conn = connMySQL.conectar();
    }
    public Cliente obtenerPorDPI(String dpi) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE dpi_pasaporte = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dpi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }
    
    public Cliente obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE id_cliente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Cliente> obtenerTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente WHERE activo=TRUE ORDER BY nombre_completo";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public List<Cliente> busqueda(String q) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente WHERE activo=TRUE AND (nombre_completo LIKE ? OR dpi_pasaporte LIKE ? OR email LIKE ?) ORDER BY nombre_completo";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String pat = "%" + q + "%";
            ps.setString(1, pat); ps.setString(2, pat); ps.setString(3, pat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        }
        return lista;
    }

    public List<Cliente> obtenerPorReservacion(int idReservacion) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.* FROM cliente c JOIN reservacion_pasajero rp ON c.id_cliente=rp.id_cliente WHERE rp.id_reservacion=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReservacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        }
        return lista;
    }

    public int ingresar(Cliente cl) throws SQLException {
        String sql = "INSERT INTO cliente (dpi_pasaporte, nombre_completo, fecha_nacimiento, telefono, email, nacionalidad, activo) VALUES (?,?,?,?,?,?,TRUE)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cl.getDpiPasaporte());
            ps.setString(2, cl.getNombreCompleto());
            ps.setDate(3, cl.getFechaNacimiento() != null ? Date.valueOf(cl.getFechaNacimiento()) : null);
            ps.setString(4, cl.getTelefono());
            ps.setString(5, cl.getEmail());
            ps.setString(6, cl.getNacionalidad());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) return gk.getInt(1);
            }
        }
        return -1;
    }

    public boolean actualizar(Cliente cl) throws SQLException {
        String sql = "UPDATE cliente SET nombre_completo=?, fecha_nacimiento=?, telefono=?, email=?, nacionalidad=? WHERE id_cliente=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cl.getNombreCompleto());
            ps.setDate(2, cl.getFechaNacimiento() != null ? Date.valueOf(cl.getFechaNacimiento()) : null);
            ps.setString(3, cl.getTelefono());
            ps.setString(4, cl.getEmail());
            ps.setString(5, cl.getNacionalidad());
            ps.setInt(6, cl.getIdCliente());
            return ps.executeUpdate() > 0;
        }
    }

    private Cliente map(ResultSet rs) throws SQLException {
        Cliente cl = new Cliente();
        cl.setIdCliente(rs.getInt("id_cliente"));
        cl.setDpiPasaporte(rs.getString("dpi_pasaporte"));
        cl.setNombreCompleto(rs.getString("nombre_completo"));
        Date d = rs.getDate("fecha_nacimiento");
        if (d != null) cl.setFechaNacimiento(d.toLocalDate());
        cl.setTelefono(rs.getString("telefono"));
        cl.setEmail(rs.getString("email"));
        cl.setNacionalidad(rs.getString("nacionalidad"));
        cl.setActivo(rs.getBoolean("activo"));
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) cl.setFechaRegistro(ts.toLocalDateTime());
        return cl;
    }
}
