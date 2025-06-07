package com.matchwork.user_service.dto;

public class UsuarioHabilidadDTO {
    private Long id;
    private String nombre;

    public UsuarioHabilidadDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
