/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDateTime;

/**
 *
 * @author jeffm
 */
public class Usuario {
    private Integer idUsuario;
    private String usuario;
    private String contraHasheada;
    private String nombreCompleto;
    private int rol;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    
    public Usuario (){}

    public Usuario(String usuario, String contraHasheada, String nombreCompleto, int rol) {
        this.usuario = usuario;
        this.contraHasheada = contraHasheada;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.activo = true;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContraHasheada() {
        return contraHasheada;
    }

    public void setContraHasheada(String contraHasheada) {
        this.contraHasheada = contraHasheada;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public int getRol() {
        return rol;
    }

    public void setRol(int rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public String getRolNombre() {
        return switch (rol){
            case 1 -> "Atención al cliente";
            case 2 -> "Operaciones";
            case 3 -> "Administrador";
            default ->  throw new IllegalArgumentException ("Desconocido");
        };
    }            
}
