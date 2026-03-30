/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author jeffm
 */
public class Destino {
    private Integer idDestino;
    private String nombre;
    private String pais;
    private String descripcion;
    private String clima;
    private String mejorEpoca;
    private String urlImagen;
    private boolean activo;
    
    public Destino(){}

    public Destino(String nombre, String pais, String descripcion, String clima, String mejorEpoca, String urlImagen) {
        this.nombre = nombre;
        this.pais = pais;
        this.descripcion = descripcion;
        this.clima = clima;
        this.mejorEpoca = mejorEpoca;
        this.urlImagen = urlImagen;
        this.activo = true;
    }

    public Integer getIdDestino() {
        return idDestino;
    }

    public void setIdDestino(Integer idDestino) {
        this.idDestino = idDestino;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getClima() {
        return clima;
    }

    public void setClima(String clima) {
        this.clima = clima;
    }

    public String getMejorEpoca() {
        return mejorEpoca;
    }

    public void setMejorEpoca(String mejorEpoca) {
        this.mejorEpoca = mejorEpoca;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    
    
}
