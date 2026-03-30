/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author jeffm
 */
public class Proveedor {
    private Integer idProveedor;
    private String nombre;
    private int tipoServicio;
    private String paisOperacion;
    private String telefono;
    private String email;
    private boolean activo;  
    
    public Proveedor(){}

    public Proveedor(String nombre, int tipoServicio, String paisOperacion, String telefono, String email) {
        this.nombre = nombre;
        this.tipoServicio = tipoServicio;
        this.paisOperacion = paisOperacion;
        this.telefono = telefono;
        this.email = email;
        this.activo = true;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(int tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getPaisOperacion() {
        return paisOperacion;
    }

    public void setPaisOperacion(String paisOperacion) {
        this.paisOperacion = paisOperacion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public String getTipoNombre() {
        return switch (tipoServicio){
            case 1 -> "Aerolínea";
            case 2 -> "Hotel";
            case 3 -> "Tour";
            case 4 -> "Traslado";
            default -> "Otro";
        };
    }
}
