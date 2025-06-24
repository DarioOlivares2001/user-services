package com.matchwork.user_service.dto;

import java.time.LocalDate;


public class EstudioDTO {

    private Long id;
    private String nombreInstitucion;
    private String grado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public EstudioDTO() {}

    public EstudioDTO(Long id, String nombreInstitucion, String grado, LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.nombreInstitucion = nombreInstitucion;
        this.grado = grado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    // ─── Getters / Setters ───

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreInstitucion() {
        return nombreInstitucion;
    }

    public void setNombreInstitucion(String nombreInstitucion) {
        this.nombreInstitucion = nombreInstitucion;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
}
