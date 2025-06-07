
package com.matchwork.user_service.dto;

public class PerfilEmpresaDTO {
    private Long id;
    private String nombreFantasia;
    private String logoUrl;
    private String descripcion;
    private String industria;
    private String ubicacion;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNombreFantasia() {
        return nombreFantasia;
    }
    public void setNombreFantasia(String nombreFantasia) {
        this.nombreFantasia = nombreFantasia;
    }
    public String getLogoUrl() {
        return logoUrl;
    }
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getIndustria() {
        return industria;
    }
    public void setIndustria(String industria) {
        this.industria = industria;
    }
    public String getUbicacion() {
        return ubicacion;
    }
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    
}
