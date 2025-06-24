package com.matchwork.user_service.dto;

import java.time.LocalDate;


public class ExperienciaDTO {

    private Long id;
    private String cargo;
    private String empresa;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public ExperienciaDTO() {}

    public ExperienciaDTO(Long id, String cargo, String empresa, String descripcion, LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.cargo = cargo;
        this.empresa = empresa;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

  

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
