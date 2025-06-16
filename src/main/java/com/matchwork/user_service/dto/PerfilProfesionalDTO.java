package com.matchwork.user_service.dto;

import java.util.List;

/**
 * DTO principal para exponer el perfil profesional completo,
 * incluyendo datos básicos de Usuario, experiencias y estudios.
 */
public class PerfilProfesionalDTO {

    private Long id;                        // corresponde al userId
    private UsuarioDTO usuario;             // datos del usuario anidado
    private String titulo;
    private String fotoUrl;
    private String cvUrl;
    private String presentacion;
    private String disponibilidad;
    private String modoTrabajo;
    private List<ExperienciaDTO> experiencias;
    private List<EstudioDTO> estudios;

    public PerfilProfesionalDTO() {}

    public PerfilProfesionalDTO(
            Long id,
            UsuarioDTO usuario,
            String titulo,
            String fotoUrl,
            String cvUrl,
            String presentacion,
            String disponibilidad,
            String modoTrabajo,
            List<ExperienciaDTO> experiencias,
            List<EstudioDTO> estudios) {
        this.id = id;
        this.usuario = usuario;
        this.titulo = titulo;
        this.fotoUrl = fotoUrl;
        this.cvUrl = cvUrl;
        this.presentacion = presentacion;
        this.disponibilidad = disponibilidad;
        this.modoTrabajo = modoTrabajo;
        this.experiencias = experiencias;
        this.estudios = estudios;
    }

    // ─── Getters / Setters ───

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UsuarioDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioDTO usuario) {
        this.usuario = usuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public String getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(String disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public String getModoTrabajo() {
        return modoTrabajo;
    }

    public void setModoTrabajo(String modoTrabajo) {
        this.modoTrabajo = modoTrabajo;
    }

    public List<ExperienciaDTO> getExperiencias() {
        return experiencias;
    }

    public void setExperiencias(List<ExperienciaDTO> experiencias) {
        this.experiencias = experiencias;
    }

    public List<EstudioDTO> getEstudios() {
        return estudios;
    }

    public void setEstudios(List<EstudioDTO> estudios) {
        this.estudios = estudios;
    }

    public String getCvUrl() {
        return cvUrl;
    }

    public void setCvUrl(String cvUrl) {
        this.cvUrl = cvUrl;
    }

    
}
