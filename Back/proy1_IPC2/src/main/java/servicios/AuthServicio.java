/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicios;

import daos.UsuarioDAO;
import modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

/**
 *
 * @author jeffm
 */
public class AuthServicio {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    
    public Usuario login (String usuario, String contra) throws SQLException {
        if (usuario == null || contra == null) return null;
        Usuario u = usuarioDAO.obtenerPorUsuario(usuario);
        if (u == null) return null;
        if (!u.isActivo()) return null;
        if (!BCrypt.checkpw(contra, u.getContraHasheada())) return null;
        return u;
    }
    
    public String hashPassword(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt(10));
    }
    
    public boolean validarPassword(String raw, String hash){
        return BCrypt.checkpw(raw, hash);
    }
}
