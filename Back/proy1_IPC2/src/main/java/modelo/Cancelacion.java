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
public class Cancelacion {
    private Integer idCancelacion;
    private Integer idReservacion;
    private String numeroReservacion;
    private LocalDateTime fechaCancelacion;
    private String motivo;
    private double montoPagado;
    private double porcentajeReembolso;
    private double montoReembolso;
    private double perdidaAgencia;
    
    public Cancelacion() {}

    public Cancelacion(Integer idReservacion, String numeroReservacion, LocalDateTime fechaCancelacion, String motivo, double montoPagado, double porcentajeReembolso, double montoReembolso, double perdidaAgencia) {
        this.idReservacion = idReservacion;
        this.numeroReservacion = numeroReservacion;
        this.fechaCancelacion = fechaCancelacion;
        this.motivo = motivo;
        this.montoPagado = montoPagado;
        this.porcentajeReembolso = porcentajeReembolso;
        this.montoReembolso = montoReembolso;
        this.perdidaAgencia = perdidaAgencia;
    }

    public Integer getIdCancelacion() {
        return idCancelacion;
    }

    public void setIdCancelacion(Integer idCancelacion) {
        this.idCancelacion = idCancelacion;
    }

    public Integer getIdReservacion() {
        return idReservacion;
    }

    public void setIdReservacion(Integer idReservacion) {
        this.idReservacion = idReservacion;
    }

    public String getNumeroReservacion() {
        return numeroReservacion;
    }

    public void setNumeroReservacion(String numeroReservacion) {
        this.numeroReservacion = numeroReservacion;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public double getPorcentajeReembolso() {
        return porcentajeReembolso;
    }

    public void setPorcentajeReembolso(double porcentajeReembolso) {
        this.porcentajeReembolso = porcentajeReembolso;
    }

    public double getMontoReembolso() {
        return montoReembolso;
    }

    public void setMontoReembolso(double montoReembolso) {
        this.montoReembolso = montoReembolso;
    }

    public double getPerdidaAgencia() {
        return perdidaAgencia;
    }

    public void setPerdidaAgencia(double perdidaAgencia) {
        this.perdidaAgencia = perdidaAgencia;
    }    
}
