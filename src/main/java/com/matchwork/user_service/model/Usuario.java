package com.matchwork.user_service.model;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String correo;
    private String contrasena;
    private String rol; 
    private String comuna;

   
    @Column(name = "cognito_sub", unique = true)
    private String cognitoSub;

   
    @Column(name = "usa_cognito", columnDefinition = "NUMBER(1) DEFAULT 0")
    private Boolean usaCognito = false;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<UsuarioHabilidad> habilidades = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public List<UsuarioHabilidad> getHabilidades() {
        return habilidades;
    }

    public void setHabilidades(List<UsuarioHabilidad> habilidades) {
        this.habilidades = habilidades;
    }

    public String getCognitoSub() {
        return cognitoSub;
    }

    public void setCognitoSub(String cognitoSub) {
        this.cognitoSub = cognitoSub;
    }

    public Boolean getUsaCognito() {
        return usaCognito;
    }

    public void setUsaCognito(Boolean usaCognito) {
        this.usaCognito = usaCognito;
    }

    
}