/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.List;

/**
 *
 * @author jeffm
 */
public class Paquete {
    private Integer idPaquete;
    private String nombre;
    private Integer idDestino;
    private String destinoNombre;
    private String destinoPais;
    private int duracionDias;
    private String descripcion;
    private double precioVenta;
    private double costoTotalProveedores;
    private double gananciaBruta;
    private int capacidadMaxima;
    private boolean activo;
    private List<ServicioPaquete> servicios;
    
    public Paquete(){}

    public Paquete(String nombre, Integer idDestino, int duracionDias, String descripcion, double precioVenta, double costoTotalProveedores, int capacidadMaxima) {
        this.nombre = nombre;
        this.idDestino = idDestino;
        this.duracionDias = duracionDias;
        this.descripcion = descripcion;
        this.precioVenta = precioVenta;
        this.costoTotalProveedores = costoTotalProveedores;
        this.capacidadMaxima = capacidadMaxima;
        this.activo = true;
    }

    public Integer getIdPaquete() {
        return idPaquete;
    }

    public void setIdPaquete(Integer idPaquete) {
        this.idPaquete = idPaquete;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getIdDestino() {
        return idDestino;
    }

    public void setIdDestino(Integer idDestino) {
        this.idDestino = idDestino;
    }

    public String getDestinoNombre() {
        return destinoNombre;
    }

    public void setDestinoNombre(String destinoNombre) {
        this.destinoNombre = destinoNombre;
    }

    public String getDestinoPais() {
        return destinoPais;
    }

    public void setDestinoPais(String destinoPais) {
        this.destinoPais = destinoPais;
    }

    public int getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(int duracionDias) {
        this.duracionDias = duracionDias;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public double getCostoTotalProveedores() {
        return costoTotalProveedores;
    }

    public void setCostoTotalProveedores(double costoTotalProveedores) {
        this.costoTotalProveedores = costoTotalProveedores;
    }

    public double getGananciaBruta() {
        return gananciaBruta;
    }

    public void setGananciaBruta(double gananciaBruta) {
        this.gananciaBruta = gananciaBruta;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<ServicioPaquete> getServicios() {
        return servicios;
    }

    public void setServicios(List<ServicioPaquete> servicios) {
        this.servicios = servicios;
    }
    
    
}
