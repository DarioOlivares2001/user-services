// src/main/java/com/matchwork/user_service/model/PerfilEmpresa.java
package com.matchwork.user_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "perfil_empresa")
@Data @NoArgsConstructor @AllArgsConstructor
public class PerfilEmpresa {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    private String nombreFantasia;
    private String logoUrl;

    @Column(length = 2000)
    private String descripcion;

    private String industria;      // p.ej. "Tecnología", "Salud", etc.
    private String ubicacion;
}
